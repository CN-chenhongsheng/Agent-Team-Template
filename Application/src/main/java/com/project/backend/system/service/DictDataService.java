package com.project.backend.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.core.result.PageResult;
import com.project.backend.system.dto.DictDataQueryDTO;
import com.project.backend.system.dto.DictDataSaveDTO;
import com.project.backend.system.entity.DictData;
import com.project.backend.system.vo.DictDataVO;

import java.util.List;
import java.util.Map;

/**
 * 字典数据Service
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
public interface DictDataService extends IService<DictData> {

    /**
     * 分页查询字典数据列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<DictDataVO> pageList(DictDataQueryDTO queryDTO);

    /**
     * 根据字典编码获取字典数据列表
     *
     * @param dictCode 字典编码
     * @return 字典数据列表
     */
    List<DictDataVO> listByDictCode(String dictCode);

    /**
     * 根据字典编码批量获取字典数据
     *
     * @param dictCodes 字典编码列表
     * @return 字典数据Map，key为字典编码，value为对应的字典数据列表
     */
    Map<String, List<DictDataVO>> listByDictCodes(List<String> dictCodes);

    /**
     * 根据ID获取字典数据详情
     * 
     * @param id 字典数据ID
     * @return 字典数据信息
     */
    DictDataVO getDetailById(Long id);

    /**
     * 保存字典数据（新增或编辑）
     * 
     * @param saveDTO 字典数据保存DTO
     * @return 是否成功
     */
    boolean saveDictData(DictDataSaveDTO saveDTO);

    /**
     * 删除字典数据
     * 
     * @param id 字典数据ID
     * @return 是否成功
     */
    boolean deleteDictData(Long id);
}
