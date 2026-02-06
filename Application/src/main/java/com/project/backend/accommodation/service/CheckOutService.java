package com.project.backend.accommodation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.accommodation.dto.checkout.CheckOutQueryDTO;
import com.project.backend.accommodation.dto.checkout.CheckOutSaveDTO;
import com.project.backend.accommodation.entity.CheckOut;
import com.project.backend.accommodation.vo.CheckOutVO;
import com.project.core.result.PageResult;

/**
 * 退宿管理Service
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
public interface CheckOutService extends IService<CheckOut> {

    /**
     * 分页查询退宿管理列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<CheckOutVO> pageList(CheckOutQueryDTO queryDTO);

    /**
     * 根据ID获取退宿管理详情
     * 
     * @param id 退宿记录ID
     * @return 退宿管理信息
     */
    CheckOutVO getDetailById(Long id);

    /**
     * 保存退宿管理（新增或编辑）
     * 
     * @param saveDTO 退宿管理保存DTO
     * @return 是否成功
     */
    boolean saveCheckOut(CheckOutSaveDTO saveDTO);

    /**
     * 删除退宿管理
     * 
     * @param id 退宿记录ID
     * @return 是否成功
     */
    boolean deleteCheckOut(Long id);

    /**
     * 批量删除退宿管理
     *
     * @param ids 退宿记录ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 撤回退宿申请
     *
     * @param id 退宿记录ID
     * @return 是否成功
     */
    boolean cancelCheckOut(Long id);
}
