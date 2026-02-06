package com.project.backend.allocation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.allocation.algorithm.AlgorithmFactory;
import com.project.backend.allocation.dto.config.AllocationConfigQueryDTO;
import com.project.backend.allocation.dto.config.AllocationConfigSaveDTO;
import com.project.backend.allocation.entity.AllocationConfig;
import com.project.backend.allocation.mapper.AllocationConfigMapper;
import com.project.backend.allocation.service.AllocationConfigService;
import com.project.backend.allocation.vo.AllocationConfigVO;
import com.project.backend.allocation.vo.AlgorithmInfoVO;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分配配置服务实现
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationConfigServiceImpl extends ServiceImpl<AllocationConfigMapper, AllocationConfig>
        implements AllocationConfigService {

    private final AlgorithmFactory algorithmFactory;

    private static final Map<String, String> ALGORITHM_NAMES = new HashMap<>();

    static {
        ALGORITHM_NAMES.put("greedy", "贪心算法");
        ALGORITHM_NAMES.put("kmeans", "聚类分配算法");
        ALGORITHM_NAMES.put("annealing", "模拟退火算法");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AllocationConfigVO> pageList(AllocationConfigQueryDTO queryDTO) {
        LambdaQueryWrapper<AllocationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getConfigName()),
                        AllocationConfig::getConfigName, queryDTO.getConfigName())
                .eq(StrUtil.isNotBlank(queryDTO.getAlgorithmType()),
                        AllocationConfig::getAlgorithmType, queryDTO.getAlgorithmType())
                .eq(queryDTO.getStatus() != null, AllocationConfig::getStatus, queryDTO.getStatus())
                .orderByDesc(AllocationConfig::getCreateTime);

        Page<AllocationConfig> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<AllocationConfigVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.build(voList, page.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationConfigVO getDetailById(Long id) {
        AllocationConfig config = getById(id);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }
        return convertToVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveConfig(AllocationConfigSaveDTO saveDTO) {
        // 验证配置
        validateConfig(saveDTO);

        // 检查名称重复
        LambdaQueryWrapper<AllocationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AllocationConfig::getConfigName, saveDTO.getConfigName());
        if (saveDTO.getId() != null) {
            wrapper.ne(AllocationConfig::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("配置名称已存在");
        }

        AllocationConfig config;
        if (saveDTO.getId() != null) {
            // 编辑
            config = getById(saveDTO.getId());
            if (config == null) {
                throw new BusinessException("配置不存在");
            }
            BeanUtil.copyProperties(saveDTO, config, "id", "createTime", "createBy");
        } else {
            // 新增
            config = new AllocationConfig();
            BeanUtil.copyProperties(saveDTO, config);
            config.setStatus(1); // 默认启用
        }

        return saveOrUpdate(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(Long id) {
        AllocationConfig config = getById(id);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }

        // TODO: 检查是否被任务引用

        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyConfig(Long sourceId, String newName) {
        AllocationConfig source = getById(sourceId);
        if (source == null) {
            throw new BusinessException("源配置不存在");
        }

        // 检查新名称是否重复
        LambdaQueryWrapper<AllocationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AllocationConfig::getConfigName, newName);
        if (count(wrapper) > 0) {
            throw new BusinessException("配置名称已存在");
        }

        AllocationConfig newConfig = new AllocationConfig();
        BeanUtil.copyProperties(source, newConfig, "id", "createTime", "createBy", "updateTime", "updateBy");
        newConfig.setConfigName(newName);
        newConfig.setRemark("复制自：" + source.getConfigName());

        save(newConfig);
        return newConfig.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationConfigVO getDefaultTemplate() {
        // 查找名为"默认配置模板"的配置
        LambdaQueryWrapper<AllocationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AllocationConfig::getConfigName, "默认配置模板")
                .eq(AllocationConfig::getStatus, 1);
        AllocationConfig config = getOne(wrapper);

        if (config != null) {
            return convertToVO(config);
        }

        // 如果没有，返回一个默认的VO
        AllocationConfigVO vo = new AllocationConfigVO();
        vo.setConfigName("");
        vo.setSmokingConstraint(1);
        vo.setGenderConstraint(1);
        vo.setSleepHardConstraint(0);
        vo.setSleepWeight(30);
        vo.setSmokingWeight(20);
        vo.setCleanlinessWeight(15);
        vo.setSocialWeight(15);
        vo.setStudyWeight(10);
        vo.setEntertainmentWeight(10);
        vo.setAlgorithmType("kmeans");
        vo.setAlgorithmTypeName("聚类分配算法");
        vo.setSameDeptBonus(5);
        vo.setSameMajorBonus(10);
        vo.setSameClassBonus(15);
        vo.setMinMatchScore(60);
        return vo;
    }

    @Override
    public void validateConfig(AllocationConfigSaveDTO saveDTO) {
        // 验证权重总和
        int totalWeight = (saveDTO.getSleepWeight() != null ? saveDTO.getSleepWeight() : 0)
                + (saveDTO.getSmokingWeight() != null ? saveDTO.getSmokingWeight() : 0)
                + (saveDTO.getCleanlinessWeight() != null ? saveDTO.getCleanlinessWeight() : 0)
                + (saveDTO.getSocialWeight() != null ? saveDTO.getSocialWeight() : 0)
                + (saveDTO.getStudyWeight() != null ? saveDTO.getStudyWeight() : 0)
                + (saveDTO.getEntertainmentWeight() != null ? saveDTO.getEntertainmentWeight() : 0);

        if (totalWeight != 100) {
            throw new BusinessException("维度权重总和必须为100，当前为：" + totalWeight);
        }

        // 验证算法类型
        if (StrUtil.isNotBlank(saveDTO.getAlgorithmType())) {
            if (!ALGORITHM_NAMES.containsKey(saveDTO.getAlgorithmType())) {
                throw new BusinessException("不支持的算法类型：" + saveDTO.getAlgorithmType());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        AllocationConfig config = getById(id);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }
        config.setStatus(status);
        return updateById(config);
    }

    @Override
    public List<AlgorithmInfoVO> getAlgorithms() {
        return algorithmFactory.getAllAlgorithms();
    }

    private AllocationConfigVO convertToVO(AllocationConfig config) {
        AllocationConfigVO vo = new AllocationConfigVO();
        BeanUtil.copyProperties(config, vo);
        vo.setAlgorithmTypeName(ALGORITHM_NAMES.getOrDefault(config.getAlgorithmType(), "未知"));
        return vo;
    }
}
