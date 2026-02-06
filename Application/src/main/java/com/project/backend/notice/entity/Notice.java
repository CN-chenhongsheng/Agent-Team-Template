package com.project.backend.notice.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知公告实体
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
@Schema(description = "通知公告实体")
public class Notice extends BaseEntity {

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "内容")
    @TableField("content")
    private String content;

    @Schema(description = "通知类型：1系统通知 2宿舍公告 3安全提醒 4停水停电 99其他")
    @TableField("notice_type")
    private Integer noticeType;

    @Schema(description = "封面图片")
    @TableField("cover_image")
    private String coverImage;

    @Schema(description = "附件（JSON数组）")
    @TableField("attachments")
    private String attachments;

    @Schema(description = "发布人ID")
    @TableField("publisher_id")
    private Long publisherId;

    @Schema(description = "发布人姓名")
    @TableField("publisher_name")
    private String publisherName;

    @Schema(description = "发布时间")
    @TableField("publish_time")
    private LocalDateTime publishTime;

    @Schema(description = "是否置顶：0否 1是")
    @TableField("is_top")
    private Boolean isTop;

    @Schema(description = "目标楼层（JSON数组，为空表示全部）")
    @TableField("target_floors")
    private String targetFloors;

    @Schema(description = "阅读次数")
    @TableField("read_count")
    private Integer readCount;

    @Schema(description = "状态：0草稿 1已发布")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
