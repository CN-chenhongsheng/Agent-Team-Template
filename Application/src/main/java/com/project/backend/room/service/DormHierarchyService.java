package com.project.backend.room.service;

import com.project.backend.room.vo.DormHierarchyVO;

/**
 * 宿舍层级服务
 * 
 * @author 陈鸿昇
 * @since 2026-02-04
 */
public interface DormHierarchyService {

    /**
     * 获取完整的宿舍层级树
     * 包含校区、楼层、房间、床位的完整层级结构
     * 
     * @return 完整的层级树
     */
    DormHierarchyVO getFullHierarchy();
}
