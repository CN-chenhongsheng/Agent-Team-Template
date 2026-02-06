package com.project.backend.accommodation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.accommodation.dto.checkout.CheckOutQueryDTO;
import com.project.backend.accommodation.dto.checkout.CheckOutSaveDTO;
import com.project.backend.accommodation.entity.CheckOut;
import com.project.backend.accommodation.mapper.CheckOutMapper;
import com.project.backend.accommodation.service.CheckOutService;
import com.project.backend.accommodation.vo.CheckOutVO;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.core.vo.ApprovalProgress;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.common.service.ApprovalProgressBuilder;
import com.project.backend.common.service.StudentInfoEnricher;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.mapper.CampusMapper;
import com.project.backend.approval.service.ApprovalService;
import com.project.backend.approval.mapper.ApprovalInstanceMapper;
import com.project.backend.approval.mapper.ApprovalRecordMapper;
import com.project.backend.approval.entity.ApprovalRecord;
import com.project.backend.util.DictUtils;
import com.project.core.context.UserContext;
import com.project.core.util.EntityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退宿管理Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckOutServiceImpl extends ServiceImpl<CheckOutMapper, CheckOut> implements CheckOutService {

    private final StudentMapper studentMapper;
    private final CampusMapper campusMapper;
    private final StudentInfoEnricher studentInfoEnricher;
    private final ApprovalService approvalService;
    private final ApprovalInstanceMapper approvalInstanceMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final ApprovalProgressBuilder approvalProgressBuilder;

    @Override
    public PageResult<CheckOutVO> pageList(CheckOutQueryDTO queryDTO) {
        LambdaQueryWrapper<CheckOut> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getStudentNo()), CheckOut::getStudentNo, queryDTO.getStudentNo())
               .like(StrUtil.isNotBlank(queryDTO.getStudentName()), CheckOut::getStudentName, queryDTO.getStudentName())
               .eq(queryDTO.getStudentId() != null, CheckOut::getStudentId, queryDTO.getStudentId())
               .eq(StrUtil.isNotBlank(queryDTO.getCampusCode()), CheckOut::getCampusCode, queryDTO.getCampusCode())
               .eq(queryDTO.getBedId() != null, CheckOut::getBedId, queryDTO.getBedId())
               .eq(queryDTO.getStatus() != null, CheckOut::getStatus, queryDTO.getStatus())
               .ge(queryDTO.getApplyDateStart() != null, CheckOut::getApplyDate, queryDTO.getApplyDateStart())
               .le(queryDTO.getApplyDateEnd() != null, CheckOut::getApplyDate, queryDTO.getApplyDateEnd())
               .orderByDesc(CheckOut::getCreateTime);

        Page<CheckOut> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<CheckOutVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public CheckOutVO getDetailById(Long id) {
        CheckOut checkOut = EntityUtils.requireNonNull(getById(id), "退宿记录");
        return convertToVO(checkOut);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCheckOut(CheckOutSaveDTO saveDTO) {
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

        CheckOut checkOut = new CheckOut();
        BeanUtil.copyProperties(saveDTO, checkOut);

        // 填充学生冗余字段
        checkOut.setStudentName(student.getStudentName());
        checkOut.setStudentNo(student.getStudentNo());

        boolean isNew = saveDTO.getId() == null;

        if (isNew) {
            // 新增时先保存记录（需要获取ID）
            if (checkOut.getStatus() == null) {
                checkOut.setStatus(1); // 临时状态，后续会根据审批结果更新
            }
            save(checkOut);

            // 发起审批流程
            Long instanceId = approvalService.startApproval(
                "check_out",
                checkOut.getId(),
                saveDTO.getStudentId(),
                student.getStudentName()
            );

            if (instanceId != null) {
                // 有审批流程，状态设为"待审批"（状态1）
                checkOut.setApprovalInstanceId(instanceId);
                checkOut.setStatus(1);
                log.info("退宿申请已发起审批，申请ID：{}，审批实例ID：{}", checkOut.getId(), instanceId);
            } else {
                // 无审批流程，直接通过，状态设为"已通过"（状态2）
                checkOut.setStatus(2);
                log.info("退宿申请无需审批，直接通过，申请ID：{}", checkOut.getId());
            }

            return updateById(checkOut);
        } else {
            return updateById(checkOut);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCheckOut(Long id) {
        if (id == null) {
            throw new BusinessException("退宿记录ID不能为空");
        }

        // 查询退宿记录
        CheckOut checkOut = EntityUtils.requireNonNull(getById(id), "退宿记录");

        // 如果存在审批实例，删除关联的审批记录和审批实例
        if (checkOut.getApprovalInstanceId() != null) {
            Long instanceId = checkOut.getApprovalInstanceId();

            // 删除审批记录
            LambdaQueryWrapper<ApprovalRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(ApprovalRecord::getInstanceId, instanceId);
            approvalRecordMapper.delete(recordWrapper);

            // 删除审批实例
            approvalInstanceMapper.deleteById(instanceId);

            log.info("删除退宿记录时同步删除审批实例，退宿记录ID：{}，审批实例ID：{}", id, instanceId);
        }

        // 删除退宿记录
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("退宿记录ID不能为空");
        }

        // 查询所有退宿记录
        List<CheckOut> checkOutList = listByIds(Arrays.asList(ids));
        if (checkOutList.isEmpty()) {
            throw new BusinessException("退宿记录不存在");
        }

        // 收集所有需要删除的审批实例ID
        List<Long> instanceIds = checkOutList.stream()
                .map(CheckOut::getApprovalInstanceId)
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

            log.info("批量删除退宿记录时同步删除审批实例，退宿记录数量：{}，审批实例数量：{}",
                    checkOutList.size(), instanceIds.size());
        }

        // 批量删除退宿记录
        return removeByIds(Arrays.asList(ids));
    }

    /**
     * 撤回退宿申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelCheckOut(Long id) {
        if (id == null) {
            throw new BusinessException("退宿记录ID不能为空");
        }

        CheckOut checkOut = EntityUtils.requireNonNull(getById(id), "退宿记录");

        // 只有待审核状态才能撤回
        if (checkOut.getStatus() != 1) {
            throw new BusinessException("只有待审核状态的申请才能撤回");
        }

        // 更新状态为已撤回（状态5）
        checkOut.setStatus(5);
        return updateById(checkOut);
    }

    /**
     * 实体转VO
     */
    private CheckOutVO convertToVO(CheckOut checkOut) {
        CheckOutVO vo = new CheckOutVO();
        BeanUtil.copyProperties(checkOut, vo);
        vo.setStatusText(DictUtils.getLabel("check_out_status", checkOut.getStatus(), "未知"));

        // 填充学生详细信息（studentInfo 中含 campusName 等）
        if (checkOut.getStudentId() != null) {
            Student student = studentMapper.selectById(checkOut.getStudentId());
            if (student != null) {
                studentInfoEnricher.enrichStudentInfo(student, vo, "campusName");
            }
        }

        // 填充审批进度信息
        if (checkOut.getApprovalInstanceId() != null) {
            vo.setApprovalInstanceId(checkOut.getApprovalInstanceId());
            vo.setApprovalProgress(approvalProgressBuilder.buildProgress(checkOut.getApprovalInstanceId(), checkOut.getStatus(), "check_out_status"));
        }

        return vo;
    }

    /**
     * 构建审批进度信息
     */
}
