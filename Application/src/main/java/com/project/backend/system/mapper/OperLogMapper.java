package com.project.backend.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.system.entity.OperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Mapper
public interface OperLogMapper extends BaseMapper<OperLog> {
}
