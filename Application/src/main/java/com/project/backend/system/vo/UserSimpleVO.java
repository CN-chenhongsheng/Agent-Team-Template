package com.project.backend.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户简单信息VO
 * 用于角色用户查询等场景，只包含基本信息
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Data
@Schema(description = "用户简单信息")
public class UserSimpleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;
}
