package com.project.backend.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求DTO
 *
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "微信登录请求参数")
public class WxLoginDTO {

    @NotBlank(message = "微信授权码不能为空")
    @Schema(description = "微信授权码", example = "0x1234567890abcdef")
    private String code;
}
