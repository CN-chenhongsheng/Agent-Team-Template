package com.project.backend.organization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.core.result.PageResult;
import com.project.backend.organization.dto.classes.ClassQueryDTO;
import com.project.backend.organization.dto.classes.ClassSaveDTO;
import com.project.backend.organization.entity.Class;
import com.project.backend.organization.vo.ClassVO;

/**
 * 班级Service
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
public interface ClassService extends IService<Class> {

    /**
     * 分页查询班级列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<ClassVO> pageList(ClassQueryDTO queryDTO);

    /**
     * 根据ID获取班级详情
     * 
     * @param id 班级ID
     * @return 班级信息
     */
    ClassVO getDetailById(Long id);

    /**
     * 保存班级（新增或编辑）
     * 
     * @param saveDTO 班级保存DTO
     * @return 是否成功
     */
    boolean saveClass(ClassSaveDTO saveDTO);

    /**
     * 删除班级
     * 
     * @param id 班级ID
     * @return 是否成功
     */
    boolean deleteClass(Long id);

    /**
     * 批量删除班级
     * 
     * @param ids 班级ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 更新班级状态
     * 
     * @param id 班级ID
     * @param status 状态：1启用 0停用
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}
