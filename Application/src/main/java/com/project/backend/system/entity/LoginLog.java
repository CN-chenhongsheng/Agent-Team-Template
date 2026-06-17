package com.project.backend.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登入日志实体
 * 
 * @author 陈鸿昇
 * @since 2026-03-08
 */
@Data
@TableName("sys_login_log")
@Schema(description = "登入日志实体")
public class LoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户账号")
    @TableField("username")
    private String username;

    @Schema(description = "登录类型：password-密码登录")
    @TableField("login_type")
    private String loginType;

    @Schema(description = "登录状态：0-失败 1-成功 2-登出")
    @TableField("login_status")
    private Integer loginStatus;

    @Schema(description = "IP地址")
    @TableField("ip_address")
    private String ipAddress;

    @Schema(description = "登录地点")
    @TableField("login_location")
    private String loginLocation;

    @Schema(description = "浏览器类型")
    @TableField("browser")
    private String browser;

    @Schema(description = "操作系统")
    @TableField("os")
    private String os;

    @Schema(description = "提示消息")
    @TableField("message")
    private String message;

    @Schema(description = "登录时间")
    @TableField("login_time")
    private LocalDateTime loginTime;
}
