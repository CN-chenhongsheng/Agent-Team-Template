package com.project.backend.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.approval.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批记录Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    /**
     * 根据审批实例ID查询审批记录列表
     *
     * @param instanceId 审批实例ID
     * @return 审批记录列表
     */
    @Select("SELECT * FROM sys_approval_record WHERE instance_id = #{instanceId} AND deleted = 0 ORDER BY approve_time ASC")
    List<ApprovalRecord> selectByInstanceId(Long instanceId);

    /**
     * 查询指定实例、节点的审批记录数量
     *
     * @param instanceId 实例ID
     * @param nodeId 节点ID
     * @return 审批记录数量
     */
    @Select("SELECT COUNT(*) FROM sys_approval_record WHERE instance_id = #{instanceId} AND node_id = #{nodeId} AND deleted = 0")
    int countByInstanceAndNode(@Param("instanceId") Long instanceId, @Param("nodeId") Long nodeId);

    /**
     * 查询指定实例、节点的通过记录数量
     *
     * @param instanceId 实例ID
     * @param nodeId 节点ID
     * @return 通过记录数量
     */
    @Select("SELECT COUNT(*) FROM sys_approval_record WHERE instance_id = #{instanceId} AND node_id = #{nodeId} AND action = 1 AND deleted = 0")
    int countApprovedByInstanceAndNode(@Param("instanceId") Long instanceId, @Param("nodeId") Long nodeId);

    /**
     * 根据审批人ID查询审批记录列表
     *
     * @param approverId 审批人ID
     * @return 审批记录列表
     */
    @Select("SELECT * FROM sys_approval_record WHERE approver_id = #{approverId} AND deleted = 0 ORDER BY approve_time DESC")
    List<ApprovalRecord> selectByApproverId(Long approverId);
}
