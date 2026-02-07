package com.project.backend.accommodation.dto.stay;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 留宿管理查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "留宿管理查询条件")
public class StayQueryDTO extends BaseQueryDTO {

    @Schema(description = "学号（模糊查询）")
    private String studentNo;

    @Schema(description = "学生姓名（模糊查询）")
    private String studentName;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "校区编码")
    private String campusCode;

    @Schema(description = "床位ID")
    private Long bedId;

    @Schema(description = "状态：1待审核 2已通过 3已拒绝 4已完成")
    private Integer status;

    @Schema(description = "申请日期开始")
    private LocalDate applyDateStart;

    @Schema(description = "申请日期结束")
    private LocalDate applyDateEnd;

    @Schema(description = "留宿开始日期开始")
    private LocalDate stayStartDateStart;

    @Schema(description = "留宿开始日期结束")
    private LocalDate stayStartDateEnd;
}
