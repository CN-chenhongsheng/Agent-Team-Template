package com.project.backend.organization.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 班级展示VO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@Schema(description = "班级信息响应")
public class ClassVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "班级编码")
    private String classCode;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "所属专业编码")
    private String majorCode;

    @Schema(description = "所属专业名称")
    private String majorName;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "负责人姓名（冗余字段）")
    private String teacherName;

    @Schema(description = "负责人ID（关联sys_user）")
    private Long teacherId;

    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    @Schema(description = "当前人数")
    private Integer currentCount;

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
