package com.project.backend.common.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.project.backend.common.dto.chunk.MergeRequest;
import com.project.backend.common.dto.chunk.MergeResponse;
import com.project.backend.common.dto.chunk.PrecheckRequest;
import com.project.backend.common.dto.chunk.PrecheckResponse;
import com.project.core.exception.BusinessException;
import com.project.core.result.R;
import com.project.core.util.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 分片上传控制器
 * 仅负责合并文件并返回文件 URL，不触发导入
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Slf4j
@RestController("backendChunkUploadController")
@RequestMapping("/v1/upload")
@RequiredArgsConstructor
@Tag(name = "分片上传", description = "大文件分片上传：预检、分片上传、合并、校验、取消")
public class ChunkUploadController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.public-path:/v1/common/files}")
    private String publicPath;

    @Value("${chunk-upload.temp-dir:./uploads/temp}")
    private String tempDir;

    @Value("${chunk-upload.max-chunk-size:10485760}")
    private long maxChunkSize;

    /**
     * 单个文件允许的最大分片数，防止恶意请求耗尽磁盘
     */
    private static final int MAX_CHUNKS = 10_000;

    /**
     * 获取临时目录的绝对路径
     */
    private Path getTempDirPath() {
        return FileUtils.resolveTempDir(tempDir);
    }

    /**
     * 获取上传目录的绝对路径
     */
    private Path getUploadDirPath() {
        return FileUtils.resolveUploadDir(uploadDir);
    }

    /**
     * 预检：秒传或返回已上传分片列表
     */
    @PostMapping("/precheck")
    @SaCheckPermission(value = "system:user:import", orRole = "SUPER_ADMIN")
    @Operation(summary = "预检", description = "检查是否可秒传或返回已上传分片列表")
    public R<PrecheckResponse> precheck(@RequestBody PrecheckRequest request) {
        String fileHash = FileUtils.validateFileHash(request.getFileHash());
        String fileName = request.getFileName();
        if (fileName == null || fileName.isEmpty()) {
            throw new BusinessException("fileName 不能为空");
        }
        FileUtils.sanitizeFileName(fileName);
        if (request.getTotalChunks() != null
                && (request.getTotalChunks() <= 0 || request.getTotalChunks() > MAX_CHUNKS)) {
            throw new BusinessException("totalChunks 不合法");
        }

        Path tempPath = FileUtils.resolveWithin(getTempDirPath(), fileHash);
        if (!Files.exists(tempPath) || !Files.isDirectory(tempPath)) {
            return R.ok(PrecheckResponse.builder()
                    .canSkip(false)
                    .uploadedChunks(List.of())
                    .build());
        }

        List<Integer> uploaded = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempPath)) {
            for (Path p : stream) {
                if (Files.isRegularFile(p)) {
                    String name = p.getFileName().toString();
                    try {
                        int idx = Integer.parseInt(name);
                        uploaded.add(idx);
                    } catch (NumberFormatException ignored) {
                        // ignore non-numeric files
                    }
                }
            }
        } catch (IOException e) {
            log.warn("预检读取分片列表失败: {}", e.getMessage());
        }
        uploaded.sort(Integer::compareTo);
        return R.ok(PrecheckResponse.builder()
                .canSkip(false)
                .uploadedChunks(uploaded)
                .build());
    }

    /**
     * 上传单个分片
     */
    @PostMapping("/chunk")
    @SaCheckPermission(value = "system:user:import", orRole = "SUPER_ADMIN")
    @Operation(summary = "上传分片", description = "上传单个分片")
    public R<Void> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileHash") String fileHash,
            @RequestParam("chunkIndex") String chunkIndexStr,
            @RequestParam("totalChunks") String totalChunksStr) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("分片文件不能为空");
        }
        int chunkIndex;
        int totalChunks;
        try {
            chunkIndex = Integer.parseInt(chunkIndexStr.trim());
            totalChunks = Integer.parseInt(totalChunksStr.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("chunkIndex 和 totalChunks 必须为数字");
        }
        if (totalChunks <= 0 || totalChunks > MAX_CHUNKS) {
            throw new BusinessException("totalChunks 不合法");
        }
        if (chunkIndex < 0 || chunkIndex >= totalChunks) {
            throw new BusinessException("chunkIndex 不合法");
        }
        if (file.getSize() > maxChunkSize) {
            throw new BusinessException("分片大小超出限制");
        }

        Path dir = FileUtils.resolveWithin(getTempDirPath(), FileUtils.validateFileHash(fileHash));
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BusinessException("创建临时目录失败: " + e.getMessage());
        }
        Path chunkPath = dir.resolve(String.valueOf(chunkIndex));
        try {
            file.transferTo(chunkPath.toFile());
        } catch (IOException e) {
            log.error("保存分片失败: {}", e.getMessage());
            throw new BusinessException("保存分片失败: " + e.getMessage());
        }
        return R.ok();
    }

    /**
     * 合并分片，保存到最终目录，仅返回文件 URL，不触发导入
     */
    @PostMapping("/merge")
    @SaCheckPermission(value = "system:user:import", orRole = "SUPER_ADMIN")
    @Operation(summary = "合并分片", description = "合并分片并返回文件 URL，不触发导入")
    public R<MergeResponse> merge(
            @RequestBody MergeRequest request,
            HttpServletRequest httpRequest) {
        String fileHash = FileUtils.validateFileHash(request.getFileHash());
        String fileName = request.getFileName();
        Integer totalChunks = request.getTotalChunks();
        if (fileName == null || fileName.isEmpty()) {
            throw new BusinessException("fileName 不能为空");
        }
        if (totalChunks == null || totalChunks <= 0 || totalChunks > MAX_CHUNKS) {
            throw new BusinessException("totalChunks 不合法");
        }
        String safeName = FileUtils.sanitizeFileName(fileName);

        Path tempPath = FileUtils.resolveWithin(getTempDirPath(), fileHash);
        if (!Files.exists(tempPath) || !Files.isDirectory(tempPath)) {
            throw new BusinessException("未找到分片目录，请先上传全部分片");
        }

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Path baseDir = getUploadDirPath();
        Path finalDir = baseDir.resolve("uploads").resolve(dateDir);
        try {
            Files.createDirectories(finalDir);
        } catch (IOException e) {
            throw new BusinessException("创建目标目录失败: " + e.getMessage());
        }
        Path targetFile = FileUtils.resolveWithin(finalDir, safeName);

        try (RandomAccessFile out = new RandomAccessFile(targetFile.toFile(), "rw")) {
            // 同名文件重复上传时先清空旧内容，避免残留旧文件尾部
            out.setLength(0);
            for (int i = 0; i < totalChunks; i++) {
                Path chunkFile = tempPath.resolve(String.valueOf(i));
                if (!Files.exists(chunkFile)) {
                    throw new BusinessException("缺少分片: " + i);
                }
                byte[] buf = Files.readAllBytes(chunkFile);
                out.write(buf);
            }
        } catch (IOException e) {
            log.error("合并分片失败: {}", e.getMessage());
            try {
                Files.deleteIfExists(targetFile);
            } catch (IOException ignored) {
            }
            throw new BusinessException("合并分片失败: " + e.getMessage());
        }

        // 删除临时分片目录
        try {
            for (int i = 0; i < totalChunks; i++) {
                Files.deleteIfExists(tempPath.resolve(String.valueOf(i)));
            }
            Files.deleteIfExists(tempPath);
        } catch (IOException e) {
            log.warn("清理临时分片失败: {}", e.getMessage());
        }

        long size = targetFile.toFile().length();
        String relativePath = "uploads/" + dateDir + "/" + safeName;
        String contextPath = httpRequest.getContextPath() != null ? httpRequest.getContextPath() : "";
        String url = contextPath + publicPath + "/" + relativePath;
        // 若前端需要完整 URL，可在此拼接 scheme+host+port；当前返回相对路径供代理使用
        MergeResponse response = MergeResponse.builder()
                .url(url)
                .name(fileName)
                .size(size)
                .build();
        return R.ok(response);
    }

    /**
     * 校验已上传分片
     */
    @PostMapping("/verify")
    @SaCheckPermission(value = "system:user:import", orRole = "SUPER_ADMIN")
    @Operation(summary = "校验分片", description = "返回服务端实际存在的分片索引列表")
    public R<List<Integer>> verify(@RequestBody PrecheckRequest request) {
        String fileHash = FileUtils.validateFileHash(request.getFileHash());
        Path tempPath = FileUtils.resolveWithin(getTempDirPath(), fileHash);
        List<Integer> list = new ArrayList<>();
        if (Files.exists(tempPath) && Files.isDirectory(tempPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempPath)) {
                for (Path p : stream) {
                    if (Files.isRegularFile(p)) {
                        try {
                            list.add(Integer.parseInt(p.getFileName().toString()));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("校验读取分片列表失败: {}", e.getMessage());
            }
        }
        list.sort(Integer::compareTo);
        return R.ok(list);
    }

    /**
     * 取消上传，清理临时分片
     */
    @PostMapping("/abort")
    @SaCheckPermission(value = "system:user:import", orRole = "SUPER_ADMIN")
    @Operation(summary = "取消上传", description = "清理该文件的临时分片")
    public R<Void> abort(@RequestBody PrecheckRequest request) {
        String fileHash = FileUtils.validateFileHash(request.getFileHash());
        Path tempPath = FileUtils.resolveWithin(getTempDirPath(), fileHash);
        if (Files.exists(tempPath) && Files.isDirectory(tempPath)) {
            try (Stream<Path> paths = Files.list(tempPath)) {
                for (Path p : paths.toList()) {
                    Files.deleteIfExists(p);
                }
                Files.deleteIfExists(tempPath);
            } catch (IOException e) {
                log.warn("清理临时分片失败: {}", e.getMessage());
            }
        }
        return R.ok();
    }

}
