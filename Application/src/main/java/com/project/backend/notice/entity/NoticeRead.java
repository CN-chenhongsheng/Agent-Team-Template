package com.project.backend.notice.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知阅读记录实体
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice_read")
@Schema(description = "通知阅读记录实体")
public class NoticeRead extends BaseEntity {

    @Schema(description = "通知ID")
    @TableField("notice_id")
    private Long noticeId;

    @Schema(description = "用户ID（学生ID）")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "阅读时间")
    @TableField("read_time")
    private LocalDateTime readTime;
}
