package com.project.backend.room.controller;

import com.project.core.result.PageResult;
import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import com.project.backend.controller.base.StatusUpdateController;
import com.project.core.result.R;
import com.project.backend.room.dto.bed.BedBatchCreateDTO;
import com.project.backend.room.dto.bed.BedQueryDTO;
import com.project.backend.room.dto.bed.BedSaveDTO;
import com.project.backend.room.service.BedService;
import com.project.backend.room.vo.BedVO;
import com.project.core.annotation.Log;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 床位管理控制器
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/bed")
@RequiredArgsConstructor
@Tag(name = "床位管理", description = "床位增删改查")
public class BedController extends BaseCrudController<BedVO, BedQueryDTO, BedSaveDTO>
        implements BatchDeleteController, StatusUpdateController {

    private final BedService bedService;

    @Override
    public String getEntityName() {
        return "床位";
    }

    @Override
    protected PageResult<BedVO> callPageList(BedQueryDTO queryDTO) {
        return bedService.pageList(queryDTO);
    }

    @Override
    protected BedVO callGetDetailById(Long id) {
        return bedService.getDetailById(id);
    }

    @Override
    protected boolean callSave(BedSaveDTO saveDTO) {
        return bedService.saveBed(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return bedService.deleteBed(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return bedService.batchDelete(ids);
    }

    @Override
    public boolean callUpdateStatus(Long id, Integer status) {
        return bedService.updateStatus(id, status);
    }

    @PostMapping("/batch-create")
    @Operation(summary = "批量创建床位")
    @Log(title = "批量创建床位", businessType = 1)
    public R<Integer> batchCreate(@RequestBody @Valid BedBatchCreateDTO dto) {
        return R.ok(bedService.batchCreateBeds(dto));
    }
}
