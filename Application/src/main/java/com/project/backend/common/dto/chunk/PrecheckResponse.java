package com.project.backend.common.dto.chunk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分片上传预检响应
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分片上传预检响应")
public class PrecheckResponse implements Serializable {

    @Schema(description = "是否可以秒传（已存在完整文件）")
    private Boolean canSkip;

    @Schema(description = "已上传的分片索引列表")
    private List<Integer> uploadedChunks;

    @Schema(description = "秒传时返回的文件访问 URL")
    private String fileUrl;
}
