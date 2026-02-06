package com.project.backend.organization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.core.result.PageResult;
import com.project.backend.organization.dto.campus.CampusQueryDTO;
import com.project.backend.organization.dto.campus.CampusSaveDTO;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.vo.CampusVO;

import java.util.List;

/**
 * 校区Service
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
public interface CampusService extends IService<Campus> {

    /**
     * 分页查询校区列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<CampusVO> pageList(CampusQueryDTO queryDTO);

    /**
     * 查询校区树形列表
     * 
     * @param queryDTO 查询条件
     * @return 校区树形列表
     */
    List<CampusVO> treeList(CampusQueryDTO queryDTO);

    /**
     * 根据ID获取校区详情
     * 
     * @param id 校区ID
     * @return 校区信息
     */
    CampusVO getDetailById(Long id);

    /**
     * 保存校区（新增或编辑）
     * 
     * @param saveDTO 校区保存DTO
     * @return 是否成功
     */
    boolean saveCampus(CampusSaveDTO saveDTO);

    /**
     * 删除校区
     * 
     * @param id 校区ID
     * @return 是否成功
     */
    boolean deleteCampus(Long id);

    /**
     * 批量删除校区
     * 
     * @param ids 校区ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 更新校区状态
     * 
     * @param id 校区ID
     * @param status 状态：1启用 0停用
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}
