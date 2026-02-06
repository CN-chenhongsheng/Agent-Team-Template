package com.project.backend.organization.controller;

import com.project.core.result.PageResult;
import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import com.project.backend.controller.base.StatusUpdateController;
import com.project.backend.controller.base.TreeController;
import com.project.backend.organization.dto.department.DepartmentQueryDTO;
import com.project.backend.organization.dto.department.DepartmentSaveDTO;
import com.project.backend.organization.service.DepartmentService;
import com.project.backend.organization.vo.DepartmentVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 院系管理控制器
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/department")
@RequiredArgsConstructor
@Tag(name = "院系管理", description = "院系增删改查、树形列表等")
public class DepartmentController extends BaseCrudController<DepartmentVO, DepartmentQueryDTO, DepartmentSaveDTO>
        implements TreeController<DepartmentVO, DepartmentQueryDTO>,
                   BatchDeleteController,
                   StatusUpdateController {

    private final DepartmentService departmentService;

    @Override
    public String getEntityName() {
        return "院系";
    }

    @Override
    protected PageResult<DepartmentVO> callPageList(DepartmentQueryDTO queryDTO) {
        return departmentService.pageList(queryDTO);
    }

    @Override
    protected DepartmentVO callGetDetailById(Long id) {
        return departmentService.getDetailById(id);
    }

    @Override
    protected boolean callSave(DepartmentSaveDTO saveDTO) {
        return departmentService.saveDepartment(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return departmentService.deleteDepartment(id);
    }

    @Override
    public List<DepartmentVO> callTreeList(DepartmentQueryDTO queryDTO) {
        return departmentService.treeList(queryDTO);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return departmentService.batchDelete(ids);
    }

    @Override
    public boolean callUpdateStatus(Long id, Integer status) {
        return departmentService.updateStatus(id, status);
    }
}
