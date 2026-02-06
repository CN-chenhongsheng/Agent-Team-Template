package com.project.backend.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "用户查询条件")
public class UserQueryDTO {

    @Schema(description = "用户名（模糊查询）")
    private String username;

    @Schema(description = "昵称（模糊查询）")
    private String nickname;

    @Schema(description = "手机号（模糊查询）")
    private String phone;

    @Schema(description = "管理范围")
    private String manageScope;

    @Schema(description = "状态：1正常 0停用")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
