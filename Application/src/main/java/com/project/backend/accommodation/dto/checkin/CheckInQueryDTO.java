package com.project.backend.accommodation.dto.checkin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 入住管理查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@Schema(description = "入住管理查询条件")
public class CheckInQueryDTO {

    @Schema(description = "学号（模糊查询）")
    private String studentNo;

    @Schema(description = "学生姓名（模糊查询）")
    private String studentName;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "入住类型：1长期住宿 2临时住宿")
    private Integer checkInType;

    @Schema(description = "校区编码")
    private String campusCode;

    @Schema(description = "床位ID")
    private Long bedId;

    @Schema(description = "状态：1待审核 2已通过 3已拒绝 4已入住")
    private Integer status;

    @Schema(description = "申请日期开始")
    private LocalDate applyDateStart;

    @Schema(description = "申请日期结束")
    private LocalDate applyDateEnd;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
