package com.project.backend.accommodation.vo.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学生宿舍信息VO
 *
 * @author 陈鸿昇
 * @since 2026-01-16
 */
@Data
@Schema(description = "学生宿舍信息")
public class DormInfoVO {

    @Schema(description = "校区名称")
    private String campusName;

    @Schema(description = "楼栋名称")
    private String buildingName;

    @Schema(description = "楼层名称")
    private String floorName;

    @Schema(description = "房间编码")
    private String roomCode;

    @Schema(description = "床位编码")
    private String bedCode;

    @Schema(description = "入住日期")
    private LocalDate checkInDate;
}
