package com.project.backend.room.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 宿舍完整层级树VO
 * 
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@Schema(description = "宿舍完整层级树")
public class DormHierarchyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "校区列表（包含完整的宿舍层级结构：校区-楼层-房间-床位）")
    private List<DormHierarchyNodeVO> campuses;
}
