package com.project.backend.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.room.entity.Bed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 床位Mapper
 *
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Mapper
public interface BedMapper extends BaseMapper<Bed> {

    /**
     * 按房间ID批量统计床位数
     */
    @Select("<script>" +
            "SELECT room_id, COUNT(*) AS cnt FROM sys_bed " +
            "WHERE deleted = 0 AND room_id IN " +
            "<foreach collection='roomIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " GROUP BY room_id" +
            "</script>")
    List<Map<String, Object>> countGroupByRoomIds(@Param("roomIds") Set<Long> roomIds);
}
