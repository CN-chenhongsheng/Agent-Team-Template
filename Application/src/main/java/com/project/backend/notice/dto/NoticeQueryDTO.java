package com.project.backend.notice.dto;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知公告查询DTO
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知公告查询条件")
public class NoticeQueryDTO extends BaseQueryDTO {

    @Schema(description = "标题（模糊查询）")
    private String title;

    @Schema(description = "通知类型：1系统通知 2宿舍公告 3安全提醒 4停水停电 99其他")
    private Integer noticeType;

    @Schema(description = "发布人姓名（模糊查询）")
    private String publisherName;

    @Schema(description = "状态：0草稿 1已发布")
    private Integer status;

    @Schema(description = "发布时间开始")
    private LocalDateTime publishTimeStart;

    @Schema(description = "发布时间结束")
    private LocalDateTime publishTimeEnd;

    @Schema(description = "用户ID（用于查询已读状态，小程序端传入）")
    private Long userId;
}
