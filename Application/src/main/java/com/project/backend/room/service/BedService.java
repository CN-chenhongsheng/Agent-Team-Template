package com.project.backend.room.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.core.result.PageResult;
import com.project.backend.room.dto.bed.BedBatchCreateDTO;
import com.project.backend.room.dto.bed.BedQueryDTO;
import com.project.backend.room.dto.bed.BedSaveDTO;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.vo.BedVO;

/**
 * 床位Service
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
public interface BedService extends IService<Bed> {

    /**
     * 分页查询床位列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<BedVO> pageList(BedQueryDTO queryDTO);

    /**
     * 根据ID获取床位详情
     * 
     * @param id 床位ID
     * @return 床位信息
     */
    BedVO getDetailById(Long id);

    /**
     * 保存床位（新增或编辑）
     * 
     * @param saveDTO 床位保存DTO
     * @return 是否成功
     */
    boolean saveBed(BedSaveDTO saveDTO);

    /**
     * 删除床位
     * 
     * @param id 床位ID
     * @return 是否成功
     */
    boolean deleteBed(Long id);

    /**
     * 批量删除床位
     * 
     * @param ids 床位ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 更新床位状态
     * 
     * @param id 床位ID
     * @param status 状态：1正常 0停用
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 批量创建床位
     * 
     * @param dto 批量创建参数
     * @return 创建的床位数
     */
    int batchCreateBeds(BedBatchCreateDTO dto);
}
