package com.project.backend.allocation.service;

import com.project.backend.allocation.vo.BedRecommendVO;

import java.util.List;

/**
 * 床位推荐服务接口
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
public interface BedRecommendService {

    /**
     * 为学生推荐床位
     *
     * @param studentId 学生ID
     * @param limit     返回数量限制
     * @return 推荐床位列表（按匹配度排序）
     */
    List<BedRecommendVO> recommendBeds(Long studentId, int limit);

    /**
     * 调宿推荐（为已有床位的学生推荐更好的房间）
     *
     * @param studentId 学生ID
     * @param limit     返回数量限制
     * @return 推荐床位列表
     */
    List<BedRecommendVO> recommendTransfer(Long studentId, int limit);
}
