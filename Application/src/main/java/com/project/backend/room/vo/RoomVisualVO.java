package com.project.backend.room.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 房间可视化展示VO（含床位列表）
 * 用于可视化平面图展示，包含房间信息和床位列表
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "房间可视化信息响应（含床位列表）")
public class RoomVisualVO extends RoomVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "床位列表（含学生信息）")
    private List<BedVO> beds;

    @Schema(description = "适用性别：1男 2女 3混合（来自楼层）")
    private Integer genderType;

    @Schema(description = "适用性别文本")
    private String genderTypeText;
}
