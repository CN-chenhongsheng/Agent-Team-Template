package com.project.backend.common.task;

import com.project.core.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 分片上传临时目录清理任务
 *
 * @author 陈鸿昇
 * @since 2026-11-05
 */
@Slf4j
@Component
public class ChunkUploadCleanupTask {

    @Value("${chunk-upload.cleanup-enabled:true}")
    private boolean cleanupEnabled;

    @Value("${chunk-upload.temp-dir:./uploads/temp}")
    private String tempDir;

    @Value("${chunk-upload.expired-days:7}")
    private long expiredDays;

    /**
     * 定期删除超过保留期的分片目录
     */
    @Scheduled(cron = "${chunk-upload.cleanup-cron:0 0 3 * * ?}")
    public void cleanupExpiredChunks() {
        if (!cleanupEnabled) {
            return;
        }

        Path root = FileUtils.resolveTempDir(tempDir);
        if (!Files.isDirectory(root)) {
            return;
        }

        Instant threshold = Instant.now().minus(Math.max(1, expiredDays), ChronoUnit.DAYS);
        int cleaned = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path dir : stream) {
                if (!Files.isDirectory(dir)) {
                    continue;
                }
                try {
                    if (Files.getLastModifiedTime(dir).toInstant().isBefore(threshold)) {
                        deleteRecursively(dir);
                        cleaned++;
                    }
                } catch (IOException e) {
                    log.warn("清理分片目录失败：{}", dir, e);
                }
            }
        } catch (IOException e) {
            log.warn("扫描分片临时目录失败：{}", root, e);
        }

        if (cleaned > 0) {
            log.info("已清理 {} 个过期分片目录", cleaned);
        }
    }

    private void deleteRecursively(Path dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
