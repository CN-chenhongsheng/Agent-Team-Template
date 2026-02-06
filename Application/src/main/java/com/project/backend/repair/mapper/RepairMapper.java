package com.project.backend.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.repair.entity.Repair;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报修管理Mapper
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Mapper
public interface RepairMapper extends BaseMapper<Repair> {
}
