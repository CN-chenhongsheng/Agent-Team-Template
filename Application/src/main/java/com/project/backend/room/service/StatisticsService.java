package com.project.backend.room.service;

/**
 * 统计服务接口
 * 提供房间和楼层的统计数据更新功能
 *
 * @author 陈鸿昇
 * @since 2026-01-28
 */
public interface StatisticsService {

    /**
     * 更新房间统计字段
     *
     * @param roomId 房间ID
     */
    void updateRoomStatistics(Long roomId);

    /**
     * 更新楼层统计字段
     *
     * @param floorId 楼层ID
     */
    void updateFloorStatistics(Long floorId);
}
