package com.project.backend.room.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 宿舍层级树节点VO
 * 用于返回完整的校区-楼层-房间-床位层级结构
 * 
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@Schema(description = "宿舍层级树节点")
public class DormHierarchyNodeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "节点ID")
    private Long id;

    @Schema(description = "节点编码")
    private String code;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "节点类型：campus-校区, floor-楼层, room-房间, bed-床位")
    private String type;

    @Schema(description = "父节点编码或ID")
    private String parentCode;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;

    @Schema(description = "床位状态（仅床位节点有效）：1空闲 2已占用 3维修中 4已预订")
    private Integer bedStatus;

    @Schema(description = "子节点列表")
    private List<DormHierarchyNodeVO> children;
}
