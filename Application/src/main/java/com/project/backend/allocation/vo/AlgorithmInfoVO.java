package com.project.backend.allocation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 算法信息VO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "算法信息")
public class AlgorithmInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "算法类型标识")
    private String type;

    @Schema(description = "算法名称")
    private String name;

    @Schema(description = "算法描述")
    private String description;

    @Schema(description = "算法优点")
    private String advantages;

    @Schema(description = "算法缺点")
    private String disadvantages;

    @Schema(description = "预估耗时说明")
    private String estimatedTime;

    @Schema(description = "是否推荐")
    private Boolean recommended;
}
