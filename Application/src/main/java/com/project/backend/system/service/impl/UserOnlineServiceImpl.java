package com.project.backend.system.service.impl;

import com.project.backend.system.service.UserOnlineService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 用户在线状态服务实现
 * 在线用户以 Redis ZSet 存储，score 为最后活跃时间，超过有效期自动清理；
 * SSE 连接保存在当前实例内存中
 *
 * @author 陈鸿昇
 * @since 2026-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserOnlineServiceImpl implements UserOnlineService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String ONLINE_USERS_KEY = "sys:user:online";
    private static final Duration ONLINE_TTL = Duration.ofHours(2);
    private static final Duration KEY_TTL = Duration.ofDays(1);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * 应用启动时清理上次运行残留的在线状态
     */
    @PostConstruct
    public void clearStaleOnlineUsers() {
        redisTemplate.delete(ONLINE_USERS_KEY);
    }

    @Override
    public SseEmitter subscribe() {
        // 设置超时时间0（永不超时），并配置 CORS 响应头
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE connection completed");
            emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE connection timeout");
            emitters.remove(emitter);
        });
        emitter.onError((e) -> {
            log.error("SSE connection error", e);
            emitters.remove(emitter);
        });

        try {
            String snapshot = String.format("{\"onlineUserIds\": %s}", getOnlineUserIds());
            emitter.send(SseEmitter.event().name("init").data(snapshot));
            log.debug("SSE connection established");
        } catch (IOException e) {
            log.error("SSE init error", e);
            emitters.remove(emitter);
        }

        return emitter;
    }

    @Override
    public void userOnline(Long userId) {
        redisTemplate.opsForZSet().add(ONLINE_USERS_KEY, userId.toString(), System.currentTimeMillis());
        redisTemplate.expire(ONLINE_USERS_KEY, KEY_TTL);
        broadcastStatusChange(userId, true);
    }

    @Override
    public void userOffline(Long userId) {
        redisTemplate.opsForZSet().remove(ONLINE_USERS_KEY, userId.toString());
        broadcastStatusChange(userId, false);
    }

    @Override
    public List<Long> getOnlineUserIds() {
        long threshold = System.currentTimeMillis() - ONLINE_TTL.toMillis();
        redisTemplate.opsForZSet().removeRangeByScore(ONLINE_USERS_KEY, 0, threshold);
        Set<Object> members = redisTemplate.opsForZSet().range(ONLINE_USERS_KEY, 0, -1);
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        return members.stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    try {
                        return Long.parseLong(item.toString());
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void broadcastStatusChange(Long userId, boolean isOnline) {
        String data = String.format("{\"userId\": %d, \"isOnline\": %b}", userId, isOnline);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("status-update").data(data));
            } catch (IOException e) {
                log.warn("发送状态更新到Emitter失败，移除Emitter");
                emitters.remove(emitter);
            }
        }
    }
}
