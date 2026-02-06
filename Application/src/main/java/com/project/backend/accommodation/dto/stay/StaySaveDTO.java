package com.project.backend.accommodation.dto.stay;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 留宿管理保存DTO（新增或编辑）
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@Schema(description = "留宿管理保存请求")
public class StaySaveDTO {

    @Schema(description = "留宿记录ID（编辑时必传）")
    private Long id;

    @Schema(description = "学生ID（学生端提交可不传，从 token 获取；管理端编辑时必传）")
    private Long studentId;

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

    @Schema(description = "留宿开始日期")
    private LocalDate stayStartDate;

    @Schema(description = "留宿结束日期")
    private LocalDate stayEndDate;

    @Schema(description = "状态：1待审核 2已通过 3已拒绝 4已完成")
    private Integer status;

    @Schema(description = "审核人ID")
    private Long approverId;

    @Schema(description = "审核人姓名")
    private String approverName;

    @Schema(description = "审核意见")
    private String approveOpinion;

    @Schema(description = "留宿理由（必填）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "留宿理由不能为空")
    private String stayReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "家长姓名")
    private String parentName;

    @Schema(description = "家长电话")
    private String parentPhone;

    @Schema(description = "家长是否同意：agree-同意 disagree-不同意")
    private String parentAgree;

    @Schema(description = "本人签名图片URL数组")
    private List<String> signature;

    @Schema(description = "附件图片URL数组")
    private List<String> images;
}
