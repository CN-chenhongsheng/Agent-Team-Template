package com.project.backend.allocation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.allocation.dto.task.AllocationTaskQueryDTO;
import com.project.backend.allocation.dto.task.AllocationTaskSaveDTO;
import com.project.backend.allocation.entity.AllocationTask;
import com.project.backend.allocation.vo.AllocationPreviewVO;
import com.project.backend.allocation.vo.AllocationProgressVO;
import com.project.backend.allocation.vo.AllocationTaskVO;
import com.project.core.result.PageResult;

/**
 * 分配任务服务接口
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
public interface AllocationTaskService extends IService<AllocationTask> {

    /**
     * 分页查询任务列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AllocationTaskVO> pageList(AllocationTaskQueryDTO queryDTO);

    /**
     * 获取任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    AllocationTaskVO getDetailById(Long id);

    /**
     * 保存任务（新增或编辑）
     *
     * @param saveDTO 保存DTO
     * @return 是否成功
     */
    boolean saveTask(AllocationTaskSaveDTO saveDTO);

    /**
     * 删除任务
     *
     * @param id 任务ID
     * @return 是否成功
     */
    boolean deleteTask(Long id);

    /**
     * 预览分配（不执行，仅统计）
     *
     * @param saveDTO 任务配置
     * @return 预览结果
     */
    AllocationPreviewVO previewTask(AllocationTaskSaveDTO saveDTO);

    /**
     * 执行分配任务
     *
     * @param taskId 任务ID
     */
    void executeTask(Long taskId);

    /**
     * 获取执行进度
     *
     * @param taskId 任务ID
     * @return 进度信息
     */
    AllocationProgressVO getTaskProgress(Long taskId);

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean cancelTask(Long taskId);
}
