package com.project.backend.notice.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知公告展示VO
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@Schema(description = "通知公告信息响应")
public class NoticeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "通知类型：1系统通知 2宿舍公告 3安全提醒 4停水停电 99其他")
    private Integer noticeType;

    @Schema(description = "通知类型文本")
    private String noticeTypeText;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "附件列表")
    private List<String> attachments;

    @Schema(description = "发布人ID")
    private Long publisherId;

    @Schema(description = "发布人姓名")
    private String publisherName;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    @Schema(description = "是否置顶：0否 1是")
    private Boolean isTop;

    @Schema(description = "目标楼层列表")
    private List<String> targetFloors;

    @Schema(description = "阅读次数")
    private Integer readCount;

    @Schema(description = "状态：0草稿 1已发布")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "当前用户是否已读")
    private Boolean isRead;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
