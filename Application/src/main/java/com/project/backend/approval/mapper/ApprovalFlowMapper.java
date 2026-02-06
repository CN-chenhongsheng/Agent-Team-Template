package com.project.backend.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.approval.entity.ApprovalFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批流程Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Mapper
public interface ApprovalFlowMapper extends BaseMapper<ApprovalFlow> {

    /**
     * 根据业务类型查询启用的流程列表
     *
     * @param businessType 业务类型
     * @return 流程列表
     */
    @Select("SELECT * FROM sys_approval_flow WHERE business_type = #{businessType} AND status = 1 AND deleted = 0 ORDER BY create_time DESC")
    List<ApprovalFlow> selectByBusinessType(String businessType);

    /**
     * 根据流程编码查询流程
     *
     * @param flowCode 流程编码
     * @return 流程
     */
    @Select("SELECT * FROM sys_approval_flow WHERE flow_code = #{flowCode} AND deleted = 0")
    ApprovalFlow selectByFlowCode(String flowCode);
}
