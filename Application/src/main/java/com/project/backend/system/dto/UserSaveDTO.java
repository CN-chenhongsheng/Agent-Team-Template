package com.project.backend.system.dto;

import com.project.core.constant.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户保存DTO（新增或编辑）
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "用户保存请求")
public class UserSaveDTO {

    @Schema(description = "用户ID（编辑时必传）")
    private Long id;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = RegexConstants.USERNAME, message = "用户名只能包含字母、数字、下划线，长度应在4-20个字符之间")
    private String username;

    @Schema(description = "密码（新增时必传，编辑时不传则不修改）")
    @Size(min = 6, max = 20, message = "密码长度应在6-20个字符之间")
    private String password;

    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号")
    @Pattern(regexp = RegexConstants.MOBILE, message = "手机号格式不正确")
    private String phone;

    @Schema(description = "状态：1正常 0停用")
    @Min(value = 0, message = "状态值只能为0或1")
    @Max(value = 1, message = "状态值只能为0或1")
    private Integer status;

    @Schema(description = "性别（字典sys_user_sex）：0未知 1男 2女")
    @Min(value = 0, message = "性别值只能为0、1或2")
    @Max(value = 2, message = "性别值只能为0、1或2")
    private Integer gender;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "个人介绍")
    private String introduction;

    @Schema(description = "角色ID列表")
    private Long[] roleIds;
}
