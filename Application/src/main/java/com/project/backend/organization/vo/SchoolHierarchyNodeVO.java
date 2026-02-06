package com.project.backend.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 学校层级树节点VO
 * 用于返回完整的校区-院系-专业-班级层级结构
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Data
@Schema(description = "学校层级树节点")
public class SchoolHierarchyNodeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "节点ID")
    private Long id;

    @Schema(description = "节点编码")
    private String code;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "节点类型：campus-校区, department-院系, major-专业, class-班级")
    private String type;

    @Schema(description = "父节点编码")
    private String parentCode;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;

    @Schema(description = "子节点列表")
    private List<SchoolHierarchyNodeVO> children;
}
