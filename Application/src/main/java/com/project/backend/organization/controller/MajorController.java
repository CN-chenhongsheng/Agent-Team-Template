package com.project.backend.organization.controller;

import com.project.core.result.PageResult;
import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import com.project.backend.controller.base.StatusUpdateController;
import com.project.backend.organization.dto.major.MajorQueryDTO;
import com.project.backend.organization.dto.major.MajorSaveDTO;
import com.project.backend.organization.service.MajorService;
import com.project.backend.organization.vo.MajorVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 专业管理控制器
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/major")
@RequiredArgsConstructor
@Tag(name = "专业管理", description = "专业增删改查")
public class MajorController extends BaseCrudController<MajorVO, MajorQueryDTO, MajorSaveDTO>
        implements BatchDeleteController, StatusUpdateController {

    private final MajorService majorService;

    @Override
    public String getEntityName() {
        return "专业";
    }

    @Override
    protected PageResult<MajorVO> callPageList(MajorQueryDTO queryDTO) {
        return majorService.pageList(queryDTO);
    }

    @Override
    protected MajorVO callGetDetailById(Long id) {
        return majorService.getDetailById(id);
    }

    @Override
    protected boolean callSave(MajorSaveDTO saveDTO) {
        return majorService.saveMajor(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return majorService.deleteMajor(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return majorService.batchDelete(ids);
    }

    @Override
    public boolean callUpdateStatus(Long id, Integer status) {
        return majorService.updateStatus(id, status);
    }
}
