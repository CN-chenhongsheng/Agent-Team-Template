package com.project.backend.common.controller;

import com.project.backend.common.vo.UploadFileVO;
import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 公共文件上传控制器
 *
 * @author 陈鸿昇
 * @since 2026-01-25
 */
@Slf4j
@Tag(name = "公共文件管理", description = "公共文件上传与访问")
@RestController
@RequestMapping("/v1/common/files")
public class CommonFileController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.public-path:/v1/common/files}")
    private String publicPath;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 通用图片上传（支持多文件）
     *
     * @param files   文件数组
     * @param request HTTP 请求
     * @return 上传结果列表
     */
    @Operation(summary = "上传图片（支持多文件）")
    @PostMapping("/upload")
    public R<List<UploadFileVO>> upload(
            @RequestParam("file") MultipartFile[] files,
            HttpServletRequest request
    ) {
        // 1. 校验文件数组
        if (files == null || files.length == 0) {
            return R.fail("文件不能为空");
        }

        List<UploadFileVO> resultList = new ArrayList<>();
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        for (MultipartFile file : files) {
            try {
                // 2. 校验单个文件
                if (file == null || file.isEmpty()) {
                    continue; // 跳过空文件
                }

                // 3. 校验文件类型
                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                    return R.fail("仅支持 PNG 和 JPEG 格式图片");
                }

                // 4. 校验文件大小
                if (file.getSize() > MAX_FILE_SIZE) {
                    return R.fail("文件大小不能超过 5MB");
                }

                // 5. 生成文件名：按日期目录 + UUID
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                } else {
                    // 根据 contentType 设置默认扩展名
                    if ("image/png".equals(contentType)) {
                        extension = ".png";
                    } else if ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType)) {
                        extension = ".jpg";
                    }
                }

                String filename = UUID.randomUUID().toString() + extension;
                String relativePath = "uploads/" + dateDir + "/" + filename;

                // 6. 创建目录并保存文件
                Path baseDir = com.project.core.util.FileUtils.resolveUploadDir(uploadDir);
                Path targetDir = baseDir.resolve("uploads").resolve(dateDir);

                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }

                Path targetFile = targetDir.resolve(filename);
                file.transferTo(targetFile.toFile());

                // 7. 生成完整访问 URL
                String baseUrl = getBaseUrl(request);
                String fileUrl = baseUrl + publicPath + "/" + relativePath;

                log.info("图片上传成功，访问URL：{}", fileUrl);

                // 8. 构建返回对象
                UploadFileVO vo = UploadFileVO.builder()
                        .url(fileUrl)
                        .name(originalFilename)
                        .size(file.getSize())
                        .build();
                resultList.add(vo);

            } catch (IOException e) {
                log.error("图片上传失败", e);
                return R.fail("文件上传失败：" + e.getMessage());
            } catch (Exception e) {
                log.error("图片上传失败", e);
                return R.fail("文件上传失败：" + e.getMessage());
            }
        }

        if (resultList.isEmpty()) {
            return R.fail("没有有效文件上传");
        }

        return R.ok(resultList);
    }

    /**
     * 获取请求的基础 URL（协议 + 域名 + 端口）
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme(); // http 或 https
        String serverName = request.getServerName(); // 域名或 IP
        int serverPort = request.getServerPort(); // 端口

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);

        // 只有非标准端口才需要显式拼接
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }

        // 拼接 context-path（如 /api）
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()) {
            baseUrl.append(contextPath);
        }

        return baseUrl.toString();
    }
}
