package com.project.backend.room.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 床位实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_bed")
@Schema(description = "床位实体")
public class Bed extends BaseEntity {

    @Schema(description = "床位编码")
    @TableField("bed_code")
    private String bedCode;

    @Schema(description = "床位号")
    @TableField("bed_number")
    private String bedNumber;

    @Schema(description = "所属房间ID")
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "所属房间编码（冗余字段）")
    @TableField("room_code")
    private String roomCode;

    @Schema(description = "所属楼层ID（冗余字段）")
    @TableField("floor_id")
    private Long floorId;

    @Schema(description = "所属楼层编码（冗余字段）")
    @TableField("floor_code")
    private String floorCode;

    @Schema(description = "所属校区编码（冗余字段）")
    @TableField("campus_code")
    private String campusCode;

    @Schema(description = "床位位置（字典bed_position）")
    @TableField("bed_position")
    private String bedPosition;

    @Schema(description = "床位状态：1空闲 2已占用 3维修中 4已预订")
    @TableField("bed_status")
    private Integer bedStatus;

    @Schema(description = "当前入住学生ID")
    @TableField("student_id")
    private Long studentId;

    @Schema(description = "当前入住学生姓名（冗余字段）")
    @TableField("student_name")
    private String studentName;

    @Schema(description = "入住日期")
    @TableField("check_in_date")
    private LocalDate checkInDate;

    @Schema(description = "退宿日期")
    @TableField("check_out_date")
    private LocalDate checkOutDate;

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
