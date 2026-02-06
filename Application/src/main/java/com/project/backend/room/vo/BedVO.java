package com.project.backend.room.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.backend.common.vo.StudentBasicInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 床位展示VO
 *
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@Schema(description = "床位信息响应")
public class BedVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "床位编码")
    private String bedCode;

    @Schema(description = "床位号")
    private String bedNumber;

    @Schema(description = "所属房间ID")
    private Long roomId;

    @Schema(description = "所属房间编码")
    private String roomCode;

    @Schema(description = "所属房间号")
    private String roomNumber;

    @Schema(description = "所属楼层ID")
    private Long floorId;

    @Schema(description = "所属楼层编码")
    private String floorCode;

    @Schema(description = "所属楼层名称")
    private String floorName;

    @Schema(description = "所属校区编码")
    private String campusCode;

    @Schema(description = "所属校区名称")
    private String campusName;

    @Schema(description = "床位位置（字典bed_position）")
    private String bedPosition;

    @Schema(description = "床位位置文本")
    private String bedPositionText;

    @Schema(description = "床位状态：1空闲 2已占用 3维修中 4已预订")
    private Integer bedStatus;

    @Schema(description = "床位状态文本")
    private String bedStatusText;

    @Schema(description = "当前入住学生ID")
    private Long studentId;

    @Schema(description = "学生基本信息（嵌套，有学生入住时返回）")
    private StudentBasicInfoVO studentInfo;

    @Schema(description = "入住日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @Schema(description = "退宿日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

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
