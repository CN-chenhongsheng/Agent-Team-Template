package com.project.backend.system.controller;

import com.project.backend.system.service.UserOnlineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 用户在线状态控制器
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@RestController
@RequestMapping("/v1/system/user/online")
@RequiredArgsConstructor
@Tag(name = "用户在线状态管理", description = "用户在线状态的流式订阅")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserOnlineController {

    private final UserOnlineService userOnlineService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "订阅用户在线状态流")
    public SseEmitter streamOnlineStatus(HttpServletResponse response) {
        // 手动设置 CORS 响应头，确保 SSE 请求可以跨域
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");

        return userOnlineService.subscribe();
    }
}
