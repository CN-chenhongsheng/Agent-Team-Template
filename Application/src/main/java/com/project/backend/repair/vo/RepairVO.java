package com.project.backend.repair.vo;

import com.project.backend.common.vo.StudentBasicInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报修管理VO
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@Schema(description = "报修管理VO")
public class RepairVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生基本信息（嵌套）")
    private StudentBasicInfoVO studentInfo;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间编码")
    private String roomCode;

    @Schema(description = "床位ID")
    private Long bedId;

    @Schema(description = "床位编码")
    private String bedCode;

    @Schema(description = "房间名称（报修房间）")
    private String roomName;

    @Schema(description = "床位名称（报修床位）")
    private String bedName;

    @Schema(description = "维修类型：1-水电 2-门窗 3-家具 4-网络 5-其他")
    private Integer repairType;

    @Schema(description = "维修类型文本")
    private String repairTypeText;

    @Schema(description = "故障描述")
    private String faultDescription;

    @Schema(description = "故障图片列表")
    private List<String> faultImages;

    @Schema(description = "紧急程度：1-一般 2-紧急 3-非常紧急")
    private Integer urgentLevel;

    @Schema(description = "紧急程度文本")
    private String urgentLevelText;

    @Schema(description = "状态：1-待接单 2-已接单 3-维修中 4-已完成 5-已取消")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "维修人员ID")
    private Long repairPersonId;

    @Schema(description = "维修人员姓名")
    private String repairPersonName;

    @Schema(description = "预约时间")
    private LocalDateTime appointmentTime;

    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    @Schema(description = "维修结果描述")
    private String repairResult;

    @Schema(description = "维修后图片列表")
    private List<String> repairImages;

    @Schema(description = "评分：1-5星")
    private Integer rating;

    @Schema(description = "评分文本")
    private String ratingText;

    @Schema(description = "评价内容")
    private String ratingComment;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "更新人ID")
    private Long updateBy;
}
