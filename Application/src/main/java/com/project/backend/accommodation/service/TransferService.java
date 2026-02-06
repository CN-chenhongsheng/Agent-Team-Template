package com.project.backend.accommodation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.accommodation.dto.transfer.TransferQueryDTO;
import com.project.backend.accommodation.dto.transfer.TransferSaveDTO;
import com.project.backend.accommodation.entity.Transfer;
import com.project.backend.accommodation.vo.TransferVO;
import com.project.core.result.PageResult;

/**
 * 调宿管理Service
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
public interface TransferService extends IService<Transfer> {

    /**
     * 分页查询调宿管理列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<TransferVO> pageList(TransferQueryDTO queryDTO);

    /**
     * 根据ID获取调宿管理详情
     * 
     * @param id 调宿记录ID
     * @return 调宿管理信息
     */
    TransferVO getDetailById(Long id);

    /**
     * 保存调宿管理（新增或编辑）
     * 
     * @param saveDTO 调宿管理保存DTO
     * @return 是否成功
     */
    boolean saveTransfer(TransferSaveDTO saveDTO);

    /**
     * 删除调宿管理
     * 
     * @param id 调宿记录ID
     * @return 是否成功
     */
    boolean deleteTransfer(Long id);

    /**
     * 批量删除调宿管理
     *
     * @param ids 调宿记录ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 撤回调宿申请
     *
     * @param id 调宿记录ID
     * @return 是否成功
     */
    boolean cancelTransfer(Long id);
}
