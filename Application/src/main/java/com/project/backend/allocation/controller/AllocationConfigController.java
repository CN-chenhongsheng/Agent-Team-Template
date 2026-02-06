package com.project.backend.allocation.controller;

import com.project.backend.allocation.dto.config.AllocationConfigQueryDTO;
import com.project.backend.allocation.dto.config.AllocationConfigSaveDTO;
import com.project.backend.allocation.service.AllocationConfigService;
import com.project.backend.allocation.vo.AllocationConfigVO;
import com.project.backend.allocation.vo.AlgorithmInfoVO;
import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import com.project.backend.controller.base.StatusUpdateController;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分配配置管理Controller
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/allocation/config")
@RequiredArgsConstructor
@Tag(name = "分配配置管理", description = "智能分配规则配置的增删改查")
public class AllocationConfigController
        extends BaseCrudController<AllocationConfigVO, AllocationConfigQueryDTO, AllocationConfigSaveDTO>
        implements BatchDeleteController, StatusUpdateController {

    private final AllocationConfigService configService;

    @Override
    public String getEntityName() {
        return "分配配置";
    }

    @Override
    protected PageResult<AllocationConfigVO> callPageList(AllocationConfigQueryDTO queryDTO) {
        return configService.pageList(queryDTO);
    }

    @Override
    protected AllocationConfigVO callGetDetailById(Long id) {
        return configService.getDetailById(id);
    }

    @Override
    protected boolean callSave(AllocationConfigSaveDTO saveDTO) {
        return configService.saveConfig(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return configService.deleteConfig(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return configService.batchDelete(ids);
    }

    @Override
    public boolean callUpdateStatus(Long id, Integer status) {
        return configService.updateStatus(id, status);
    }

    // ==================== 额外的业务方法 ====================

    /**
     * 复制配置
     */
    @PostMapping("/{id}/copy")
    @Operation(summary = "复制配置")
    @Log(title = "复制分配配置", businessType = 1)
    public R<Long> copy(@PathVariable Long id, @RequestParam String newName) {
        Long newId = configService.copyConfig(id, newName);
        return R.ok("配置复制成功", newId);
    }

    /**
     * 获取默认配置模板
     */
    @GetMapping("/default-template")
    @Operation(summary = "获取默认配置模板")
    public R<AllocationConfigVO> getDefaultTemplate() {
        AllocationConfigVO template = configService.getDefaultTemplate();
        return R.ok(template);
    }

    /**
     * 获取可用算法列表
     */
    @GetMapping("/algorithms")
    @Operation(summary = "获取可用算法列表")
    public R<List<AlgorithmInfoVO>> getAlgorithms() {
        List<AlgorithmInfoVO> algorithms = configService.getAlgorithms();
        return R.ok(algorithms);
    }
}
