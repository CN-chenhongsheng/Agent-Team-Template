package com.project.backend.accommodation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.accommodation.dto.stay.StayQueryDTO;
import com.project.backend.accommodation.dto.stay.StaySaveDTO;
import com.project.backend.accommodation.entity.Stay;
import com.project.backend.accommodation.mapper.StayMapper;
import com.project.backend.accommodation.service.StayService;
import com.project.backend.accommodation.vo.StayVO;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.core.vo.ApprovalProgress;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.common.service.ApprovalProgressBuilder;
import com.project.backend.common.service.StudentInfoEnricher;
import com.project.core.util.EntityUtils;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.mapper.CampusMapper;
import com.project.backend.approval.service.ApprovalService;
import com.project.backend.approval.mapper.ApprovalInstanceMapper;
import com.project.backend.approval.mapper.ApprovalRecordMapper;
import com.project.backend.approval.entity.ApprovalRecord;
import com.project.backend.util.DictUtils;
import com.project.core.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 留宿管理Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StayServiceImpl extends ServiceImpl<StayMapper, Stay> implements StayService {

    private final StudentMapper studentMapper;
    private final CampusMapper campusMapper;
    private final StudentInfoEnricher studentInfoEnricher;
    private final ApprovalService approvalService;
    private final ApprovalInstanceMapper approvalInstanceMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final ObjectMapper objectMapper;
    private final ApprovalProgressBuilder approvalProgressBuilder;

    @Override
    public PageResult<StayVO> pageList(StayQueryDTO queryDTO) {
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getStudentNo()), Stay::getStudentNo, queryDTO.getStudentNo())
               .like(StrUtil.isNotBlank(queryDTO.getStudentName()), Stay::getStudentName, queryDTO.getStudentName())
               .eq(queryDTO.getStudentId() != null, Stay::getStudentId, queryDTO.getStudentId())
               .eq(StrUtil.isNotBlank(queryDTO.getCampusCode()), Stay::getCampusCode, queryDTO.getCampusCode())
               .eq(queryDTO.getBedId() != null, Stay::getBedId, queryDTO.getBedId())
               .eq(queryDTO.getStatus() != null, Stay::getStatus, queryDTO.getStatus())
               .ge(queryDTO.getApplyDateStart() != null, Stay::getApplyDate, queryDTO.getApplyDateStart())
               .le(queryDTO.getApplyDateEnd() != null, Stay::getApplyDate, queryDTO.getApplyDateEnd())
               .ge(queryDTO.getStayStartDateStart() != null, Stay::getStayStartDate, queryDTO.getStayStartDateStart())
               .le(queryDTO.getStayStartDateEnd() != null, Stay::getStayStartDate, queryDTO.getStayStartDateEnd())
               .orderByDesc(Stay::getCreateTime);

        Page<Stay> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<StayVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public StayVO getDetailById(Long id) {
        Stay stay = EntityUtils.requireNonNull(getById(id), "留宿记录");
        return convertToVO(stay);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveStay(StaySaveDTO saveDTO) {
        // 获取学生ID：优先使用 DTO 中的值（管理端编辑），否则从 UserContext 获取（学生端提交）
        Long studentId = saveDTO.getStudentId();
        if (studentId == null) {
            studentId = UserContext.getUserId();
            if (studentId == null) {
                throw new BusinessException("用户未登录");
            }
            saveDTO.setStudentId(studentId);
        }

        // 检查学生是否存在
        Student student = EntityUtils.requireNonNull(studentMapper.selectById(studentId), "学生");

        Stay stay = new Stay();
        BeanUtil.copyProperties(saveDTO, stay, "signature", "images");

        // 转换签名和附件数组为 JSON 字符串
        try {
            if (saveDTO.getSignature() != null && !saveDTO.getSignature().isEmpty()) {
                stay.setSignature(objectMapper.writeValueAsString(saveDTO.getSignature()));
            }
            if (saveDTO.getImages() != null && !saveDTO.getImages().isEmpty()) {
                stay.setImages(objectMapper.writeValueAsString(saveDTO.getImages()));
            }
        } catch (JsonProcessingException e) {
            log.error("转换签名/附件为JSON失败", e);
            throw new BusinessException("保存签名/附件失败");
        }

        // 填充学生冗余字段
        stay.setStudentName(student.getStudentName());
        stay.setStudentNo(student.getStudentNo());

        // 调试日志：打印家长信息
        log.info("保存留宿申请 - 家长信息: parentName={}, parentPhone={}, parentAgree={}, signature={}, images={}", 
                stay.getParentName(), stay.getParentPhone(), stay.getParentAgree(), 
                stay.getSignature(), stay.getImages());

        boolean isNew = saveDTO.getId() == null;

        if (isNew) {
            // 新增时先保存记录（需要获取ID）
            if (stay.getStatus() == null) {
                stay.setStatus(1); // 临时状态，后续会根据审批结果更新
            }
            save(stay);

            // 发起审批流程
            Long instanceId = approvalService.startApproval(
                "stay",
                stay.getId(),
                saveDTO.getStudentId(),
                student.getStudentName()
            );

            if (instanceId != null) {
                // 有审批流程，状态设为"待审批"（状态1）
                stay.setApprovalInstanceId(instanceId);
                stay.setStatus(1);
                log.info("留宿申请已发起审批，申请ID：{}，审批实例ID：{}", stay.getId(), instanceId);
            } else {
                // 无审批流程，直接通过，状态设为"已通过"（状态2）
                stay.setStatus(2);
                log.info("留宿申请无需审批，直接通过，申请ID：{}", stay.getId());
            }

            return updateById(stay);
        } else {
            return updateById(stay);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteStay(Long id) {
        if (id == null) {
            throw new BusinessException("留宿记录ID不能为空");
        }

        // 查询留宿记录
        Stay stay = EntityUtils.requireNonNull(getById(id), "留宿记录");

        // 如果存在审批实例，删除关联的审批记录和审批实例
        if (stay.getApprovalInstanceId() != null) {
            Long instanceId = stay.getApprovalInstanceId();

            // 删除审批记录
            LambdaQueryWrapper<ApprovalRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(ApprovalRecord::getInstanceId, instanceId);
            approvalRecordMapper.delete(recordWrapper);

            // 删除审批实例
            approvalInstanceMapper.deleteById(instanceId);

            log.info("删除留宿记录时同步删除审批实例，留宿记录ID：{}，审批实例ID：{}", id, instanceId);
        }

        // 删除留宿记录
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("留宿记录ID不能为空");
        }

        // 查询所有留宿记录
        List<Stay> stayList = listByIds(Arrays.asList(ids));
        if (stayList.isEmpty()) {
            throw new BusinessException("留宿记录不存在");
        }

        // 收集所有需要删除的审批实例ID
        List<Long> instanceIds = stayList.stream()
                .map(Stay::getApprovalInstanceId)
                .filter(instanceId -> instanceId != null)
                .distinct()
                .collect(Collectors.toList());

        // 批量删除审批记录和审批实例
        if (!instanceIds.isEmpty()) {
            // 删除审批记录
            LambdaQueryWrapper<ApprovalRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.in(ApprovalRecord::getInstanceId, instanceIds);
            approvalRecordMapper.delete(recordWrapper);

            // 删除审批实例（循环删除，因为 BaseMapper 没有批量删除方法）
            for (Long instanceId : instanceIds) {
                approvalInstanceMapper.deleteById(instanceId);
            }

            log.info("批量删除留宿记录时同步删除审批实例，留宿记录数量：{}，审批实例数量：{}",
                    stayList.size(), instanceIds.size());
        }

        // 批量删除留宿记录
        return removeByIds(Arrays.asList(ids));
    }

    /**
     * 撤回留宿申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelStay(Long id) {
        if (id == null) {
            throw new BusinessException("留宿记录ID不能为空");
        }

        Stay stay = EntityUtils.requireNonNull(getById(id), "留宿记录");

        // 只有待审核状态才能撤回
        if (stay.getStatus() != 1) {
            throw new BusinessException("只有待审核状态的申请才能撤回");
        }

        // 更新状态为已撤回（状态5）
        stay.setStatus(5);
        return updateById(stay);
    }

    /**
     * 实体转VO
     */
    private StayVO convertToVO(Stay stay) {
        StayVO vo = new StayVO();
        BeanUtil.copyProperties(stay, vo);
        vo.setStatusText(DictUtils.getLabel("stay_status", stay.getStatus(), "未知"));

        // 填充学生详细信息（studentInfo 中含 campusName 等）
        if (stay.getStudentId() != null) {
            Student student = studentMapper.selectById(stay.getStudentId());
            if (student != null) {
                studentInfoEnricher.enrichStudentInfo(student, vo, "campusName");
            }
        }

        // 转换家长是否同意文本
        if (StrUtil.isNotBlank(stay.getParentAgree())) {
            vo.setParentAgreeText(DictUtils.getLabel("parent_agree_status", stay.getParentAgree(), "未填写"));
        }

        // 填充审批进度信息
        if (stay.getApprovalInstanceId() != null) {
            vo.setApprovalInstanceId(stay.getApprovalInstanceId());
            vo.setApprovalProgress(approvalProgressBuilder.buildProgress(stay.getApprovalInstanceId(), stay.getStatus(), "stay_status"));
        }

        return vo;
    }

    /**
     * 构建审批进度信息
     */
}
