package com.project.backend.organization.controller;

import com.project.core.result.R;
import com.project.backend.organization.service.SchoolHierarchyService;
import com.project.backend.organization.vo.SchoolHierarchyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学校层级管理控制器
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/school")
@RequiredArgsConstructor
@Tag(name = "学校层级管理", description = "获取完整的学校层级树数据")
public class SchoolHierarchyController {

    private final SchoolHierarchyService schoolHierarchyService;

    /**
     * 获取完整的学校层级树
     * 包含校区、院系、专业、班级的完整层级结构
     */
    @GetMapping("/hierarchy")
    @Operation(summary = "获取完整的学校层级树")
    public R<SchoolHierarchyVO> getFullHierarchy() {
        SchoolHierarchyVO hierarchy = schoolHierarchyService.getFullHierarchy();
        if (hierarchy != null) {
            return R.ok(hierarchy);
        } else {
            return R.fail("学校层级树为空");
        }
    }
}
