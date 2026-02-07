package com.project.backend.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.approval.entity.ApprovalInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 审批实例Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Mapper
public interface ApprovalInstanceMapper extends BaseMapper<ApprovalInstance> {

    /**
     * 根据业务类型和业务ID查询审批实例
     *
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 审批实例
     */
    @Select("SELECT id, flow_id, flow_name, business_type, business_id, applicant_id, applicant_name, current_node_id, current_node_name, status, create_time, update_time, create_by, update_by FROM sys_approval_instance WHERE business_type = #{businessType} AND business_id = #{businessId} AND deleted = 0")
    ApprovalInstance selectByBusiness(@Param("businessType") String businessType, @Param("businessId") Long businessId);

    /**
     * 根据申请人ID查询审批实例列表
     *
     * @param applicantId 申请人ID
     * @return 审批实例列表
     */
    @Select("SELECT id, flow_id, flow_name, business_type, business_id, applicant_id, applicant_name, current_node_id, current_node_name, status, create_time, update_time, create_by, update_by FROM sys_approval_instance WHERE applicant_id = #{applicantId} AND deleted = 0 ORDER BY create_time DESC")
    List<ApprovalInstance> selectByApplicantId(Long applicantId);

    /**
     * 根据当前节点ID列表查询待审批的实例
     *
     * @param nodeIds 节点ID列表
     * @return 审批实例列表
     */
    List<ApprovalInstance> selectPendingByNodeIds(@Param("nodeIds") List<Long> nodeIds);

    /**
     * 查询进行中的实例数量（按业务类型）
     *
     * @param businessType 业务类型
     * @return 实例数量
     */
    @Select("SELECT COUNT(*) FROM sys_approval_instance WHERE business_type = #{businessType} AND status = 1 AND deleted = 0")
    int countPendingByBusinessType(String businessType);

    /**
     * 按业务类型分组统计待审批数量（单次查询消除 N+1）
     *
     * @param nodeIds 当前用户可审批的节点ID列表
     * @return 每行包含 business_type 和 cnt
     */
    @Select("<script>" +
            "SELECT business_type, COUNT(*) AS cnt " +
            "FROM sys_approval_instance " +
            "WHERE status = 1 AND deleted = 0 " +
            "AND current_node_id IN " +
            "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> " +
            "GROUP BY business_type" +
            "</script>")
    List<Map<String, Object>> countPendingGroupByBusinessType(@Param("nodeIds") Collection<Long> nodeIds);
}
