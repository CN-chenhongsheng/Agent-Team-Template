package com.project.backend.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "系统用户实体")
public class User extends BaseEntity {

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "密码（加密）")
    @TableField("password")
    private String password;

    @Schema(description = "昵称")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "头像URL")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "管理范围")
    @TableField("manage_scope")
    private String manageScope;

    @Schema(description = "状态：1正常 0停用")
    @TableField("status")
    private Integer status;

    @Schema(description = "性别（字典sys_user_sex）：0未知 1男 2女")
    @TableField("gender")
    private Integer gender;

    @Schema(description = "地址")
    @TableField("address")
    private String address;

    @Schema(description = "个人介绍")
    @TableField("introduction")
    private String introduction;

    @Schema(description = "企业微信ID")
    @TableField("cp_user_id")
    private String cpUserId;

    @Schema(description = "微信openid")
    @TableField("openid")
    private String openid;

    @Schema(description = "最后登录时间")
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
}
