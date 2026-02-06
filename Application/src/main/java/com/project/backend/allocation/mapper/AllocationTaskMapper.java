package com.project.backend.allocation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.allocation.entity.AllocationTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分配任务Mapper
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Mapper
public interface AllocationTaskMapper extends BaseMapper<AllocationTask> {
}
