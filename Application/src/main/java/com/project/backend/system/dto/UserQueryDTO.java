package com.project.backend.system.dto;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询条件")
public class UserQueryDTO extends BaseQueryDTO {

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
}
