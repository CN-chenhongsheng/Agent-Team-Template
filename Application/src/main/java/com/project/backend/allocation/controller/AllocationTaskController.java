package com.project.backend.allocation.controller;

import com.project.backend.allocation.dto.task.AllocationTaskQueryDTO;
import com.project.backend.allocation.dto.task.AllocationTaskSaveDTO;
import com.project.backend.allocation.service.AllocationTaskService;
import com.project.backend.allocation.vo.AllocationPreviewVO;
import com.project.backend.allocation.vo.AllocationProgressVO;
import com.project.backend.allocation.vo.AllocationTaskVO;
import com.project.backend.controller.base.BaseCrudController;
import com.project.core.annotation.Log;
import com.project.core.result.PageResult;
import com.project.core.result.R;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分配任务管理Controller
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/allocation/task")
@RequiredArgsConstructor
@Tag(name = "分配任务管理", description = "智能分配任务的创建、执行、管理")
public class AllocationTaskController
        extends BaseCrudController<AllocationTaskVO, AllocationTaskQueryDTO, AllocationTaskSaveDTO> {

    private final AllocationTaskService taskService;

    @Override
    public String getEntityName() {
        return "分配任务";
    }

    @Override
    protected PageResult<AllocationTaskVO> callPageList(AllocationTaskQueryDTO queryDTO) {
        return taskService.pageList(queryDTO);
    }

    @Override
    protected AllocationTaskVO callGetDetailById(Long id) {
        return taskService.getDetailById(id);
    }

    @Override
    protected boolean callSave(AllocationTaskSaveDTO saveDTO) {
        return taskService.saveTask(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return taskService.deleteTask(id);
    }

    // ==================== 额外的业务方法 ====================

    /**
     * 预览分配（不执行）
     */
    @PostMapping("/preview")
    @Operation(summary = "预览分配（不执行）")
    public R<AllocationPreviewVO> preview(@RequestBody AllocationTaskSaveDTO saveDTO) {
        AllocationPreviewVO preview = taskService.previewTask(saveDTO);
        return R.ok(preview);
    }

    /**
     * 执行分配任务
     */
    @PutMapping("/{id}/execute")
    @Operation(summary = "执行分配任务")
    @Log(title = "执行分配任务", businessType = 2)
    public R<Void> execute(@PathVariable Long id) {
        taskService.executeTask(id);
        return R.ok("任务开始执行", null);
    }

    /**
     * 获取执行进度
     */
    @GetMapping("/{id}/progress")
    @Operation(summary = "获取执行进度")
    public R<AllocationProgressVO> getProgress(@PathVariable Long id) {
        AllocationProgressVO progress = taskService.getTaskProgress(id);
        return R.ok(progress);
    }

    /**
     * 取消任务
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消任务")
    @Log(title = "取消分配任务", businessType = 2)
    public R<Void> cancel(@PathVariable Long id) {
        boolean success = taskService.cancelTask(id);
        return success ? R.ok("任务已取消", null) : R.fail("取消失败");
    }
}
