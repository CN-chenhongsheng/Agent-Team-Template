package com.project.backend.repair.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报修保存DTO
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@Schema(description = "报修保存DTO")
public class RepairSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID（编辑时必传）")
    private Long id;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间编码")
    private String roomCode;

    @Schema(description = "床位ID")
    private Long bedId;

    @Schema(description = "床位编码")
    private String bedCode;

    @Schema(description = "维修类型：1-水电 2-门窗 3-家具 4-网络 5-其他", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "维修类型不能为空")
    private Integer repairType;

    @Schema(description = "故障描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "故障描述不能为空")
    private String faultDescription;

    @Schema(description = "故障图片列表")
    private List<String> faultImages;

    @Schema(description = "紧急程度：1-一般 2-紧急 3-非常紧急", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "紧急程度不能为空")
    private Integer urgentLevel;

    @Schema(description = "状态：1-待接单 2-已接单 3-维修中 4-已完成 5-已取消")
    private Integer status;

    @Schema(description = "维修人员ID")
    private Long repairPersonId;

    @Schema(description = "维修人员姓名")
    private String repairPersonName;

    @Schema(description = "预约时间")
    private LocalDateTime appointmentTime;

    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    @Schema(description = "维修结果描述")
    private String repairResult;

    @Schema(description = "维修后图片列表")
    private List<String> repairImages;

    @Schema(description = "评分：1-5星")
    private Integer rating;

    @Schema(description = "评价内容")
    private String ratingComment;

    @Schema(description = "备注")
    private String remark;
}
