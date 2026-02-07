package com.project.backend.system.dto;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "操作日志查询条件")
public class OperLogQueryDTO extends BaseQueryDTO {

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
}
