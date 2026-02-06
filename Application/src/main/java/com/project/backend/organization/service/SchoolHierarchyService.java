package com.project.backend.organization.service;

import com.project.backend.organization.vo.SchoolHierarchyVO;

/**
 * 学校层级服务
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
public interface SchoolHierarchyService {

    /**
     * 获取完整的学校层级树
     * 包含校区、院系、专业、班级的完整层级结构
     * 
     * @return 完整的层级树
     */
    SchoolHierarchyVO getFullHierarchy();
}
