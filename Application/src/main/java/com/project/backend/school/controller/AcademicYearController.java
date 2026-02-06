package com.project.backend.school.controller;

import com.project.core.result.PageResult;
import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import com.project.backend.controller.base.StatusUpdateController;
import com.project.backend.school.dto.AcademicYearQueryDTO;
import com.project.backend.school.dto.AcademicYearSaveDTO;
import com.project.backend.school.service.AcademicYearService;
import com.project.backend.school.vo.AcademicYearVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学年管理控制器
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/academic-year")
@RequiredArgsConstructor
@Tag(name = "学年管理控制器", description = "学年增删改查、状态更新等")
public class AcademicYearController extends BaseCrudController<AcademicYearVO, AcademicYearQueryDTO, AcademicYearSaveDTO>
        implements BatchDeleteController, StatusUpdateController {

    private final AcademicYearService academicYearService;

    @Override
    public String getEntityName() {
        return "学年";
    }

    @Override
    protected PageResult<AcademicYearVO> callPageList(AcademicYearQueryDTO queryDTO) {
        return academicYearService.pageList(queryDTO);
    }

    @Override
    protected AcademicYearVO callGetDetailById(Long id) {
        return academicYearService.getDetailById(id);
    }

    @Override
    protected boolean callSave(AcademicYearSaveDTO saveDTO) {
        return academicYearService.saveAcademicYear(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return academicYearService.deleteAcademicYear(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return academicYearService.batchDelete(ids);
    }

    @Override
    public boolean callUpdateStatus(Long id, Integer status) {
        return academicYearService.updateStatus(id, status);
    }
}
