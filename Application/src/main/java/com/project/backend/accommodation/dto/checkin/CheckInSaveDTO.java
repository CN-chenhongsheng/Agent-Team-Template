package com.project.backend.accommodation.dto.checkin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 入住管理保存DTO（新增或编辑）
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@Schema(description = "入住管理保存请求")
public class CheckInSaveDTO {

    @Schema(description = "入住记录ID（编辑时必传）")
    private Long id;

    @Schema(description = "学生ID（学生端提交可不传，从 token 获取；管理端编辑时必传）")
    private Long studentId;

    @Schema(description = "入住类型：1长期住宿 2临时住宿", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "入住类型不能为空")
    private Integer checkInType;

    @Schema(description = "校区编码")
    private String campusCode;

    @Schema(description = "楼层编码")
    private String floorCode;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间编码")
    private String roomCode;

    @Schema(description = "床位ID")
    private Long bedId;

    @Schema(description = "床位编码")
    private String bedCode;

    @Schema(description = "申请日期")
    private LocalDate applyDate;

    @Schema(description = "入住日期")
    private LocalDate checkInDate;

    @Schema(description = "预计退宿日期（临时住宿）")
    private LocalDate expectedCheckOutDate;

    @Schema(description = "状态：1待审核 2已通过 3已拒绝 4已入住")
    private Integer status;

    @Schema(description = "审核人ID")
    private Long approverId;

    @Schema(description = "审核人姓名")
    private String approverName;

    @Schema(description = "审核意见")
    private String approveOpinion;

    @Schema(description = "申请原因/备注")
    private String applyReason;

    @Schema(description = "备注")
    private String remark;
}
