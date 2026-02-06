package com.project.backend.room.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 楼层实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_floor")
@Schema(description = "楼层实体")
public class Floor extends BaseEntity {

    @Schema(description = "楼层编码")
    @TableField("floor_code")
    private String floorCode;

    @Schema(description = "楼层名称")
    @TableField("floor_name")
    private String floorName;

    @Schema(description = "楼层数（数字）")
    @TableField("floor_number")
    private Integer floorNumber;

    @Schema(description = "所属校区编码")
    @TableField("campus_code")
    private String campusCode;

    @Schema(description = "适用性别：1男 2女 3混合")
    @TableField("gender_type")
    private Integer genderType;

    @Schema(description = "该楼层房间数（统计字段）")
    @TableField("total_rooms")
    private Integer totalRooms;

    @Schema(description = "该楼层床位数（统计字段）")
    @TableField("total_beds")
    private Integer totalBeds;

    @Schema(description = "当前入住人数（统计字段）")
    @TableField("current_occupancy")
    private Integer currentOccupancy;

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
