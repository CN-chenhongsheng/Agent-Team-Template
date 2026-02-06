package com.project.backend.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 学校完整层级树VO
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Data
@Schema(description = "学校完整层级树")
public class SchoolHierarchyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "校区列表（包含完整的层级结构）")
    private List<SchoolHierarchyNodeVO> campuses;
}
