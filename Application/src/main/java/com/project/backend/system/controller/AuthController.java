package com.project.backend.system.controller;

import com.project.core.annotation.RateLimit;
import com.project.core.result.R;
import com.project.backend.system.dto.LoginDTO;
import com.project.backend.system.dto.StudentLoginDTO;
import com.project.backend.system.dto.WxLoginDTO;
import com.project.backend.system.service.AuthService;
import com.project.backend.system.vo.LoginVO;
import com.project.backend.system.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@Tag(name = "认证管理", description = "登录、登出、获取用户信息")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @RateLimit(key = "login", time = 60, count = 5, message = "登录请求过于频繁，请稍后再试")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        log.info("用户登录请求: {}", loginDTO.getUsername());
        LoginVO loginVO = authService.login(loginDTO, request, response);
        return R.ok(loginVO);
    }

    /**
     * 学生登录
     */
    @Operation(summary = "学生登录")
    @PostMapping("/student/login")
    @RateLimit(key = "student-login", time = 60, count = 5, message = "登录请求过于频繁，请稍后再试")
    public R<LoginVO> studentLogin(@Valid @RequestBody StudentLoginDTO studentLoginDTO,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        log.info("学生登录请求: {}", studentLoginDTO.getStudentNo());
        LoginVO loginVO = authService.studentLogin(studentLoginDTO, request, response);
        return R.ok(loginVO);
    }

    /**
     * 微信小程序登录
     */
    @Operation(summary = "微信小程序登录")
    @PostMapping("/wx-login")
    @RateLimit(key = "wx-login", time = 60, count = 10, message = "登录请求过于频繁，请稍后再试")
    public R<LoginVO> wxLogin(@Valid @RequestBody WxLoginDTO wxLoginDTO,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        log.info("微信登录请求");
        LoginVO loginVO = authService.wxLogin(wxLoginDTO, request, response);
        return R.ok(loginVO);
    }

    /**
     * 刷新 Access Token
     */
    @Operation(summary = "刷新Access Token")
    @PostMapping("/refresh")
    public R<String> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("Token刷新请求");
        String newAccessToken = authService.refreshToken(request, response);
        return R.ok(newAccessToken);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return R.ok();
    }

    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userinfo")
    public R<UserInfoVO> getUserInfo() {
        UserInfoVO userInfo = authService.getCurrentUserInfo();
        return R.ok(userInfo);
    }
}
