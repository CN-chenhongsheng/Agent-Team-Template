package com.project.backend.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.room.entity.Room;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房间Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
}
