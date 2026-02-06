package com.project.backend.student.dto.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 导入文件请求
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@Schema(description = "导入文件请求")
public class ImportFileRequest implements Serializable {

    @Schema(description = "文件访问 URL（由分片上传 merge 返回）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;

    @Schema(description = "前端扫描得到的有效行数（可选，用于精确计算进度）")
    private Integer totalRows;
}
