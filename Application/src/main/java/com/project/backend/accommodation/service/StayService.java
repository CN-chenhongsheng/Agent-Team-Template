package com.project.backend.accommodation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.accommodation.dto.stay.StayQueryDTO;
import com.project.backend.accommodation.dto.stay.StaySaveDTO;
import com.project.backend.accommodation.entity.Stay;
import com.project.backend.accommodation.vo.StayVO;
import com.project.core.result.PageResult;

/**
 * 留宿管理Service
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
public interface StayService extends IService<Stay> {

    /**
     * 分页查询留宿管理列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<StayVO> pageList(StayQueryDTO queryDTO);

    /**
     * 根据ID获取留宿管理详情
     * 
     * @param id 留宿记录ID
     * @return 留宿管理信息
     */
    StayVO getDetailById(Long id);

    /**
     * 保存留宿管理（新增或编辑）
     * 
     * @param saveDTO 留宿管理保存DTO
     * @return 是否成功
     */
    boolean saveStay(StaySaveDTO saveDTO);

    /**
     * 删除留宿管理
     * 
     * @param id 留宿记录ID
     * @return 是否成功
     */
    boolean deleteStay(Long id);

    /**
     * 批量删除留宿管理
     *
     * @param ids 留宿记录ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 撤回留宿申请
     *
     * @param id 留宿记录ID
     * @return 是否成功
     */
    boolean cancelStay(Long id);
}
