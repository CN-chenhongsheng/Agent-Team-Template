package com.project.backend.accommodation.vo.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 室友信息VO
 *
 * @author 陈鸿昇
 * @since 2026-01-16
 */
@Data
@Schema(description = "室友信息")
public class RoommateVO {

    @Schema(description = "学生ID")
    private Long id;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "姓名")
    private String studentName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "床位编码")
    private String bedCode;
}
