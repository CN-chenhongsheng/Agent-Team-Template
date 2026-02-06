package com.project.backend.allocation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.allocation.entity.AllocationResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 分配结果Mapper
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Mapper
public interface AllocationResultMapper extends BaseMapper<AllocationResult> {

    /**
     * 统计任务的平均匹配分
     *
     * @param taskId 任务ID
     * @return 平均匹配分
     */
    @Select("SELECT AVG(match_score) FROM sys_allocation_result WHERE task_id = #{taskId}")
    BigDecimal calculateAvgMatchScore(@Param("taskId") Long taskId);

    /**
     * 统计任务的最低匹配分
     *
     * @param taskId 任务ID
     * @return 最低匹配分
     */
    @Select("SELECT MIN(match_score) FROM sys_allocation_result WHERE task_id = #{taskId}")
    BigDecimal calculateMinMatchScore(@Param("taskId") Long taskId);

    /**
     * 统计任务的最高匹配分
     *
     * @param taskId 任务ID
     * @return 最高匹配分
     */
    @Select("SELECT MAX(match_score) FROM sys_allocation_result WHERE task_id = #{taskId}")
    BigDecimal calculateMaxMatchScore(@Param("taskId") Long taskId);

    /**
     * 统计低于阈值的数量
     *
     * @param taskId    任务ID
     * @param threshold 阈值
     * @return 低于阈值的数量
     */
    @Select("SELECT COUNT(*) FROM sys_allocation_result WHERE task_id = #{taskId} AND match_score < #{threshold}")
    int countLowScoreResults(@Param("taskId") Long taskId, @Param("threshold") BigDecimal threshold);

    /**
     * 统计指定状态的数量
     *
     * @param taskId 任务ID
     * @param status 状态
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_allocation_result WHERE task_id = #{taskId} AND status = #{status}")
    int countByStatus(@Param("taskId") Long taskId, @Param("status") Integer status);
}
