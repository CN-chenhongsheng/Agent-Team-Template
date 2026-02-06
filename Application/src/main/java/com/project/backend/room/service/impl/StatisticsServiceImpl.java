package com.project.backend.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.entity.Room;
import com.project.backend.room.mapper.BedMapper;
import com.project.backend.room.mapper.FloorMapper;
import com.project.backend.room.mapper.RoomMapper;
import com.project.backend.room.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 统计服务实现
 *
 * @author 陈鸿昇
 * @since 2026-01-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final RoomMapper roomMapper;
    private final FloorMapper floorMapper;
    private final BedMapper bedMapper;

    /**
     * 更新房间统计字段
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoomStatistics(Long roomId) {
        if (roomId == null) {
            return;
        }

        try {
            // 统计该房间已占用的床位数
            LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
            bedWrapper.eq(Bed::getRoomId, roomId)
                      .eq(Bed::getBedStatus, 2); // 2-已占用
            long currentOccupancy = bedMapper.selectCount(bedWrapper);

            // 更新房间统计字段
            Room room = roomMapper.selectById(roomId);
            if (room != null) {
                room.setCurrentOccupancy((int) currentOccupancy);
                roomMapper.updateById(room);
            } else {
                log.warn("更新房间统计字段失败：房间不存在，房间ID：{}", roomId);
            }
        } catch (Exception e) {
            log.error("更新房间统计字段异常，房间ID：{}", roomId, e);
            throw e; // 重新抛出异常，由调用方处理
        }
    }

    /**
     * 更新楼层统计字段
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFloorStatistics(Long floorId) {
        if (floorId == null) {
            return;
        }

        try {
            // 统计该楼层的房间数和床位数
            LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
            roomWrapper.eq(Room::getFloorId, floorId);
            long totalRooms = roomMapper.selectCount(roomWrapper);

            // 统计该楼层所有房间的床位数
            List<Room> rooms = roomMapper.selectList(roomWrapper);
            int totalBeds = rooms.stream()
                    .mapToInt(room -> room.getBedCount() != null ? room.getBedCount() : 0)
                    .sum();

            // 统计该楼层所有床位的入住人数
            LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
            bedWrapper.eq(Bed::getFloorId, floorId)
                       .eq(Bed::getBedStatus, 2); // 2-已占用
            long currentOccupancy = bedMapper.selectCount(bedWrapper);

            // 更新楼层统计字段
            Floor floor = floorMapper.selectById(floorId);
            if (floor != null) {
                floor.setTotalRooms((int) totalRooms);
                floor.setTotalBeds(totalBeds);
                floor.setCurrentOccupancy((int) currentOccupancy);
                floorMapper.updateById(floor);
            } else {
                log.warn("更新楼层统计字段失败：楼层不存在，楼层ID：{}", floorId);
            }
        } catch (Exception e) {
            log.error("更新楼层统计字段异常，楼层ID：{}", floorId, e);
            throw e; // 重新抛出异常，由调用方处理
        }
    }
}
