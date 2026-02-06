package com.project.backend.organization.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 院系展示VO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@Schema(description = "院系信息响应")
public class DepartmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "院系编码")
    private String deptCode;

    @Schema(description = "院系名称")
    private String deptName;

    @Schema(description = "所属校区编码")
    private String campusCode;

    @Schema(description = "所属校区名称")
    private String campusName;

    @Schema(description = "院系领导")
    private String leader;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "排序")
    private Integer sort;

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
