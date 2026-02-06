package com.project.backend.controller;

import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 测试控制器（仅用于开发环境）
 * 用于生成BCrypt密码hash等测试功能
 *
 * @author 陈鸿昇
 * @since 2025-12-30
 */
@Slf4j
@Tag(name = "测试工具", description = "开发测试用接口")
@RestController
@RequestMapping("/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 生成BCrypt密码hash
     * 仅用于开发测试，生产环境应删除此接口
     */
    @Operation(summary = "生成BCrypt密码hash")
    @PostMapping("/generate-password")
    public R<String> generatePassword(@RequestParam String password) {
        log.info("========== 生成密码Hash ==========");
        log.info("原始密码: {}", password);

        try {
            String hash = passwordEncoder.encode(password);
            log.info("生成的Hash: {}", hash);

            // 验证
            boolean matches = passwordEncoder.matches(password, hash);
            log.info("验证结果: {}", matches);
            log.info("================================");

            return R.ok(hash);
        } catch (Exception e) {
            log.error("生成密码Hash异常", e);
            return R.fail("生成失败: " + e.getMessage());
        }
    }

    /**
     * 验证密码
     */
    @Operation(summary = "验证密码")
    @PostMapping("/verify-password")
    public R<Boolean> verifyPassword(
            @RequestParam String password,
            @RequestParam String hash
    ) {
        log.info("========== 验证密码 ==========");
        log.info("原始密码: {}", password);
        log.info("Hash: {}", hash);

        try {
            boolean matches = passwordEncoder.matches(password, hash);
            log.info("验证结果: {}", matches);
            log.info("============================");

            return R.ok(matches);
        } catch (Exception e) {
            log.error("验证密码异常", e);
            return R.fail("验证失败: " + e.getMessage());
        }
    }
}
