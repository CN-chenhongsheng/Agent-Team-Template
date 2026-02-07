package com.project.backend.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.approval.entity.ApprovalNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 审批流程节点Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Mapper
public interface ApprovalNodeMapper extends BaseMapper<ApprovalNode> {

    /**
     * 根据流程ID查询节点列表（按顺序排序）
     *
     * @param flowId 流程ID
     * @return 节点列表
     */
    @Select("SELECT id, flow_id, node_name, node_order, node_type, reject_action, remark, create_time, update_time, create_by, update_by FROM sys_approval_node WHERE flow_id = #{flowId} AND deleted = 0 ORDER BY node_order ASC")
    List<ApprovalNode> selectByFlowId(Long flowId);

    /**
     * 获取流程的第一个节点
     *
     * @param flowId 流程ID
     * @return 第一个节点
     */
    @Select("SELECT id, flow_id, node_name, node_order, node_type, reject_action, remark, create_time, update_time, create_by, update_by FROM sys_approval_node WHERE flow_id = #{flowId} AND deleted = 0 ORDER BY node_order ASC LIMIT 1")
    ApprovalNode selectFirstNode(Long flowId);

    /**
     * 获取下一个节点
     *
     * @param flowId 流程ID
     * @param currentOrder 当前节点顺序
     * @return 下一个节点
     */
    @Select("SELECT id, flow_id, node_name, node_order, node_type, reject_action, remark, create_time, update_time, create_by, update_by FROM sys_approval_node WHERE flow_id = #{flowId} AND node_order > #{currentOrder} AND deleted = 0 ORDER BY node_order ASC LIMIT 1")
    ApprovalNode selectNextNode(Long flowId, Integer currentOrder);

    /**
     * 获取上一个节点
     *
     * @param flowId 流程ID
     * @param currentOrder 当前节点顺序
     * @return 上一个节点
     */
    @Select("SELECT id, flow_id, node_name, node_order, node_type, reject_action, remark, create_time, update_time, create_by, update_by FROM sys_approval_node WHERE flow_id = #{flowId} AND node_order < #{currentOrder} AND deleted = 0 ORDER BY node_order DESC LIMIT 1")
    ApprovalNode selectPrevNode(Long flowId, Integer currentOrder);

    /**
     * 删除流程下的所有节点（软删除）
     *
     * @param flowId 流程ID
     * @return 删除数量
     */
    @Update("UPDATE sys_approval_node SET deleted = 1, update_time = NOW() WHERE flow_id = #{flowId} AND deleted = 0")
    int deleteByFlowId(Long flowId);
}
