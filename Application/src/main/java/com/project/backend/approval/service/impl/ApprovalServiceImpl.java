package com.project.backend.approval.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.backend.approval.dto.ApprovalActionDTO;
import com.project.backend.approval.dto.ApprovalInstanceQueryDTO;
import com.project.backend.approval.dto.ApprovalRecordQueryDTO;
import com.project.backend.approval.entity.ApprovalFlow;
import com.project.backend.approval.entity.ApprovalFlowBinding;
import com.project.backend.approval.entity.ApprovalInstance;
import com.project.backend.approval.entity.ApprovalNode;
import com.project.backend.approval.entity.ApprovalNodeAssignee;
import com.project.backend.approval.entity.ApprovalRecord;
import com.project.backend.approval.mapper.ApprovalFlowBindingMapper;
import com.project.backend.approval.mapper.ApprovalFlowMapper;
import com.project.backend.approval.mapper.ApprovalInstanceMapper;
import com.project.backend.approval.mapper.ApprovalNodeAssigneeMapper;
import com.project.backend.approval.mapper.ApprovalNodeMapper;
import com.project.backend.approval.mapper.ApprovalRecordMapper;
import com.project.backend.approval.service.ApprovalService;
import com.project.backend.approval.vo.ApprovalAssigneeVO;
import com.project.backend.approval.vo.ApprovalInstanceVO;
import com.project.backend.approval.vo.ApprovalNodeVO;
import com.project.backend.approval.vo.ApprovalRecordVO;
import com.project.backend.system.entity.Role;
import com.project.backend.system.entity.User;
import com.project.backend.system.mapper.RoleMapper;
import com.project.backend.system.mapper.UserMapper;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.common.service.StudentInfoEnricher;
import com.project.backend.util.DictUtils;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审批核心Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalFlowMapper flowMapper;
    private final ApprovalFlowBindingMapper bindingMapper;
    private final ApprovalNodeMapper nodeMapper;
    private final ApprovalNodeAssigneeMapper assigneeMapper;
    private final ApprovalInstanceMapper instanceMapper;
    private final ApprovalRecordMapper recordMapper;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final StudentInfoEnricher studentInfoEnricher;

    /**
     * 业务类型映射
     */
    private String getBusinessTypeText(String businessType) {
        return DictUtils.getLabel("approval_business_type", businessType, businessType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startApproval(String businessType, Long businessId, Long applicantId, String applicantName) {
        // 检查是否已有审批实例
        ApprovalInstance existing = instanceMapper.selectByBusiness(businessType, businessId);
        if (existing != null) {
            throw new BusinessException("该申请已发起审批，请勿重复提交");
        }

        // 获取绑定的流程
        ApprovalFlowBinding binding = bindingMapper.selectByBusinessType(businessType);
        if (binding == null || binding.getStatus() != 1) {
            // 无流程绑定时返回null，表示不需要审批，直接通过
            log.info("业务类型 {} 未配置审批流程，无需审批，直接通过", businessType);
            return null;
        }

        // 获取流程
        ApprovalFlow flow = flowMapper.selectById(binding.getFlowId());
        if (flow == null || flow.getStatus() != 1) {
            throw new BusinessException("审批流程不存在或已停用");
        }

        // 获取第一个节点
        ApprovalNode firstNode = nodeMapper.selectFirstNode(flow.getId());
        if (firstNode == null) {
            throw new BusinessException("审批流程未配置节点");
        }

        // 创建审批实例
        ApprovalInstance instance = new ApprovalInstance();
        instance.setFlowId(flow.getId());
        instance.setFlowName(flow.getFlowName());
        instance.setBusinessType(businessType);
        instance.setBusinessId(businessId);
        instance.setApplicantId(applicantId);
        instance.setApplicantName(applicantName);
        instance.setCurrentNodeId(firstNode.getId());
        instance.setCurrentNodeName(firstNode.getNodeName());
        instance.setStatus(1); // 进行中
        instance.setStartTime(LocalDateTime.now());

        instanceMapper.insert(instance);
        log.info("创建审批实例，ID：{}，业务类型：{}，业务ID：{}", instance.getId(), businessType, businessId);

        return instance.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doApprove(ApprovalActionDTO actionDTO, Long approverId, String approverName) {
        ApprovalInstance instance = instanceMapper.selectById(actionDTO.getInstanceId());
        if (instance == null) {
            throw new BusinessException("审批实例不存在");
        }
        if (instance.getStatus() != 1) {
            throw new BusinessException("审批已结束，无法操作");
        }

        // 获取当前节点
        ApprovalNode currentNode = nodeMapper.selectById(instance.getCurrentNodeId());
        if (currentNode == null) {
            throw new BusinessException("当前审批节点不存在");
        }

        // 验证审批权限
        if (!hasApprovePermission(currentNode.getId(), approverId, null)) {
            throw new BusinessException("您没有权限审批此申请");
        }

        // 检查是否已审批过（同一节点不能重复审批）
        LambdaQueryWrapper<ApprovalRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(ApprovalRecord::getInstanceId, instance.getId())
                     .eq(ApprovalRecord::getNodeId, currentNode.getId())
                     .eq(ApprovalRecord::getApproverId, approverId);
        if (recordMapper.selectCount(recordWrapper) > 0) {
            throw new BusinessException("您已审批过此节点，请勿重复操作");
        }

        // 创建审批记录
        ApprovalRecord record = new ApprovalRecord();
        record.setInstanceId(instance.getId());
        record.setNodeId(currentNode.getId());
        record.setNodeName(currentNode.getNodeName());
        record.setApproverId(approverId);
        record.setApproverName(approverName);
        record.setAction(actionDTO.getAction());
        record.setOpinion(actionDTO.getOpinion());
        record.setApproveTime(LocalDateTime.now());
        recordMapper.insert(record);

        // 处理审批结果
        if (actionDTO.getAction() == 2) {
            // 拒绝
            handleReject(instance, currentNode);
        } else {
            // 通过 - 检查是否需要流转
            handleApprove(instance, currentNode);
        }

        return true;
    }

    /**
     * 处理审批通过
     */
    private void handleApprove(ApprovalInstance instance, ApprovalNode currentNode) {
        boolean shouldMoveNext = false;

        if (currentNode.getNodeType() == 1) {
            // 串行：直接流转
            shouldMoveNext = true;
        } else if (currentNode.getNodeType() == 2) {
            // 会签：检查是否所有人都审批通过
            int totalAssignees = countNodeAssignees(currentNode.getId());
            int approvedCount = recordMapper.countApprovedByInstanceAndNode(instance.getId(), currentNode.getId());
            shouldMoveNext = approvedCount >= totalAssignees;
        } else if (currentNode.getNodeType() == 3) {
            // 或签：任一人通过即流转
            shouldMoveNext = true;
        }

        if (shouldMoveNext) {
            // 获取下一个节点
            ApprovalNode nextNode = nodeMapper.selectNextNode(instance.getFlowId(), currentNode.getNodeOrder());
            if (nextNode != null) {
                // 流转到下一节点
                instance.setCurrentNodeId(nextNode.getId());
                instance.setCurrentNodeName(nextNode.getNodeName());
                instanceMapper.updateById(instance);
                log.info("审批流转，实例ID：{}，从节点 {} 到节点 {}", instance.getId(), currentNode.getNodeName(), nextNode.getNodeName());
            } else {
                // 没有下一节点，审批完成
                instance.setStatus(2); // 已通过
                instance.setEndTime(LocalDateTime.now());
                instance.setCurrentNodeId(null);
                instance.setCurrentNodeName("已完成");
                instanceMapper.updateById(instance);
                log.info("审批完成，实例ID：{}，状态：已通过", instance.getId());

                // 更新业务状态
                updateBusinessStatus(instance.getBusinessType(), instance.getBusinessId(), 2);
            }
        }
    }

    /**
     * 处理审批拒绝
     */
    private void handleReject(ApprovalInstance instance, ApprovalNode currentNode) {
        int rejectAction = currentNode.getRejectAction();

        if (rejectAction == 1) {
            // 直接结束
            instance.setStatus(3); // 已拒绝
            instance.setEndTime(LocalDateTime.now());
            instance.setCurrentNodeId(null);
            instance.setCurrentNodeName("已拒绝");
            instanceMapper.updateById(instance);
            log.info("审批拒绝-直接结束，实例ID：{}", instance.getId());

            // 更新业务状态
            updateBusinessStatus(instance.getBusinessType(), instance.getBusinessId(), 3);
        } else if (rejectAction == 2) {
            // 退回申请人（重新开始流程）
            ApprovalNode firstNode = nodeMapper.selectFirstNode(instance.getFlowId());
            instance.setCurrentNodeId(firstNode.getId());
            instance.setCurrentNodeName(firstNode.getNodeName());
            instanceMapper.updateById(instance);
            log.info("审批拒绝-退回申请人，实例ID：{}", instance.getId());
        } else if (rejectAction == 3) {
            // 退回上一节点
            ApprovalNode prevNode = nodeMapper.selectPrevNode(instance.getFlowId(), currentNode.getNodeOrder());
            if (prevNode != null) {
                instance.setCurrentNodeId(prevNode.getId());
                instance.setCurrentNodeName(prevNode.getNodeName());
                instanceMapper.updateById(instance);
                log.info("审批拒绝-退回上一节点，实例ID：{}，退回到：{}", instance.getId(), prevNode.getNodeName());
            } else {
                // 没有上一节点，按退回申请人处理
                ApprovalNode firstNode = nodeMapper.selectFirstNode(instance.getFlowId());
                instance.setCurrentNodeId(firstNode.getId());
                instance.setCurrentNodeName(firstNode.getNodeName());
                instanceMapper.updateById(instance);
            }
        }
    }

    /**
     * 更新业务状态
     */
    private void updateBusinessStatus(String businessType, Long businessId, Integer status) {
        // 这里需要根据业务类型更新对应表的状态
        // 由于涉及多个表，建议通过事件或回调方式处理
        log.info("更新业务状态，类型：{}，ID：{}，状态：{}", businessType, businessId, status);
    }

    /**
     * 统计节点审批人数
     */
    private int countNodeAssignees(Long nodeId) {
        List<ApprovalNodeAssignee> assignees = assigneeMapper.selectByNodeId(nodeId);
        // 如果是角色，需要统计角色下的用户数
        // 简化处理：返回审批人配置数
        return assignees.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdraw(Long instanceId, Long applicantId) {
        ApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException("审批实例不存在");
        }
        if (!instance.getApplicantId().equals(applicantId)) {
            throw new BusinessException("只能撤回自己的申请");
        }
        if (instance.getStatus() != 1) {
            throw new BusinessException("只能撤回进行中的申请");
        }

        instance.setStatus(4); // 已撤回
        instance.setEndTime(LocalDateTime.now());
        instance.setCurrentNodeId(null);
        instance.setCurrentNodeName("已撤回");
        instanceMapper.updateById(instance);

        log.info("撤回审批，实例ID：{}", instanceId);
        return true;
    }

    @Override
    public ApprovalInstanceVO getInstanceDetail(Long instanceId) {
        ApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            return null;
        }
        return convertInstanceToVO(instance);
    }

    @Override
    public ApprovalInstanceVO getInstanceByBusiness(String businessType, Long businessId) {
        ApprovalInstance instance = instanceMapper.selectByBusiness(businessType, businessId);
        if (instance == null) {
            return null;
        }
        return convertInstanceToVO(instance);
    }

    @Override
    public PageResult<ApprovalInstanceVO> pagePendingList(ApprovalInstanceQueryDTO queryDTO, Long userId, List<Long> roleIds) {
        // 获取用户可审批的节点ID列表
        Set<Long> nodeIds = getApproveNodeIds(userId, roleIds);
        if (nodeIds.isEmpty()) {
            return PageResult.build(new ArrayList<>(), 0L, queryDTO.getPageNum(), queryDTO.getPageSize());
        }

        // 查询待审批实例
        LambdaQueryWrapper<ApprovalInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalInstance::getStatus, 1) // 进行中
               .in(ApprovalInstance::getCurrentNodeId, nodeIds)
               .eq(StrUtil.isNotBlank(queryDTO.getBusinessType()), ApprovalInstance::getBusinessType, queryDTO.getBusinessType())
               .like(StrUtil.isNotBlank(queryDTO.getApplicantName()), ApprovalInstance::getApplicantName, queryDTO.getApplicantName())
               .like(StrUtil.isNotBlank(queryDTO.getFlowName()), ApprovalInstance::getFlowName, queryDTO.getFlowName())
               .orderByDesc(ApprovalInstance::getCreateTime);

        Page<ApprovalInstance> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        instanceMapper.selectPage(page, wrapper);

        List<ApprovalInstanceVO> voList = page.getRecords().stream()
                .map(this::convertInstanceToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PageResult<ApprovalRecordVO> pageRecordList(ApprovalRecordQueryDTO queryDTO, Long approverId) {
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(approverId != null, ApprovalRecord::getApproverId, approverId)
               .eq(queryDTO.getAction() != null, ApprovalRecord::getAction, queryDTO.getAction())
               .like(StrUtil.isNotBlank(queryDTO.getApproverName()), ApprovalRecord::getApproverName, queryDTO.getApproverName())
               .orderByDesc(ApprovalRecord::getApproveTime);

        Page<ApprovalRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        recordMapper.selectPage(page, wrapper);

        List<ApprovalRecordVO> voList = page.getRecords().stream()
                .map(this::convertRecordToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<ApprovalRecordVO> getRecordsByInstance(Long instanceId) {
        List<ApprovalRecord> records = recordMapper.selectByInstanceId(instanceId);
        return records.stream()
                .map(this::convertRecordToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canApprove(Long instanceId, Long userId, List<Long> roleIds) {
        ApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null || instance.getStatus() != 1 || instance.getCurrentNodeId() == null) {
            return false;
        }
        return hasApprovePermission(instance.getCurrentNodeId(), userId, roleIds);
    }

    @Override
    public Map<String, Long> getPendingCount(Long userId, List<Long> roleIds) {
        Map<String, Long> countMap = new HashMap<>();

        Set<Long> nodeIds = getApproveNodeIds(userId, roleIds);
        if (nodeIds.isEmpty()) {
            return countMap;
        }

        // 按业务类型统计
        String[] businessTypes = {"check_in", "transfer", "check_out", "stay"};
        for (String businessType : businessTypes) {
            LambdaQueryWrapper<ApprovalInstance> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ApprovalInstance::getStatus, 1)
                   .eq(ApprovalInstance::getBusinessType, businessType)
                   .in(ApprovalInstance::getCurrentNodeId, nodeIds);
            long count = instanceMapper.selectCount(wrapper);
            countMap.put(businessType, count);
        }

        return countMap;
    }

    /**
     * 获取用户可审批的节点ID集合
     */
    private Set<Long> getApproveNodeIds(Long userId, List<Long> roleIds) {
        Set<Long> nodeIds = new HashSet<>();

        // 查询用户直接指派的节点
        List<Long> userNodeIds = assigneeMapper.selectNodeIdsByUserId(userId);
        nodeIds.addAll(userNodeIds);

        // 查询用户角色指派的节点
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                List<Long> roleNodeIds = assigneeMapper.selectNodeIdsByRoleId(roleId);
                nodeIds.addAll(roleNodeIds);
            }
        }

        return nodeIds;
    }

    /**
     * 检查是否有审批权限
     */
    private boolean hasApprovePermission(Long nodeId, Long userId, List<Long> roleIds) {
        List<ApprovalNodeAssignee> assignees = assigneeMapper.selectByNodeId(nodeId);
        for (ApprovalNodeAssignee assignee : assignees) {
            if (assignee.getAssigneeType() == 2 && assignee.getAssigneeId().equals(userId)) {
                return true;
            }
            if (assignee.getAssigneeType() == 1 && roleIds != null && roleIds.contains(assignee.getAssigneeId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 实例转换为VO
     */
    private ApprovalInstanceVO convertInstanceToVO(ApprovalInstance instance) {
        ApprovalInstanceVO vo = new ApprovalInstanceVO();
        BeanUtil.copyProperties(instance, vo);
        vo.setBusinessTypeText(getBusinessTypeText(instance.getBusinessType()));
        vo.setStatusText(getStatusText(instance.getStatus()));

        // 加载审批记录
        List<ApprovalRecordVO> records = getRecordsByInstance(instance.getId());
        vo.setRecords(records);

        // 加载流程节点
        List<ApprovalNode> nodes = nodeMapper.selectByFlowId(instance.getFlowId());
        List<ApprovalNodeVO> nodeVOs = nodes.stream().map(node -> {
            ApprovalNodeVO nodeVO = new ApprovalNodeVO();
            BeanUtil.copyProperties(node, nodeVO);
            
            // 加载审批人列表
            List<ApprovalNodeAssignee> assignees = assigneeMapper.selectByNodeId(node.getId());
            List<ApprovalAssigneeVO> assigneeVOs = new ArrayList<>();
            for (ApprovalNodeAssignee assignee : assignees) {
                ApprovalAssigneeVO assigneeVO = new ApprovalAssigneeVO();
                BeanUtil.copyProperties(assignee, assigneeVO);
                assigneeVO.setAssigneeTypeText(assignee.getAssigneeType() == 1 ? "角色" : "用户");
                
                // 获取审批人姓名
                if (assignee.getAssigneeType() == 1) {
                    Role role = roleMapper.selectById(assignee.getAssigneeId());
                    assigneeVO.setAssigneeName(role != null ? role.getRoleName() : "未知角色");
                } else {
                    User user = userMapper.selectById(assignee.getAssigneeId());
                    assigneeVO.setAssigneeName(user != null ? user.getNickname() : "未知用户");
                }
                assigneeVOs.add(assigneeVO);
            }
            nodeVO.setAssignees(assigneeVOs);
            
            return nodeVO;
        }).collect(Collectors.toList());
        vo.setNodes(nodeVOs);

        // 填充学生详细信息（当业务类型为学生相关业务时）
        if (isStudentBusinessType(instance.getBusinessType()) && instance.getApplicantId() != null) {
            Student student = studentMapper.selectById(instance.getApplicantId());
            if (student != null) {
                studentInfoEnricher.enrichStudentInfo(student, vo, "campusName");
            }
        }

        return vo;
    }

    /**
     * 判断是否为学生相关业务类型
     */
    private boolean isStudentBusinessType(String businessType) {
        return "check_in".equals(businessType) 
            || "check_out".equals(businessType)
            || "transfer".equals(businessType)
            || "stay".equals(businessType);
    }

    /**
     * 记录转换为VO
     */
    private ApprovalRecordVO convertRecordToVO(ApprovalRecord record) {
        ApprovalRecordVO vo = new ApprovalRecordVO();
        BeanUtil.copyProperties(record, vo);
        vo.setActionText(DictUtils.getLabel("approval_action", record.getAction(), "未知"));

        // 获取实例信息
        ApprovalInstance instance = instanceMapper.selectById(record.getInstanceId());
        if (instance != null) {
            vo.setBusinessType(instance.getBusinessType());
            vo.setBusinessTypeText(getBusinessTypeText(instance.getBusinessType()));
            vo.setApplicantName(instance.getApplicantName());
            vo.setFlowName(instance.getFlowName());
        }

        return vo;
    }

    /**
     * 状态文本
     */
    private String getStatusText(Integer status) {
        return DictUtils.getLabel("approval_instance_status", status, "未知");
    }

    @Override
    public boolean requiresApproval(String businessType) {
        // 检查是否有启用的流程绑定
        ApprovalFlowBinding binding = bindingMapper.selectByBusinessType(businessType);
        if (binding == null || binding.getStatus() != 1) {
            return false;
        }

        // 检查流程是否启用
        ApprovalFlow flow = flowMapper.selectById(binding.getFlowId());
        if (flow == null || flow.getStatus() != 1) {
            return false;
        }

        return true;
    }
}


