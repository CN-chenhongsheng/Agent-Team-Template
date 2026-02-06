package com.project.backend.organization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.core.result.PageResult;
import com.project.backend.organization.dto.department.DepartmentQueryDTO;
import com.project.backend.organization.dto.department.DepartmentSaveDTO;
import com.project.backend.organization.entity.Department;
import com.project.backend.organization.vo.DepartmentVO;

import java.util.List;

/**
 * 院系Service
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
public interface DepartmentService extends IService<Department> {

    /**
     * 分页查询院系列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<DepartmentVO> pageList(DepartmentQueryDTO queryDTO);

    /**
     * 查询院系树形列表
     * 
     * @param queryDTO 查询条件
     * @return 院系树形列表
     */
    List<DepartmentVO> treeList(DepartmentQueryDTO queryDTO);

    /**
     * 根据ID获取院系详情
     * 
     * @param id 院系ID
     * @return 院系信息
     */
    DepartmentVO getDetailById(Long id);

    /**
     * 保存院系（新增或编辑）
     * 
     * @param saveDTO 院系保存DTO
     * @return 是否成功
     */
    boolean saveDepartment(DepartmentSaveDTO saveDTO);

    /**
     * 删除院系
     * 
     * @param id 院系ID
     * @return 是否成功
     */
    boolean deleteDepartment(Long id);

    /**
     * 批量删除院系
     * 
     * @param ids 院系ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 更新院系状态
     * 
     * @param id 院系ID
     * @param status 状态：1启用 0停用
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}
