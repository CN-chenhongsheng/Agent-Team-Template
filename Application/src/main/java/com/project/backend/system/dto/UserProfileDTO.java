package com.project.backend.system.dto;

import com.project.core.constant.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户个人信息更新DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "用户个人信息更新请求")
public class UserProfileDTO {

    @Schema(description = "昵称")
    @Size(min = 2, max = 50, message = "昵称长度应在2-50个字符之间")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号")
    @Pattern(regexp = RegexConstants.MOBILE, message = "手机号格式不正确")
    private String phone;

    @Schema(description = "性别（字典sys_user_sex）：0未知 1男 2女")
    private Integer gender;

    @Schema(description = "地址")
    @Size(max = 255, message = "地址长度不能超过255个字符")
    private String address;

    @Schema(description = "个人介绍")
    @Size(max = 500, message = "个人介绍长度不能超过500个字符")
    private String introduction;
}
