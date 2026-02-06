package com.project.backend.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 我的申请查询DTO
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@Schema(description = "我的申请查询条件")
public class MyApplyQueryDTO {

    @Schema(description = "学生ID", required = true)
    private Long studentId;

    @Schema(description = "申请类型：check_in-入住 transfer-调宿 check_out-退宿 stay-留宿")
    private String applyType;

    @Schema(description = "状态：1待审核 2已通过 3已拒绝 4已完成")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;

    @Schema(description = "限制返回条数（不分页模式，如果设置则忽略pageNum和pageSize）", example = "5")
    private Integer limit;
}
