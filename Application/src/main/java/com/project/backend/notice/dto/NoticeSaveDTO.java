package com.project.backend.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 通知公告保存DTO
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@Schema(description = "通知公告保存参数")
public class NoticeSaveDTO {

    @Schema(description = "主键ID（编辑时传入）")
    private Long id;

    @Schema(description = "标题", required = true)
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "内容", required = true)
    @NotBlank(message = "内容不能为空")
    private String content;

    @Schema(description = "通知类型：1系统通知 2宿舍公告 3安全提醒 4停水停电 99其他", required = true)
    @NotNull(message = "通知类型不能为空")
    private Integer noticeType;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "附件列表")
    private List<String> attachments;

    @Schema(description = "是否置顶：0否 1是")
    private Boolean isTop;

    @Schema(description = "目标楼层列表（为空表示全部）")
    private List<String> targetFloors;

    @Schema(description = "状态：0草稿 1已发布")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
