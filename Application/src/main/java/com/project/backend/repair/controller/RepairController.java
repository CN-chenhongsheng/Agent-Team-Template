package com.project.backend.repair.controller;

import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import com.project.backend.repair.dto.RepairQueryDTO;
import com.project.backend.repair.dto.RepairSaveDTO;
import com.project.backend.repair.service.RepairService;
import com.project.backend.repair.vo.RepairVO;
import com.project.core.annotation.Log;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 报修管理Controller（管理端）
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@RestController
@RequestMapping("/v1/system/repair")
@RequiredArgsConstructor
@Tag(name = "报修管理", description = "报修管理相关接口")
public class RepairController extends BaseCrudController<RepairVO, RepairQueryDTO, RepairSaveDTO>
        implements BatchDeleteController {

    private final RepairService repairService;

    @Override
    public String getEntityName() {
        return "报修";
    }

    @Override
    protected PageResult<RepairVO> callPageList(RepairQueryDTO queryDTO) {
        return repairService.pageList(queryDTO);
    }

    @Override
    protected RepairVO callGetDetailById(Long id) {
        return repairService.getDetailById(id);
    }

    @Override
    protected boolean callSave(RepairSaveDTO saveDTO) {
        return repairService.saveRepair(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return repairService.deleteRepair(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return repairService.batchDelete(ids);
    }

    /**
     * 接单
     */
    @Operation(summary = "接单")
    @Log(title = "接单报修", businessType = 2)
    @PutMapping("/{id}/accept")
    public R<Void> accept(@PathVariable Long id) {
        boolean success = repairService.acceptRepair(id);
        return success ? R.ok() : R.fail("接单失败");
    }

    /**
     * 完成维修
     */
    @Operation(summary = "完成维修")
    @Log(title = "完成报修", businessType = 2)
    @PutMapping("/{id}/complete")
    public R<Void> complete(
            @PathVariable Long id,
            @RequestBody @Valid CompleteRepairRequest request
    ) {
        boolean success = repairService.completeRepair(
                id,
                request.getRepairResult(),
                request.getRepairImages()
        );
        return success ? R.ok() : R.fail("操作失败");
    }

    /**
     * 更新报修信息
     */
    @Operation(summary = "更新报修信息")
    @Log(title = "更新报修", businessType = 2)
    @PutMapping("/{id}")
    public R<Void> update(
            @PathVariable Long id,
            @RequestBody @Valid RepairSaveDTO saveDTO
    ) {
        saveDTO.setId(id);
        boolean success = repairService.saveRepair(saveDTO);
        return success ? R.ok() : R.fail("更新失败");
    }

    /**
     * 完成维修请求
     */
    @lombok.Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "完成维修请求")
    public static class CompleteRepairRequest {
        @io.swagger.v3.oas.annotations.media.Schema(description = "维修结果描述", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        @jakarta.validation.constraints.NotBlank(message = "维修结果描述不能为空")
        private String repairResult;

        @io.swagger.v3.oas.annotations.media.Schema(description = "维修后图片（JSON数组）")
        private String repairImages;
    }
}
