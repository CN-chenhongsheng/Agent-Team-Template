package com.project.backend.accommodation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.accommodation.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入住管理Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {
}
