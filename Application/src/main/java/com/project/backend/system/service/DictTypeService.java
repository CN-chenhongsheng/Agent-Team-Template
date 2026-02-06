package com.project.backend.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.core.result.PageResult;
import com.project.backend.system.dto.DictTypeQueryDTO;
import com.project.backend.system.dto.DictTypeSaveDTO;
import com.project.backend.system.entity.DictType;
import com.project.backend.system.vo.DictTypeVO;

/**
 * 字典类型Service
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
public interface DictTypeService extends IService<DictType> {

    /**
     * 分页查询字典类型列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<DictTypeVO> pageList(DictTypeQueryDTO queryDTO);

    /**
     * 根据ID获取字典类型详情
     * 
     * @param id 字典类型ID
     * @return 字典类型信息
     */
    DictTypeVO getDetailById(Long id);

    /**
     * 保存字典类型（新增或编辑）
     * 
     * @param saveDTO 字典类型保存DTO
     * @return 是否成功
     */
    boolean saveDictType(DictTypeSaveDTO saveDTO);

    /**
     * 删除字典类型
     * 
     * @param id 字典类型ID
     * @return 是否成功
     */
    boolean deleteDictType(Long id);
}
