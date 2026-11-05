package com.project.backend.system.dto;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登入日志查询DTO
 *
 * @author 陈鸿昇
 * @since 2026-03-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "登入日志查询参数")
public class LoginLogQueryDTO extends BaseQueryDTO {

    @Schema(description = "用户账号")
    private String username;

    @Schema(description = "登录类型：password-密码登录")
    private String loginType;

    @Schema(description = "登录状态：0-失败 1-成功 2-登出")
    private Integer loginStatus;

    @Schema(description = "开始时间（格式：yyyy-MM-dd）")
    private String startTime;

    @Schema(description = "结束时间（格式：yyyy-MM-dd）")
    private String endTime;
}
