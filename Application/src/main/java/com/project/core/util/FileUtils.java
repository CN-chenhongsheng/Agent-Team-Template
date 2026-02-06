package com.project.core.util;

import com.project.core.constant.RegexConstants;
import com.project.core.exception.BusinessException;
import cn.hutool.core.util.StrUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * 文件工具类
 * 提供文件名安全校验、路径解析等通用方法
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
public class FileUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private FileUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 安全文件名正则表达式 Pattern（编译后的，性能更好）
     */
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile(RegexConstants.SAFE_FILENAME);

    /**
     * 检查文件名是否安全
     *
     * @param fileName 文件名
     * @return true 安全，false 不安全或为空
     */
    public static boolean isSafeFileName(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return false;
        }
        // 提取纯文件名（去除路径）
        String name = extractFileName(fileName);
        return SAFE_FILENAME_PATTERN.matcher(name).matches();
    }

    /**
     * 清理文件名，确保安全
     * 如果文件名不合法，会抛出 BusinessException
     *
     * @param fileName 原始文件名（可能包含路径）
     * @return 清理后的安全文件名
     * @throws BusinessException 如果文件名为空或清理后仍不合法
     */
    public static String sanitizeFileName(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            throw new BusinessException("文件名为空");
        }

        // 提取纯文件名（去除路径分隔符）
        String name = extractFileName(fileName);

        // 检查是否包含路径遍历字符
        if (name.contains("..")) {
            throw new BusinessException("非法文件名");
        }

        // 如果文件名不符合安全规则，替换非法字符
        if (!SAFE_FILENAME_PATTERN.matcher(name).matches()) {
            name = name.replaceAll("[^a-zA-Z0-9._\\-\\u4e00-\\u9fa5]", "_");
        }

        // 清理后仍为空则抛出异常
        if (name.isEmpty()) {
            throw new BusinessException("非法文件名");
        }

        return name;
    }

    /**
     * 从完整路径中提取文件名
     *
     * @param filePath 文件路径（可能包含 / 或 \）
     * @return 纯文件名
     */
    private static String extractFileName(String filePath) {
        String name = filePath;
        // 处理 Unix/Linux 路径分隔符
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        // 处理 Windows 路径分隔符
        int lastBackslash = name.lastIndexOf('\\');
        if (lastBackslash >= 0) {
            name = name.substring(lastBackslash + 1);
        }
        return name;
    }

    /**
     * 解析上传目录路径
     * 将相对路径或绝对路径字符串转换为标准化的 Path 对象
     *
     * @param uploadDir 上传目录路径字符串（如 "./uploads" 或 "/var/uploads"）
     * @return 标准化后的绝对路径 Path
     */
    public static Path resolveUploadDir(String uploadDir) {
        if (StrUtil.isBlank(uploadDir)) {
            throw new BusinessException("上传目录配置不能为空");
        }
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * 解析临时目录路径
     * 将相对路径或绝对路径字符串转换为标准化的 Path 对象
     *
     * @param tempDir 临时目录路径字符串（如 "./uploads/temp" 或 "/tmp"）
     * @return 标准化后的绝对路径 Path
     */
    public static Path resolveTempDir(String tempDir) {
        if (StrUtil.isBlank(tempDir)) {
            throw new BusinessException("临时目录配置不能为空");
        }
        return Paths.get(tempDir).toAbsolutePath().normalize();
    }
}
