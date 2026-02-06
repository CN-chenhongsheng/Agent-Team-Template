package com.project.backend.accommodation.dto.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 调宿管理保存DTO（新增或编辑）
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@Schema(description = "调宿管理保存请求")
public class TransferSaveDTO {

    @Schema(description = "调宿记录ID（编辑时必传）")
    private Long id;

    @Schema(description = "学生ID（学生端提交可不传，从 token 获取；管理端编辑时必传）")
    private Long studentId;

    @Schema(description = "原校区编码")
    private String originalCampusCode;

    @Schema(description = "原楼层编码")
    private String originalFloorCode;

    @Schema(description = "原房间ID")
    private Long originalRoomId;

    @Schema(description = "原房间编码")
    private String originalRoomCode;

    @Schema(description = "原床位ID")
    private Long originalBedId;

    @Schema(description = "原床位编码")
    private String originalBedCode;

    @Schema(description = "目标校区编码")
    private String targetCampusCode;

    @Schema(description = "目标楼层编码")
    private String targetFloorCode;

    @Schema(description = "目标房间ID")
    private Long targetRoomId;

    @Schema(description = "目标房间编码")
    private String targetRoomCode;

    @Schema(description = "目标床位ID")
    private Long targetBedId;

    @Schema(description = "目标床位编码")
    private String targetBedCode;

    @Schema(description = "申请日期")
    private LocalDate applyDate;

    @Schema(description = "调宿日期")
    private LocalDate transferDate;

    @Schema(description = "状态：1待审核 2已通过 3已拒绝 4已完成")
    private Integer status;

    @Schema(description = "审核人ID")
    private Long approverId;

    @Schema(description = "审核人姓名")
    private String approverName;

    @Schema(description = "审核意见")
    private String approveOpinion;

    @Schema(description = "调宿原因")
    private String transferReason;

    @Schema(description = "备注")
    private String remark;
}
