package com.project.backend.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.system.dto.DictTypeQueryDTO;
import com.project.backend.system.dto.DictTypeSaveDTO;
import com.project.backend.system.entity.DictType;
import com.project.backend.system.mapper.DictTypeMapper;
import com.project.backend.system.service.DictTypeService;
import com.project.backend.util.DictUtils;
import com.project.backend.system.vo.DictTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典类型Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@Service
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictTypeService {

    /**
     * 分页查询字典类型列表
     */
    @Override
    public PageResult<DictTypeVO> pageList(DictTypeQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getDictName()), DictType::getDictName, queryDTO.getDictName())
               .like(StrUtil.isNotBlank(queryDTO.getDictCode()), DictType::getDictCode, queryDTO.getDictCode())
               .eq(queryDTO.getStatus() != null, DictType::getStatus, queryDTO.getStatus())
               .orderByDesc(DictType::getCreateTime);

        // 分页查询
        Page<DictType> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        // 转换为VO
        List<DictTypeVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 根据ID获取字典类型详情
     */
    @Override
    public DictTypeVO getDetailById(Long id) {
        DictType dictType = getById(id);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }
        return convertToVO(dictType);
    }

    /**
     * 保存字典类型（新增或编辑）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDictType(DictTypeSaveDTO saveDTO) {
        // 检查字典编码是否重复
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictType::getDictCode, saveDTO.getDictCode())
               .eq(DictType::getDeleted, 0);
        if (saveDTO.getId() != null) {
            wrapper.ne(DictType::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("字典编码已存在");
        }

        DictType dictType = new DictType();
        BeanUtil.copyProperties(saveDTO, dictType);

        if (saveDTO.getId() == null) {
            // 新增
            dictType.setStatus(1); // 默认启用
            return save(dictType);
        } else {
            // 编辑
            return updateById(dictType);
        }
    }

    /**
     * 删除字典类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictType(Long id) {
        if (id == null) {
            throw new BusinessException("字典类型ID不能为空");
        }

        // TODO: 检查是否有字典数据使用该类型

        return removeById(id);
    }

    /**
     * 实体转VO
     */
    private DictTypeVO convertToVO(DictType dictType) {
        DictTypeVO vo = new DictTypeVO();
        BeanUtil.copyProperties(dictType, vo);

        // 状态文本（使用字典类型）
        vo.setStatusText(DictUtils.getLabel("sys_user_status", dictType.getStatus(), "未知"));

        return vo;
    }
}
