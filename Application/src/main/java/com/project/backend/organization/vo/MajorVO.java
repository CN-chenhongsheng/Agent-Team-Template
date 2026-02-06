package com.project.backend.organization.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专业展示VO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@Schema(description = "专业信息响应")
public class MajorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "专业编码")
    private String majorCode;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "所属院系编码")
    private String deptCode;

    @Schema(description = "所属院系名称")
    private String deptName;

    @Schema(description = "专业负责人")
    private String director;

    @Schema(description = "学位类型（字典degree_type）")
    private String type;

    @Schema(description = "学位类型文本")
    private String typeText;

    @Schema(description = "学制")
    private String duration;

    @Schema(description = "培养目标")
    private String goal;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
