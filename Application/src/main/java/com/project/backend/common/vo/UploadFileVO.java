package com.project.backend.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件上传返回对象
 *
 * @author 陈鸿昇
 * @since 2026-01-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传返回对象")
public class UploadFileVO implements Serializable {

    @Schema(description = "文件访问URL")
    private String url;

    @Schema(description = "原始文件名")
    private String name;

    @Schema(description = "文件大小（字节）")
    private Long size;
}
