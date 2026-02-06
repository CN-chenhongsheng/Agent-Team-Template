package com.project.backend.organization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.core.result.PageResult;
import com.project.backend.organization.dto.major.MajorQueryDTO;
import com.project.backend.organization.dto.major.MajorSaveDTO;
import com.project.backend.organization.entity.Major;
import com.project.backend.organization.vo.MajorVO;

/**
 * 专业Service
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
public interface MajorService extends IService<Major> {

    /**
     * 分页查询专业列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<MajorVO> pageList(MajorQueryDTO queryDTO);

    /**
     * 根据ID获取专业详情
     * 
     * @param id 专业ID
     * @return 专业信息
     */
    MajorVO getDetailById(Long id);

    /**
     * 保存专业（新增或编辑）
     * 
     * @param saveDTO 专业保存DTO
     * @return 是否成功
     */
    boolean saveMajor(MajorSaveDTO saveDTO);

    /**
     * 删除专业
     * 
     * @param id 专业ID
     * @return 是否成功
     */
    boolean deleteMajor(Long id);

    /**
     * 批量删除专业
     * 
     * @param ids 专业ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 更新专业状态
     * 
     * @param id 专业ID
     * @param status 状态：1启用 0停用
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}
