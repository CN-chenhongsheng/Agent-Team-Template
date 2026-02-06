package com.project.backend.room.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 房间实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_room")
@Schema(description = "房间实体")
public class Room extends BaseEntity {

    @Schema(description = "房间编码")
    @TableField("room_code")
    private String roomCode;

    @Schema(description = "房间号")
    @TableField("room_number")
    private String roomNumber;

    @Schema(description = "所属楼层ID")
    @TableField("floor_id")
    private Long floorId;

    @Schema(description = "所属楼层数（冗余字段）")
    @TableField("floor_number")
    private Integer floorNumber;

    @Schema(description = "所属楼层编码（冗余字段）")
    @TableField("floor_code")
    private String floorCode;

    @Schema(description = "所属校区编码（冗余字段）")
    @TableField("campus_code")
    private String campusCode;

    @Schema(description = "房间类型（字典room_type）")
    @TableField("room_type")
    private String roomType;

    @Schema(description = "床位数（标准配置）")
    @TableField("bed_count")
    private Integer bedCount;

    @Schema(description = "当前入住人数")
    @TableField("current_occupancy")
    private Integer currentOccupancy;

    @Schema(description = "最大入住人数")
    @TableField("max_occupancy")
    private Integer maxOccupancy;

    @Schema(description = "房间面积（平方米）")
    @TableField("area")
    private BigDecimal area;

    @Schema(description = "是否有空调：1有 0无")
    @TableField("has_air_conditioner")
    private Integer hasAirConditioner;

    @Schema(description = "是否有独立卫生间：1有 0无")
    @TableField("has_bathroom")
    private Integer hasBathroom;

    @Schema(description = "是否有阳台：1有 0无")
    @TableField("has_balcony")
    private Integer hasBalcony;

    @Schema(description = "房间状态：1空闲 2已满 3维修中 4已预订")
    @TableField("room_status")
    private Integer roomStatus;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "状态：1启用 0停用")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
