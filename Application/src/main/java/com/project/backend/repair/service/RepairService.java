package com.project.backend.repair.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.repair.dto.RepairQueryDTO;
import com.project.backend.repair.dto.RepairSaveDTO;
import com.project.backend.repair.entity.Repair;
import com.project.backend.repair.vo.RepairVO;
import com.project.core.result.PageResult;

/**
 * 报修管理Service
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
public interface RepairService extends IService<Repair> {

    /**
     * 分页查询报修列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<RepairVO> pageList(RepairQueryDTO queryDTO);

    /**
     * 根据ID查询报修详情
     *
     * @param id 报修ID
     * @return 报修详情
     */
    RepairVO getDetailById(Long id);

    /**
     * 保存报修（新增或编辑）
     *
     * @param saveDTO 保存数据
     * @return 是否成功
     */
    boolean saveRepair(RepairSaveDTO saveDTO);

    /**
     * 删除报修
     *
     * @param id 报修ID
     * @return 是否成功
     */
    boolean deleteRepair(Long id);

    /**
     * 批量删除报修
     *
     * @param ids 报修ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 提交报修（小程序端）
     *
     * @param saveDTO 保存数据
     * @return 是否成功
     */
    boolean submitRepair(RepairSaveDTO saveDTO);

    /**
     * 取消报修（小程序端）
     *
     * @param id 报修ID
     * @return 是否成功
     */
    boolean cancelRepair(Long id);

    /**
     * 接单（维修人员）
     *
     * @param id 报修ID
     * @return 是否成功
     */
    boolean acceptRepair(Long id);

    /**
     * 完成维修（维修人员）
     *
     * @param id 报修ID
     * @param repairResult 维修结果描述
     * @param repairImages 维修后图片
     * @return 是否成功
     */
    boolean completeRepair(Long id, String repairResult, String repairImages);

    /**
     * 评价报修（学生）
     *
     * @param id 报修ID
     * @param rating 评分
     * @param ratingComment 评价内容
     * @return 是否成功
     */
    boolean rateRepair(Long id, Integer rating, String ratingComment);

    /**
     * 我的报修列表（小程序端）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<RepairVO> myRepairList(RepairQueryDTO queryDTO);
}
