package com.project.backend.room.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 楼层展示VO
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@Schema(description = "楼层信息响应")
public class FloorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "楼层编码")
    private String floorCode;

    @Schema(description = "楼层名称")
    private String floorName;

    @Schema(description = "楼层数（数字）")
    private Integer floorNumber;

    @Schema(description = "所属校区编码")
    private String campusCode;

    @Schema(description = "所属校区名称")
    private String campusName;

    @Schema(description = "适用性别：1男 2女 3混合")
    private Integer genderType;

    @Schema(description = "适用性别文本")
    private String genderTypeText;

    @Schema(description = "该楼层房间数（统计字段）")
    private Integer totalRooms;

    @Schema(description = "该楼层床位数（统计字段）")
    private Integer totalBeds;

    @Schema(description = "当前入住人数（统计字段）")
    private Integer currentOccupancy;

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
