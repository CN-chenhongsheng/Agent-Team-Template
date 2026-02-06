package com.project.backend.system.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 用户在线状态服务
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
public interface UserOnlineService {
    /**
     * 订阅在线状态
     * @return SSE Emitter
     */
    SseEmitter subscribe();

    /**
     * 用户上线
     * @param userId 用户ID
     */
    void userOnline(Long userId);

    /**
     * 用户下线
     * @param userId 用户ID
     */
    void userOffline(Long userId);

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isUserOnline(Long userId);
}
