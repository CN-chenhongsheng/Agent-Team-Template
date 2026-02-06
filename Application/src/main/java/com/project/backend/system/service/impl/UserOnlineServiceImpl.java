package com.project.backend.system.service.impl;

import com.project.backend.system.service.UserOnlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOnlineServiceImpl implements UserOnlineService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String ONLINE_USERS_KEY = "sys:user:online";
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

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
            emitter.send(SseEmitter.event().name("init").data("Connected"));
            log.debug("SSE connection established");
        } catch (IOException e) {
            log.error("SSE init error", e);
            emitters.remove(emitter);
        }

        return emitter;
    }

    @Override
    public void userOnline(Long userId) {
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId.toString());
        broadcastStatusChange(userId, true);
    }

    @Override
    public void userOffline(Long userId) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId.toString());
        broadcastStatusChange(userId, false);
    }

    @Override
    public boolean isUserOnline(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId.toString()));
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
