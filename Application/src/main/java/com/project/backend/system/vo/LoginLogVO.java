package com.project.backend.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登入日志VO
 * 
 * @author 陈鸿昇
 * @since 2026-03-08
 */
@Data
@Schema(description = "登入日志视图对象")
public class LoginLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志主键")
    private Long id;

    @Schema(description = "用户账号")
    private String username;

    @Schema(description = "登录类型")
    private String loginType;

    @Schema(description = "登录类型文本")
    private String loginTypeText;

    @Schema(description = "登录状态")
    private Integer loginStatus;

    @Schema(description = "登录状态文本")
    private String loginStatusText;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "登录地点")
    private String loginLocation;

    @Schema(description = "浏览器类型")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "提示消息")
    private String message;

    @Schema(description = "登录时间")
    private String loginTime;
}
