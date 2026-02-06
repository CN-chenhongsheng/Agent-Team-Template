package com.project.backend.room.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房间展示VO
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@Schema(description = "房间信息响应")
public class RoomVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "房间编码")
    private String roomCode;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "所属楼层ID")
    private Long floorId;

    @Schema(description = "所属楼层数")
    private Integer floorNumber;

    @Schema(description = "所属楼层编码")
    private String floorCode;

    @Schema(description = "所属楼层名称")
    private String floorName;

    @Schema(description = "所属校区编码")
    private String campusCode;

    @Schema(description = "所属校区名称")
    private String campusName;

    @Schema(description = "房间类型（字典room_type）")
    private String roomType;

    @Schema(description = "房间类型文本")
    private String roomTypeText;

    @Schema(description = "床位数（标准配置）")
    private Integer bedCount;

    @Schema(description = "实际关联的床位数")
    private Integer totalBeds;

    @Schema(description = "当前入住人数")
    private Integer currentOccupancy;

    @Schema(description = "最大入住人数")
    private Integer maxOccupancy;

    @Schema(description = "房间面积（平方米）")
    private BigDecimal area;

    @Schema(description = "是否有空调：1有 0无")
    private Integer hasAirConditioner;

    @Schema(description = "是否有独立卫生间：1有 0无")
    private Integer hasBathroom;

    @Schema(description = "是否有阳台：1有 0无")
    private Integer hasBalcony;

    @Schema(description = "房间状态：1空闲 2已满 3维修中 4已预订")
    private Integer roomStatus;

    @Schema(description = "房间状态文本")
    private String roomStatusText;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
