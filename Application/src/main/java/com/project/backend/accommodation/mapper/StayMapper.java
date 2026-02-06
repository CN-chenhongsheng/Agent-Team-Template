package com.project.backend.accommodation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.accommodation.entity.Stay;
import org.apache.ibatis.annotations.Mapper;

/**
 * 留宿管理Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Mapper
public interface StayMapper extends BaseMapper<Stay> {
}
