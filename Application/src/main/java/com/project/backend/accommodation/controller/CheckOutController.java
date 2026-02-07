package com.project.backend.accommodation.controller;

import com.project.backend.accommodation.dto.checkout.CheckOutQueryDTO;
import com.project.backend.accommodation.dto.checkout.CheckOutSaveDTO;
import com.project.backend.accommodation.service.CheckOutService;
import com.project.backend.accommodation.vo.CheckOutVO;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 退宿管理Controller
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@RestController
@RequestMapping("/v1/system/check-out")
@RequiredArgsConstructor
@Tag(name = "退宿管理", description = "退宿申请增删改查")
public class CheckOutController extends BaseCrudController<CheckOutVO, CheckOutQueryDTO, CheckOutSaveDTO> 
        implements BatchDeleteController {

    private final CheckOutService checkOutService;

    @Override
    public String getEntityName() {
        return "退宿管理";
    }

    @Override
    protected PageResult<CheckOutVO> callPageList(CheckOutQueryDTO queryDTO) {
        return checkOutService.pageList(queryDTO);
    }

    @Override
    protected CheckOutVO callGetDetailById(Long id) {
        return checkOutService.getDetailById(id);
    }

    @Override
    protected boolean callSave(CheckOutSaveDTO saveDTO) {
        return checkOutService.saveCheckOut(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return checkOutService.deleteCheckOut(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return checkOutService.batchDelete(ids);
    }

    /**
     * 撤回退宿申请
     */
    @Operation(summary = "撤回退宿申请")
    @PutMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        boolean success = checkOutService.cancelCheckOut(id);
        return success ? R.ok() : R.fail("撤回失败");
    }
}
