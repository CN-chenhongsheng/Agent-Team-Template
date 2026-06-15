package com.project.backend.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.system.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登入日志Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-03-08
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}
