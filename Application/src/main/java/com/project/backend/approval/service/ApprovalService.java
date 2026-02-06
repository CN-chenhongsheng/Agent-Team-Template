package com.project.backend.approval.service;

import com.project.backend.approval.dto.ApprovalActionDTO;
import com.project.backend.approval.dto.ApprovalInstanceQueryDTO;
import com.project.backend.approval.dto.ApprovalRecordQueryDTO;
import com.project.backend.approval.vo.ApprovalInstanceVO;
import com.project.backend.approval.vo.ApprovalRecordVO;
import com.project.core.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 审批核心Service
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
public interface ApprovalService {

    /**
     * 发起审批（创建审批实例）
     * 
     * @param businessType 业务类型
     * @param businessId 业务数据ID
     * @param applicantId 申请人ID
     * @param applicantName 申请人姓名
     * @return 审批实例ID，如果该业务类型未配置审批流程则返回null（表示不需要审批，直接通过）
     */
    Long startApproval(String businessType, Long businessId, Long applicantId, String applicantName);

    /**
     * 检查业务类型是否需要审批
     * 
     * @param businessType 业务类型
     * @return true需要审批，false不需要审批（直接通过）
     */
    boolean requiresApproval(String businessType);

    /**
     * 执行审批操作
     * 
     * @param actionDTO 审批操作DTO
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @return 是否成功
     */
    boolean doApprove(ApprovalActionDTO actionDTO, Long approverId, String approverName);

    /**
     * 撤回审批
     * 
     * @param instanceId 审批实例ID
     * @param applicantId 申请人ID（用于验证）
     * @return 是否成功
     */
    boolean withdraw(Long instanceId, Long applicantId);

    /**
     * 获取审批实例详情
     * 
     * @param instanceId 审批实例ID
     * @return 审批实例详情
     */
    ApprovalInstanceVO getInstanceDetail(Long instanceId);

    /**
     * 根据业务查询审批实例详情
     * 
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 审批实例详情
     */
    ApprovalInstanceVO getInstanceByBusiness(String businessType, Long businessId);

    /**
     * 分页查询待办审批列表
     * 
     * @param queryDTO 查询条件
     * @param userId 当前用户ID
     * @param roleIds 当前用户角色ID列表
     * @return 审批实例分页列表
     */
    PageResult<ApprovalInstanceVO> pagePendingList(ApprovalInstanceQueryDTO queryDTO, Long userId, List<Long> roleIds);

    /**
     * 分页查询审批记录列表
     * 
     * @param queryDTO 查询条件
     * @param approverId 审批人ID（可选，为null则查询所有）
     * @return 审批记录分页列表
     */
    PageResult<ApprovalRecordVO> pageRecordList(ApprovalRecordQueryDTO queryDTO, Long approverId);

    /**
     * 获取审批记录列表（按实例）
     * 
     * @param instanceId 审批实例ID
     * @return 审批记录列表
     */
    List<ApprovalRecordVO> getRecordsByInstance(Long instanceId);

    /**
     * 检查用户是否可以审批
     * 
     * @param instanceId 审批实例ID
     * @param userId 用户ID
     * @param roleIds 用户角色ID列表
     * @return 是否可以审批
     */
    boolean canApprove(Long instanceId, Long userId, List<Long> roleIds);

    /**
     * 获取待办审批数量统计
     * 
     * @param userId 用户ID
     * @param roleIds 用户角色ID列表
     * @return 按业务类型分组的待办数量
     */
    Map<String, Long> getPendingCount(Long userId, List<Long> roleIds);
}
