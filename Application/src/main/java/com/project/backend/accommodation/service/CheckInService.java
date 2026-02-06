package com.project.backend.accommodation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.accommodation.dto.checkin.CheckInQueryDTO;
import com.project.backend.accommodation.dto.checkin.CheckInSaveDTO;
import com.project.backend.accommodation.entity.CheckIn;
import com.project.backend.accommodation.vo.CheckInVO;
import com.project.core.result.PageResult;

/**
 * 入住管理Service
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
public interface CheckInService extends IService<CheckIn> {

    /**
     * 分页查询入住管理列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<CheckInVO> pageList(CheckInQueryDTO queryDTO);

    /**
     * 根据ID获取入住管理详情
     * 
     * @param id 入住记录ID
     * @return 入住管理信息
     */
    CheckInVO getDetailById(Long id);

    /**
     * 保存入住管理（新增或编辑）
     * 
     * @param saveDTO 入住管理保存DTO
     * @return 是否成功
     */
    boolean saveCheckIn(CheckInSaveDTO saveDTO);

    /**
     * 删除入住管理
     * 
     * @param id 入住记录ID
     * @return 是否成功
     */
    boolean deleteCheckIn(Long id);

    /**
     * 批量删除入住管理
     *
     * @param ids 入住记录ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 撤回入住申请
     *
     * @param id 入住记录ID
     * @return 是否成功
     */
    boolean cancelCheckIn(Long id);

    /**
     * 管理员直接分配床位（跳过审批流程）
     * 用于可视化视图中管理员直接将学生分配到空床位
     *
     * @param saveDTO 入住信息
     * @return 是否成功
     */
    boolean adminAssignBed(CheckInSaveDTO saveDTO);
}
