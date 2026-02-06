package com.project.backend.common.dto.chunk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分片上传预检请求
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@Schema(description = "分片上传预检请求")
public class PrecheckRequest implements Serializable {

    @Schema(description = "文件 MD5 哈希", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileHash;

    @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @Schema(description = "文件大小（字节）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long fileSize;

    @Schema(description = "分片总数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalChunks;
}
