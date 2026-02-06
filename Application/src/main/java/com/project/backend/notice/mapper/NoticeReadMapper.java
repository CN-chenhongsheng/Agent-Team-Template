package com.project.backend.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.notice.entity.NoticeRead;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知阅读记录Mapper
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Mapper
public interface NoticeReadMapper extends BaseMapper<NoticeRead> {
}
