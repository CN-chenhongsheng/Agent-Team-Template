package com.project.backend.accommodation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.backend.common.vo.StudentBasicInfoVO;
import com.project.core.vo.ApprovalProgress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 调宿管理展示VO
 *
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@Schema(description = "调宿管理信息响应")
public class TransferVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生基本信息（嵌套）")
    private StudentBasicInfoVO studentInfo;

    @Schema(description = "原校区编码")
    private String originalCampusCode;

    @Schema(description = "原校区名称")
    private String originalCampusName;

    @Schema(description = "原楼层编码")
    private String originalFloorCode;

    @Schema(description = "原楼层名称")
    private String originalFloorName;

    @Schema(description = "原房间ID")
    private Long originalRoomId;

    @Schema(description = "原房间编码")
    private String originalRoomCode;

    @Schema(description = "原房间名称")
    private String originalRoomName;

    @Schema(description = "原床位ID")
    private Long originalBedId;

    @Schema(description = "原床位编码")
    private String originalBedCode;

    @Schema(description = "原床位名称")
    private String originalBedName;

    @Schema(description = "目标校区编码")
    private String targetCampusCode;

    @Schema(description = "目标校区名称")
    private String targetCampusName;

    @Schema(description = "目标楼层编码")
    private String targetFloorCode;

    @Schema(description = "目标楼层名称")
    private String targetFloorName;

    @Schema(description = "目标房间ID")
    private Long targetRoomId;

    @Schema(description = "目标房间编码")
    private String targetRoomCode;

    @Schema(description = "目标房间名称")
    private String targetRoomName;

    @Schema(description = "目标床位ID")
    private Long targetBedId;

    @Schema(description = "目标床位编码")
    private String targetBedCode;

    @Schema(description = "目标床位名称")
    private String targetBedName;

    @Schema(description = "申请日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyDate;

    @Schema(description = "调宿日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transferDate;

    @Schema(description = "状态：1待审核 2已通过 3已拒绝 4已完成")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "审批实例ID")
    private Long approvalInstanceId;

    @Schema(description = "审批进度信息")
    private ApprovalProgress approvalProgress;

    @Schema(description = "审核人ID")
    private Long approverId;

    @Schema(description = "审核人姓名")
    private String approverName;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    @Schema(description = "审核意见")
    private String approveOpinion;

    @Schema(description = "调宿原因")
    private String transferReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
