package com.project.backend.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.room.entity.Bed;
import org.apache.ibatis.annotations.Mapper;

/**
 * 床位Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Mapper
public interface BedMapper extends BaseMapper<Bed> {
}
