package com.project.backend.allocation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.allocation.dto.config.AllocationConfigQueryDTO;
import com.project.backend.allocation.dto.config.AllocationConfigSaveDTO;
import com.project.backend.allocation.entity.AllocationConfig;
import com.project.backend.allocation.vo.AllocationConfigVO;
import com.project.backend.allocation.vo.AlgorithmInfoVO;
import com.project.core.result.PageResult;

import java.util.List;

/**
 * 分配配置服务接口
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
public interface AllocationConfigService extends IService<AllocationConfig> {

    /**
     * 分页查询配置列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AllocationConfigVO> pageList(AllocationConfigQueryDTO queryDTO);

    /**
     * 获取配置详情
     *
     * @param id 配置ID
     * @return 配置详情
     */
    AllocationConfigVO getDetailById(Long id);

    /**
     * 保存配置（新增或编辑）
     *
     * @param saveDTO 保存DTO
     * @return 是否成功
     */
    boolean saveConfig(AllocationConfigSaveDTO saveDTO);

    /**
     * 删除配置
     *
     * @param id 配置ID
     * @return 是否成功
     */
    boolean deleteConfig(Long id);

    /**
     * 批量删除配置
     *
     * @param ids 配置ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 复制配置
     *
     * @param sourceId 源配置ID
     * @param newName 新配置名称
     * @return 新配置ID
     */
    Long copyConfig(Long sourceId, String newName);

    /**
     * 获取默认配置模板
     *
     * @return 默认配置
     */
    AllocationConfigVO getDefaultTemplate();

    /**
     * 验证配置合法性
     *
     * @param saveDTO 配置DTO
     */
    void validateConfig(AllocationConfigSaveDTO saveDTO);

    /**
     * 更新配置状态
     *
     * @param id 配置ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 获取可用算法列表
     *
     * @return 算法列表
     */
    List<AlgorithmInfoVO> getAlgorithms();
}
