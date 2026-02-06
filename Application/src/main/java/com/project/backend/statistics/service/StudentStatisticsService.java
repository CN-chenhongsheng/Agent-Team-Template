package com.project.backend.statistics.service;

import com.project.backend.statistics.dto.MyApplyQueryDTO;
import com.project.backend.statistics.vo.ApplyDetailVO;
import com.project.backend.statistics.vo.MyApplyVO;
import com.project.backend.statistics.vo.StudentHomeStatisticsVO;
import com.project.core.result.PageResult;

import java.util.List;

/**
 * 学生统计服务接口
 * 提供学生首页统计和申请记录查询功能
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
public interface StudentStatisticsService {

    /**
     * 获取学生首页统计信息
     *
     * @param studentId 学生ID
     * @return 学生首页统计信息
     */
    StudentHomeStatisticsVO getStudentHomeStatistics(Long studentId);

    /**
     * 获取我的申请列表
     *
     * @param queryDTO 查询条件
     * @return 申请列表（分页或限制条数）
     */
    PageResult<MyApplyVO> getMyApplies(MyApplyQueryDTO queryDTO);

    /**
     * 获取申请详情
     *
     * @param studentId 学生ID
     * @param applyId 申请ID
     * @param applyType 申请类型（check_in/transfer/check_out/stay）
     * @return 申请详情
     */
    ApplyDetailVO getApplyDetail(Long studentId, Long applyId, String applyType);
}
