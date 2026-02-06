package com.project.backend.accommodation.controller;

import com.project.backend.accommodation.dto.stay.StayQueryDTO;
import com.project.backend.accommodation.dto.stay.StaySaveDTO;
import com.project.backend.accommodation.service.StayService;
import com.project.backend.accommodation.vo.StayVO;
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
 * 留宿管理Controller
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@RestController
@RequestMapping("/v1/system/stay")
@RequiredArgsConstructor
public class StayController extends BaseCrudController<StayVO, StayQueryDTO, StaySaveDTO> 
        implements BatchDeleteController {

    private final StayService stayService;

    @Override
    public String getEntityName() {
        return "留宿管理";
    }

    @Override
    protected PageResult<StayVO> callPageList(StayQueryDTO queryDTO) {
        return stayService.pageList(queryDTO);
    }

    @Override
    protected StayVO callGetDetailById(Long id) {
        return stayService.getDetailById(id);
    }

    @Override
    protected boolean callSave(StaySaveDTO saveDTO) {
        return stayService.saveStay(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return stayService.deleteStay(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return stayService.batchDelete(ids);
    }

    /**
     * 撤回留宿申请
     */
    @Operation(summary = "撤回留宿申请")
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        boolean success = stayService.cancelStay(id);
        return success ? R.ok() : R.fail("撤回失败");
    }
}
