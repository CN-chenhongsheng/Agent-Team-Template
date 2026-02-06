package com.project.backend.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.system.dto.DictDataQueryDTO;
import com.project.backend.system.dto.DictDataSaveDTO;
import com.project.backend.system.entity.DictData;
import com.project.backend.system.mapper.DictDataMapper;
import com.project.backend.system.service.DictDataService;
import com.project.backend.util.DictUtils;
import com.project.backend.system.vo.DictDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典数据Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@Service
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataService {

    /**
     * 分页查询字典数据列表
     */
    @Override
    public PageResult<DictDataVO> pageList(DictDataQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(queryDTO.getDictCode()), DictData::getDictCode, queryDTO.getDictCode())
               .like(StrUtil.isNotBlank(queryDTO.getLabel()), DictData::getLabel, queryDTO.getLabel())
               .eq(queryDTO.getStatus() != null, DictData::getStatus, queryDTO.getStatus())
               .orderByAsc(DictData::getSort)
               .orderByAsc(DictData::getId);

        // 分页查询
        Page<DictData> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        // 转换为VO
        List<DictDataVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 根据字典编码获取字典数据列表（缓存）
     */
    @Override
    @Cacheable(value = "dict:data", key = "#dictCode", unless = "#result == null || #result.isEmpty()")
    public List<DictDataVO> listByDictCode(String dictCode) {
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictCode, dictCode)
               .eq(DictData::getStatus, 1)
               .orderByAsc(DictData::getSort)
               .orderByAsc(DictData::getId);

        List<DictData> list = list(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据字典编码批量获取字典数据
     */
    @Override
    public Map<String, List<DictDataVO>> listByDictCodes(List<String> dictCodes) {
        if (dictCodes == null || dictCodes.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, List<DictDataVO>> result = new HashMap<>();

        // 批量查询每个字典编码的数据
        for (String dictCode : dictCodes) {
            List<DictDataVO> dictDataList = listByDictCode(dictCode);
            result.put(dictCode, dictDataList);
        }

        return result;
    }

    /**
     * 根据ID获取字典数据详情
     */
    @Override
    public DictDataVO getDetailById(Long id) {
        DictData dictData = getById(id);
        if (dictData == null) {
            throw new BusinessException("字典数据不存在");
        }
        return convertToVO(dictData);
    }

    /**
     * 保存字典数据（新增或编辑）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dict:data", key = "#saveDTO.dictCode")
    public boolean saveDictData(DictDataSaveDTO saveDTO) {
        // 检查同一字典编码下，字典值是否重复
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictCode, saveDTO.getDictCode())
               .eq(DictData::getValue, saveDTO.getValue())
               .eq(DictData::getDeleted, 0);
        if (saveDTO.getId() != null) {
            wrapper.ne(DictData::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("该字典编码下字典值已存在");
        }

        DictData dictData = new DictData();
        BeanUtil.copyProperties(saveDTO, dictData);

        boolean success;
        if (saveDTO.getId() == null) {
            // 新增
            dictData.setStatus(1); // 默认启用
            dictData.setIsDefault(0); // 默认不是默认值
            success = save(dictData);
        } else {
            // 编辑
            success = updateById(dictData);
        }

        // 刷新字典缓存
        if (success) {
            DictUtils.refreshCache(saveDTO.getDictCode());
        }

        return success;
    }

    /**
     * 删除字典数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictData(Long id) {
        if (id == null) {
            throw new BusinessException("字典数据ID不能为空");
        }

        // 获取字典编码，用于清除缓存
        DictData dictData = getById(id);
        String dictCode = dictData != null ? dictData.getDictCode() : null;

        boolean success = removeById(id);

        // 清除字典缓存（Spring Cache会通过DictUtils.refreshCache清除，这里也清除Spring Cache）
        if (success && dictCode != null) {
            DictUtils.refreshCache(dictCode);
            // Spring Cache会在DictUtils.refreshCache中处理，这里不需要额外处理
        }

        return success;
    }

    /**
     * 实体转VO
     */
    private DictDataVO convertToVO(DictData dictData) {
        DictDataVO vo = new DictDataVO();
        BeanUtil.copyProperties(dictData, vo);

        // 状态文本（使用字典数据）
        vo.setStatusText(DictUtils.getLabel("sys_user_status", dictData.getStatus(), "未知"));

        return vo;
    }
}
