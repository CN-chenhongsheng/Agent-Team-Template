package com.project.backend.room.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.core.result.PageResult;
import com.project.backend.room.dto.floor.FloorQueryDTO;
import com.project.backend.room.dto.floor.FloorSaveDTO;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.vo.FloorVO;

/**
 * 楼层Service
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
public interface FloorService extends IService<Floor> {

    /**
     * 分页查询楼层列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<FloorVO> pageList(FloorQueryDTO queryDTO);

    /**
     * 根据ID获取楼层详情
     * 
     * @param id 楼层ID
     * @return 楼层信息
     */
    FloorVO getDetailById(Long id);

    /**
     * 保存楼层（新增或编辑）
     * 
     * @param saveDTO 楼层保存DTO
     * @return 是否成功
     */
    boolean saveFloor(FloorSaveDTO saveDTO);

    /**
     * 删除楼层
     * 
     * @param id 楼层ID
     * @return 是否成功
     */
    boolean deleteFloor(Long id);

    /**
     * 批量删除楼层
     * 
     * @param ids 楼层ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 更新楼层状态
     * 
     * @param id 楼层ID
     * @param status 状态：1正常 0停用
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 检查楼层是否被房间关联
     * 
     * @param floorId 楼层ID
     * @return true-有房间关联，false-无房间关联
     */
    boolean checkFloorHasRooms(Long floorId);
}
