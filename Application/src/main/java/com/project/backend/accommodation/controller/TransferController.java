package com.project.backend.accommodation.controller;

import com.project.backend.accommodation.dto.transfer.TransferQueryDTO;
import com.project.backend.accommodation.dto.transfer.TransferSaveDTO;
import com.project.backend.accommodation.service.TransferService;
import com.project.backend.accommodation.vo.TransferVO;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调宿管理Controller
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@RestController
@RequestMapping("/v1/system/transfer")
@RequiredArgsConstructor
public class TransferController extends BaseCrudController<TransferVO, TransferQueryDTO, TransferSaveDTO> 
        implements BatchDeleteController {

    private final TransferService transferService;

    @Override
    public String getEntityName() {
        return "调宿管理";
    }

    @Override
    protected PageResult<TransferVO> callPageList(TransferQueryDTO queryDTO) {
        return transferService.pageList(queryDTO);
    }

    @Override
    protected TransferVO callGetDetailById(Long id) {
        return transferService.getDetailById(id);
    }

    @Override
    protected boolean callSave(TransferSaveDTO saveDTO) {
        return transferService.saveTransfer(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return transferService.deleteTransfer(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return transferService.batchDelete(ids);
    }

    /**
     * 撤回调宿申请
     */
    @Operation(summary = "撤回调宿申请")
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        boolean success = transferService.cancelTransfer(id);
        return success ? R.ok() : R.fail("撤回失败");
    }
}
