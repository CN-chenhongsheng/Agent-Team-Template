package com.project.backend.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 操作日志查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "操作日志查询条件")
public class OperLogQueryDTO {

    @Schema(description = "操作模块（模糊查询）")
    private String title;

    @Schema(description = "操作人员（模糊查询）")
    private String operName;

    @Schema(description = "业务类型：其它 1新增 2修改 3删除")
    private Integer businessType;

    @Schema(description = "操作状态：0正常 1异常")
    private Integer status;

    @Schema(description = "操作时间开始")
    private String operTimeStart;

    @Schema(description = "操作时间结束")
    private String operTimeEnd;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
