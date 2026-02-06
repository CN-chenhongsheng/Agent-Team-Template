package com.project.backend.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 角色用户查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "角色用户查询参数")
public class RoleUserQueryDTO {

    @Schema(description = "角色代码列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"COUNSELOR\", \"TEST_ROLE\"]")
    @NotEmpty(message = "角色代码列表不能为空")
    private List<String> roleCodes;
}
