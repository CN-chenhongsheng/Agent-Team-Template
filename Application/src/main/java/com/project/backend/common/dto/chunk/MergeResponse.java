package com.project.backend.common.dto.chunk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分片合并响应（仅返回文件 URL，不触发导入）
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分片合并响应")
public class MergeResponse implements Serializable {

    @Schema(description = "文件访问 URL")
    private String url;

    @Schema(description = "文件名")
    private String name;

    @Schema(description = "文件大小（字节）")
    private Long size;
}
