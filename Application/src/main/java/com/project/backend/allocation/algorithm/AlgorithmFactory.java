package com.project.backend.allocation.algorithm;

import com.project.backend.allocation.vo.AlgorithmInfoVO;
import com.project.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分配算法工厂
 * 提供算法查询和获取功能
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlgorithmFactory {

    private final List<AllocationAlgorithm> algorithms;

    /**
     * 根据类型获取算法
     *
     * @param type 算法类型
     * @return 算法实例
     */
    public AllocationAlgorithm getAlgorithm(String type) {
        return algorithms.stream()
                .filter(a -> a.getAlgorithmType().equals(type))
                .findFirst()
                .orElseThrow(() -> new BusinessException("不支持的算法类型: " + type));
    }

    /**
     * 获取所有可用算法信息
     *
     * @return 算法信息列表
     */
    public List<AlgorithmInfoVO> getAllAlgorithms() {
        return algorithms.stream()
                .map(a -> AlgorithmInfoVO.builder()
                        .type(a.getAlgorithmType())
                        .name(a.getAlgorithmName())
                        .description(a.getDescription())
                        .advantages(a.getAdvantages())
                        .disadvantages(a.getDisadvantages())
                        .estimatedTime(a.getEstimatedTime(10000)) // 以1万人为例
                        .recommended(a.isRecommended())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 获取推荐算法
     *
     * @return 推荐的算法
     */
    public AllocationAlgorithm getRecommendedAlgorithm() {
        return algorithms.stream()
                .filter(AllocationAlgorithm::isRecommended)
                .findFirst()
                .orElse(algorithms.get(0));
    }

    /**
     * 获取指定学生数量的预估时间
     *
     * @param type         算法类型
     * @param studentCount 学生数量
     * @return 预估时间
     */
    public String getEstimatedTime(String type, int studentCount) {
        AllocationAlgorithm algorithm = getAlgorithm(type);
        return algorithm.getEstimatedTime(studentCount);
    }
}
