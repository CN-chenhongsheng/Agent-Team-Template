package com.project.backend.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.notice.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知公告Mapper
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
