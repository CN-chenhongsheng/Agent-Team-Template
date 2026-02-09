package com.project.backend.accommodation.controller;

import com.project.backend.accommodation.dto.checkin.CheckInQueryDTO;
import com.project.backend.accommodation.dto.checkin.CheckInSaveDTO;
import com.project.backend.accommodation.service.CheckInService;
import com.project.backend.accommodation.vo.CheckInVO;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import com.project.core.annotation.Log;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 入住管理Controller
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@RestController
@RequestMapping("/v1/system/check-in")
@RequiredArgsConstructor
@Tag(name = "入住管理", description = "入住申请增删改查")
public class CheckInController extends BaseCrudController<CheckInVO, CheckInQueryDTO, CheckInSaveDTO> 
        implements BatchDeleteController {

    private final CheckInService checkInService;

    @Override
    public String getEntityName() {
        return "入住管理";
    }

    @Override
    protected PageResult<CheckInVO> callPageList(CheckInQueryDTO queryDTO) {
        return checkInService.pageList(queryDTO);
    }

    @Override
    protected CheckInVO callGetDetailById(Long id) {
        return checkInService.getDetailById(id);
    }

    @Override
    protected boolean callSave(CheckInSaveDTO saveDTO) {
        return checkInService.saveCheckIn(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return checkInService.deleteCheckIn(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return checkInService.batchDelete(ids);
    }

    /**
     * 撤回入住申请
     */
    @Operation(summary = "撤回入住申请")
    @Log(title = "撤回入住申请", businessType = 2)
    @PutMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        boolean success = checkInService.cancelCheckIn(id);
        return success ? R.ok() : R.fail("撤回失败");
    }

    /**
     * 管理员直接分配床位（跳过审批流程）
     * 用于可视化视图中管理员直接将学生分配到空床位
     */
    @Operation(summary = "管理员直接分配床位", description = "管理员直接将学生分配到空床位，跳过审批流程")
    @Log(title = "管理员直接分配床位", businessType = 1)
    @PostMapping("/admin-assign")
    public R<Void> adminAssignBed(@RequestBody CheckInSaveDTO saveDTO) {
        boolean success = checkInService.adminAssignBed(saveDTO);
        return success ? R.ok() : R.fail("分配失败");
    }
}
