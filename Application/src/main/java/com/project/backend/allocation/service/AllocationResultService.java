package com.project.backend.allocation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.allocation.dto.result.AllocationResultQueryDTO;
import com.project.backend.allocation.entity.AllocationResult;
import com.project.backend.allocation.vo.AllocationResultVO;
import com.project.backend.allocation.vo.AllocationRoommateVO;
import com.project.core.result.PageResult;

import java.util.List;

/**
 * 分配结果服务接口
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
public interface AllocationResultService extends IService<AllocationResult> {

    /**
     * 分页查询分配结果
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AllocationResultVO> pageList(AllocationResultQueryDTO queryDTO);

    /**
     * 获取分配结果详情
     *
     * @param id 结果ID
     * @return 结果详情
     */
    AllocationResultVO getDetailById(Long id);

    /**
     * 确认分配结果
     *
     * @param taskId 任务ID
     * @param resultIds 结果ID列表
     * @return 是否成功
     */
    boolean confirmResults(Long taskId, List<Long> resultIds);

    /**
     * 确认全部分配结果
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean confirmAll(Long taskId);

    /**
     * 拒绝分配结果
     *
     * @param taskId 任务ID
     * @param resultIds 结果ID列表
     * @param reason 拒绝原因
     * @return 是否成功
     */
    boolean rejectResults(Long taskId, List<Long> resultIds, String reason);

    /**
     * 调整分配结果
     *
     * @param resultId 结果ID
     * @param newBedId 新床位ID
     * @param reason 调整原因
     * @return 是否成功
     */
    boolean adjustResult(Long resultId, Long newBedId, String reason);

    /**
     * 获取问题清单（低匹配度）
     *
     * @param taskId 任务ID
     * @param threshold 分数阈值
     * @return 问题结果列表
     */
    List<AllocationResultVO> getProblemList(Long taskId, Integer threshold);

    /**
     * 根据学生ID获取最新已确认的分配结果
     *
     * @param studentId 学生ID
     * @return 分配结果
     */
    AllocationResultVO getMyResult(Long studentId);

    /**
     * 获取室友信息
     *
     * @param studentId 学生ID
     * @return 室友列表
     */
    List<AllocationRoommateVO> getRoommates(Long studentId);
}
