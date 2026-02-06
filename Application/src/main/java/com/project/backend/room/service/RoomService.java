package com.project.backend.room.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.core.result.PageResult;
import com.project.backend.room.dto.room.RoomBatchCreateDTO;
import com.project.backend.room.dto.room.RoomQueryDTO;
import com.project.backend.room.dto.room.RoomSaveDTO;
import com.project.backend.room.entity.Room;
import com.project.backend.room.vo.RoomVO;
import com.project.backend.room.vo.RoomVisualVO;

import java.util.List;

/**
 * 房间Service
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
public interface RoomService extends IService<Room> {

    /**
     * 分页查询房间列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<RoomVO> pageList(RoomQueryDTO queryDTO);

    /**
     * 根据ID获取房间详情
     * 
     * @param id 房间ID
     * @return 房间信息
     */
    RoomVO getDetailById(Long id);

    /**
     * 保存房间（新增或编辑）
     * 
     * @param saveDTO 房间保存DTO
     * @return 是否成功
     */
    boolean saveRoom(RoomSaveDTO saveDTO);

    /**
     * 删除房间
     * 
     * @param id 房间ID
     * @return 是否成功
     */
    boolean deleteRoom(Long id);

    /**
     * 批量删除房间
     * 
     * @param ids 房间ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 更新房间状态
     * 
     * @param id 房间ID
     * @param status 状态：1正常 0停用
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 批量创建房间
     * 
     * @param dto 批量创建参数
     * @return 创建的房间数
     */
    int batchCreateRooms(RoomBatchCreateDTO dto);

    /**
     * 检查房间是否被床位关联
     * 
     * @param roomId 房间ID
     * @return true-有床位关联，false-无床位关联
     */
    boolean checkRoomHasBeds(Long roomId);

    /**
     * 获取房间可视化列表（含床位和学生信息）
     * 用于可视化平面图展示
     * 
     * @param floorId 楼层ID
     * @return 房间列表（含床位信息）
     */
    List<RoomVisualVO> getVisualRoomList(Long floorId);
}
