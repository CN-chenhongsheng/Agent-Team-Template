package com.project.backend.approval.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.approval.dto.ApprovalAssigneeSaveDTO;
import com.project.backend.approval.dto.ApprovalFlowBindingDTO;
import com.project.backend.approval.dto.ApprovalFlowQueryDTO;
import com.project.backend.approval.dto.ApprovalFlowSaveDTO;
import com.project.backend.approval.dto.ApprovalNodeSaveDTO;
import com.project.backend.approval.entity.ApprovalFlow;
import com.project.backend.approval.entity.ApprovalFlowBinding;
import com.project.backend.approval.entity.ApprovalNode;
import com.project.backend.approval.entity.ApprovalNodeAssignee;
import com.project.backend.approval.mapper.ApprovalFlowBindingMapper;
import com.project.backend.approval.mapper.ApprovalFlowMapper;
import com.project.backend.approval.mapper.ApprovalNodeAssigneeMapper;
import com.project.backend.approval.mapper.ApprovalNodeMapper;
import com.project.backend.approval.service.ApprovalFlowService;
import com.project.backend.approval.vo.ApprovalAssigneeVO;
import com.project.backend.approval.vo.ApprovalFlowBindingVO;
import com.project.backend.approval.vo.ApprovalFlowVO;
import com.project.backend.approval.vo.ApprovalNodeVO;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.system.entity.Role;
import com.project.backend.system.entity.User;
import com.project.backend.system.mapper.RoleMapper;
import com.project.backend.system.mapper.UserMapper;
import com.project.backend.util.DictUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审批流程Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalFlowServiceImpl extends ServiceImpl<ApprovalFlowMapper, ApprovalFlow> implements ApprovalFlowService {

    private final ApprovalNodeMapper nodeMapper;
    private final ApprovalNodeAssigneeMapper assigneeMapper;
    private final ApprovalFlowBindingMapper bindingMapper;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    /**
     * 业务类型映射（从字典动态获取）
     */
    private String getBusinessTypeText(String businessType) {
        return DictUtils.getLabel("approval_business_type", businessType, businessType);
    }

    /**
     * 节点类型映射
     */
    private String getNodeTypeText(Integer nodeType) {
        return DictUtils.getLabel("approval_node_type", nodeType, "未知");
    }

    /**
     * 拒绝处理映射
     */
    private String getRejectActionText(Integer rejectAction) {
        return DictUtils.getLabel("approval_reject_action", rejectAction, "未知");
    }

    @Override
    public PageResult<ApprovalFlowVO> pageList(ApprovalFlowQueryDTO queryDTO) {
        LambdaQueryWrapper<ApprovalFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getFlowName()), ApprovalFlow::getFlowName, queryDTO.getFlowName())
               .like(StrUtil.isNotBlank(queryDTO.getFlowCode()), ApprovalFlow::getFlowCode, queryDTO.getFlowCode())
               .eq(StrUtil.isNotBlank(queryDTO.getBusinessType()), ApprovalFlow::getBusinessType, queryDTO.getBusinessType())
               .eq(queryDTO.getStatus() != null, ApprovalFlow::getStatus, queryDTO.getStatus())
               .orderByDesc(ApprovalFlow::getCreateTime);

        Page<ApprovalFlow> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        // 批量查询所有流程的绑定数量（消除 N+1）
        Set<Long> flowIds = page.getRecords().stream()
                .map(ApprovalFlow::getId).collect(Collectors.toSet());
        Map<Long, Long> bindingCountMap = batchCountBindings(flowIds);

        List<ApprovalFlowVO> voList = page.getRecords().stream()
                .map(flow -> convertToVO(flow, bindingCountMap))
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<ApprovalFlowVO> listAll(String businessType) {
        LambdaQueryWrapper<ApprovalFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalFlow::getStatus, 1)
               .eq(StrUtil.isNotBlank(businessType), ApprovalFlow::getBusinessType, businessType)
               .orderByDesc(ApprovalFlow::getCreateTime);

        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ApprovalFlowVO getDetailById(Long id) {
        ApprovalFlow flow = getById(id);
        if (flow == null) {
            throw new BusinessException("流程不存在");
        }
        return convertToDetailVO(flow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveFlow(ApprovalFlowSaveDTO saveDTO) {
        // 检查流程编码是否重复
        LambdaQueryWrapper<ApprovalFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalFlow::getFlowCode, saveDTO.getFlowCode())
               .eq(ApprovalFlow::getDeleted, 0);
        if (saveDTO.getId() != null) {
            wrapper.ne(ApprovalFlow::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("流程编码已存在");
        }

        ApprovalFlow flow = new ApprovalFlow();
        BeanUtil.copyProperties(saveDTO, flow, "nodes");

        boolean success;
        if (saveDTO.getId() == null) {
            // 新增
            success = save(flow);
        } else {
            // 编辑 - 先删除原有节点和审批人
            deleteFlowNodesAndAssignees(saveDTO.getId());
            success = updateById(flow);
        }

        // 保存节点和审批人
        if (success && saveDTO.getNodes() != null && !saveDTO.getNodes().isEmpty()) {
            for (ApprovalNodeSaveDTO nodeDTO : saveDTO.getNodes()) {
                ApprovalNode node = new ApprovalNode();
                BeanUtil.copyProperties(nodeDTO, node, "assignees");
                node.setFlowId(flow.getId());
                nodeMapper.insert(node);

                // 保存审批人
                if (nodeDTO.getAssignees() != null && !nodeDTO.getAssignees().isEmpty()) {
                    for (int i = 0; i < nodeDTO.getAssignees().size(); i++) {
                        ApprovalAssigneeSaveDTO assigneeDTO = nodeDTO.getAssignees().get(i);
                        ApprovalNodeAssignee assignee = new ApprovalNodeAssignee();
                        assignee.setNodeId(node.getId());
                        assignee.setAssigneeType(assigneeDTO.getAssigneeType());
                        assignee.setAssigneeId(assigneeDTO.getAssigneeId());
                        assignee.setSortOrder(i);
                        assigneeMapper.insert(assignee);
                    }
                }
            }
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFlow(Long id) {
        ApprovalFlow flow = getById(id);
        if (flow == null) {
            throw new BusinessException("流程不存在");
        }

        // 检查是否已绑定业务
        if (bindingMapper.countByFlowId(id) > 0) {
            throw new BusinessException("流程已绑定业务，请先解绑");
        }

        // 删除节点和审批人
        deleteFlowNodesAndAssignees(id);

        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        ApprovalFlow flow = getById(id);
        if (flow == null) {
            throw new BusinessException("流程不存在");
        }
        flow.setStatus(status);
        return updateById(flow);
    }

    @Override
    public ApprovalFlowBindingVO getBindingByBusinessType(String businessType) {
        ApprovalFlowBinding binding = bindingMapper.selectByBusinessType(businessType);
        if (binding == null) {
            return null;
        }
        return convertBindingToVO(binding);
    }

    @Override
    public List<ApprovalFlowBindingVO> listAllBindings() {
        LambdaQueryWrapper<ApprovalFlowBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ApprovalFlowBinding::getBusinessType);

        return bindingMapper.selectList(wrapper).stream()
                .map(this::convertBindingToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindFlow(ApprovalFlowBindingDTO bindingDTO) {
        // 检查流程是否存在
        ApprovalFlow flow = getById(bindingDTO.getFlowId());
        if (flow == null) {
            throw new BusinessException("流程不存在");
        }

        // 检查是否已绑定
        ApprovalFlowBinding existing = bindingMapper.selectByBusinessType(bindingDTO.getBusinessType());

        ApprovalFlowBinding binding = new ApprovalFlowBinding();
        BeanUtil.copyProperties(bindingDTO, binding);

        if (existing != null) {
            // 更新
            binding.setId(existing.getId());
            return bindingMapper.updateById(binding) > 0;
        } else {
            // 新增
            return bindingMapper.insert(binding) > 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbindFlow(String businessType) {
        LambdaQueryWrapper<ApprovalFlowBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalFlowBinding::getBusinessType, businessType);
        return bindingMapper.delete(wrapper) > 0;
    }

    /**
     * 删除流程的所有节点和审批人
     */
    private void deleteFlowNodesAndAssignees(Long flowId) {
        List<ApprovalNode> nodes = nodeMapper.selectByFlowId(flowId);
        if (!nodes.isEmpty()) {
            List<Long> nodeIds = nodes.stream().map(ApprovalNode::getId).toList();
            assigneeMapper.deleteByNodeIds(nodeIds);
        }
        nodeMapper.deleteByFlowId(flowId);
    }

    /**
     * 批量统计流程绑定数量
     */
    private Map<Long, Long> batchCountBindings(Set<Long> flowIds) {
        if (flowIds == null || flowIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<ApprovalFlowBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ApprovalFlowBinding::getFlowId, flowIds);
        List<ApprovalFlowBinding> bindings = bindingMapper.selectList(wrapper);
        return bindings.stream()
                .collect(Collectors.groupingBy(ApprovalFlowBinding::getFlowId, Collectors.counting()));
    }

    /**
     * 转换为VO（不含节点，使用预加载的绑定数量）
     */
    private ApprovalFlowVO convertToVO(ApprovalFlow flow, Map<Long, Long> bindingCountMap) {
        ApprovalFlowVO vo = new ApprovalFlowVO();
        BeanUtil.copyProperties(flow, vo);
        vo.setBusinessTypeText(getBusinessTypeText(flow.getBusinessType()));
        vo.setStatusText(flow.getStatus() == 1 ? "启用" : "停用");
        vo.setBound(bindingCountMap.getOrDefault(flow.getId(), 0L) > 0);
        return vo;
    }

    /**
     * 转换为VO（不含节点，单条查询绑定）
     */
    private ApprovalFlowVO convertToVO(ApprovalFlow flow) {
        ApprovalFlowVO vo = new ApprovalFlowVO();
        BeanUtil.copyProperties(flow, vo);
        vo.setBusinessTypeText(getBusinessTypeText(flow.getBusinessType()));
        vo.setStatusText(flow.getStatus() == 1 ? "启用" : "停用");
        vo.setBound(bindingMapper.countByFlowId(flow.getId()) > 0);
        return vo;
    }

    /**
     * 转换为详情VO（含节点和审批人，批量查询消除 N+1）
     */
    private ApprovalFlowVO convertToDetailVO(ApprovalFlow flow) {
        ApprovalFlowVO vo = convertToVO(flow);

        // 加载节点
        List<ApprovalNode> nodes = nodeMapper.selectByFlowId(flow.getId());
        if (nodes.isEmpty()) {
            vo.setNodes(new ArrayList<>());
            return vo;
        }

        // 1. 收集所有节点的审批人并批量加载
        Set<Long> roleIdsToLoad = new HashSet<>();
        Set<Long> userIdsToLoad = new HashSet<>();
        Map<Long, List<ApprovalNodeAssignee>> assigneesByNode = new java.util.HashMap<>();

        for (ApprovalNode node : nodes) {
            List<ApprovalNodeAssignee> assignees = assigneeMapper.selectByNodeId(node.getId());
            assigneesByNode.put(node.getId(), assignees);
            for (ApprovalNodeAssignee a : assignees) {
                if (a.getAssigneeType() == 1) {
                    roleIdsToLoad.add(a.getAssigneeId());
                } else {
                    userIdsToLoad.add(a.getAssigneeId());
                }
            }
        }

        // 2. 批量查询角色和用户名称
        Map<Long, String> roleNameMap = roleIdsToLoad.isEmpty() ? Map.of()
                : roleMapper.selectBatchIds(roleIdsToLoad).stream()
                        .collect(Collectors.toMap(Role::getId, Role::getRoleName, (a, b) -> a));
        Map<Long, String> userNameMap = userIdsToLoad.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIdsToLoad).stream()
                        .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));

        // 3. 组装节点 VO
        List<ApprovalNodeVO> nodeVOs = new ArrayList<>();
        for (ApprovalNode node : nodes) {
            ApprovalNodeVO nodeVO = new ApprovalNodeVO();
            BeanUtil.copyProperties(node, nodeVO);
            nodeVO.setNodeTypeText(getNodeTypeText(node.getNodeType()));
            nodeVO.setRejectActionText(getRejectActionText(node.getRejectAction()));

            List<ApprovalNodeAssignee> assignees = assigneesByNode.getOrDefault(node.getId(), List.of());
            List<ApprovalAssigneeVO> assigneeVOs = new ArrayList<>();
            for (ApprovalNodeAssignee assignee : assignees) {
                ApprovalAssigneeVO assigneeVO = new ApprovalAssigneeVO();
                BeanUtil.copyProperties(assignee, assigneeVO);
                assigneeVO.setAssigneeTypeText(DictUtils.getLabel("approval_assignee_type", assignee.getAssigneeType(), "未知"));
                assigneeVO.setAssigneeName(assignee.getAssigneeType() == 1
                        ? roleNameMap.getOrDefault(assignee.getAssigneeId(), "未知角色")
                        : userNameMap.getOrDefault(assignee.getAssigneeId(), "未知用户"));
                assigneeVOs.add(assigneeVO);
            }
            nodeVO.setAssignees(assigneeVOs);
            nodeVOs.add(nodeVO);
        }
        vo.setNodes(nodeVOs);

        return vo;
    }

    /**
     * 绑定转换为VO
     */
    private ApprovalFlowBindingVO convertBindingToVO(ApprovalFlowBinding binding) {
        ApprovalFlowBindingVO vo = new ApprovalFlowBindingVO();
        BeanUtil.copyProperties(binding, vo);
        vo.setBusinessTypeText(getBusinessTypeText(binding.getBusinessType()));
        vo.setStatusText(binding.getStatus() == 1 ? "启用" : "停用");

        // 获取流程信息
        ApprovalFlow flow = getById(binding.getFlowId());
        if (flow != null) {
            vo.setFlowName(flow.getFlowName());
            vo.setFlowCode(flow.getFlowCode());
        }

        return vo;
    }
}
