package com.project.core.util;

import com.project.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 文件工具测试
 */
@DisplayName("文件工具测试")
class FileUtilsTest {

    private final Path root = Path.of("uploads", "temp").toAbsolutePath().normalize();

    @Test
    @DisplayName("合法哈希会被规范化为小写")
    void validateFileHash_normalizesCase() {
        assertThat(FileUtils.validateFileHash("D41D8CD98F00B204E9800998ECF8427E"))
                .isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
    }

    @Test
    @DisplayName("非法哈希会被拒绝")
    void validateFileHash_rejectsTraversal() {
        assertThatThrownBy(() -> FileUtils.validateFileHash("../../etc/passwd"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> FileUtils.validateFileHash(null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("正常子路径会被解析到根目录内")
    void resolveWithin_resolvesSafeChild() {
        assertThat(FileUtils.resolveWithin(root, "chunk-0"))
                .isEqualTo(root.resolve("chunk-0"));
    }

    @Test
    @DisplayName("路径穿越会被拒绝")
    void resolveWithin_rejectsTraversal() {
        assertThatThrownBy(() -> FileUtils.resolveWithin(root, "../../etc/passwd"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> FileUtils.resolveWithin(root, "/etc/passwd"))
                .isInstanceOf(BusinessException.class);
    }
}
