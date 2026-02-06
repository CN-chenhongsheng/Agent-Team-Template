package com.project.backend.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 学生登录请求DTO
 *
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "学生登录请求参数")
public class StudentLoginDTO {

    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号", example = "2021001")
    private String studentNo;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;
}
