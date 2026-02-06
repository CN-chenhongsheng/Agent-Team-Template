package com.project.backend.organization.controller;

import com.project.core.result.PageResult;
import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import com.project.backend.controller.base.StatusUpdateController;
import com.project.backend.organization.dto.classes.ClassQueryDTO;
import com.project.backend.organization.dto.classes.ClassSaveDTO;
import com.project.backend.organization.service.ClassService;
import com.project.backend.organization.vo.ClassVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 班级管理控制器
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/class")
@RequiredArgsConstructor
@Tag(name = "班级管理", description = "班级增删改查")
public class ClassController extends BaseCrudController<ClassVO, ClassQueryDTO, ClassSaveDTO>
        implements BatchDeleteController, StatusUpdateController {

    private final ClassService classService;

    @Override
    public String getEntityName() {
        return "班级";
    }

    @Override
    protected PageResult<ClassVO> callPageList(ClassQueryDTO queryDTO) {
        return classService.pageList(queryDTO);
    }

    @Override
    protected ClassVO callGetDetailById(Long id) {
        return classService.getDetailById(id);
    }

    @Override
    protected boolean callSave(ClassSaveDTO saveDTO) {
        return classService.saveClass(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return classService.deleteClass(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return classService.batchDelete(ids);
    }

    @Override
    public boolean callUpdateStatus(Long id, Integer status) {
        return classService.updateStatus(id, status);
    }
}
