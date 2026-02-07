package com.project.backend.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.approval.entity.ApprovalNodeAssignee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审批节点审批人Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Mapper
public interface ApprovalNodeAssigneeMapper extends BaseMapper<ApprovalNodeAssignee> {

    /**
     * 根据节点ID查询审批人列表
     *
     * @param nodeId 节点ID
     * @return 审批人列表
     */
    @Select("SELECT id, node_id, assignee_type, assignee_id, sort_order, create_time, update_time, create_by, update_by FROM sys_approval_node_assignee WHERE node_id = #{nodeId} AND deleted = 0 ORDER BY sort_order ASC")
    List<ApprovalNodeAssignee> selectByNodeId(Long nodeId);

    /**
     * 删除节点下的所有审批人（软删除）
     *
     * @param nodeId 节点ID
     * @return 删除数量
     */
    @Update("UPDATE sys_approval_node_assignee SET deleted = 1, update_time = NOW() WHERE node_id = #{nodeId} AND deleted = 0")
    int deleteByNodeId(Long nodeId);

    /**
     * 查询指定角色ID能审批的节点ID列表
     *
     * @param roleId 角色ID
     * @return 节点ID列表
     */
    @Select("SELECT node_id FROM sys_approval_node_assignee WHERE assignee_type = 1 AND assignee_id = #{roleId} AND deleted = 0")
    List<Long> selectNodeIdsByRoleId(Long roleId);

    /**
     * 查询指定用户ID能审批的节点ID列表
     *
     * @param userId 用户ID
     * @return 节点ID列表
     */
    @Select("SELECT node_id FROM sys_approval_node_assignee WHERE assignee_type = 2 AND assignee_id = #{userId} AND deleted = 0")
    List<Long> selectNodeIdsByUserId(Long userId);

    /**
     * 批量查询指定角色ID列表能审批的节点ID列表（消除 N+1）
     *
     * @param roleIds 角色ID列表
     * @return 节点ID列表
     */
    @Select("<script>SELECT DISTINCT node_id FROM sys_approval_node_assignee WHERE assignee_type = 1 AND deleted = 0 AND assignee_id IN <foreach collection='roleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Long> selectNodeIdsByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 批量删除多个节点下的审批人（消除 N+1）
     *
     * @param nodeIds 节点ID列表
     * @return 删除数量
     */
    @Update("<script>UPDATE sys_approval_node_assignee SET deleted = 1, update_time = NOW() WHERE deleted = 0 AND node_id IN <foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int deleteByNodeIds(@Param("nodeIds") List<Long> nodeIds);
}
