package com.project.backend.allocation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.backend.allocation.vo.AllocationProgressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 分配任务进度 SSE 推送服务
 * 支持实时推送分配进度到前端，替代轮询
 *
 * @author 陈鸿昇
 * @since 2026-02-08
 */
@Slf4j
@Service
public class AllocationProgressService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * taskId -> emitter 列表（支持多客户端订阅同一任务）
     */
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 订阅任务进度
     */
    public void subscribe(Long taskId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.computeIfAbsent(taskId, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        emitter.onCompletion(() -> {
            log.debug("[AllocationSSE] 连接完成，taskId: {}", taskId);
            removeEmitter(taskId, emitter);
        });
        emitter.onTimeout(() -> {
            log.debug("[AllocationSSE] 连接超时，taskId: {}", taskId);
            removeEmitter(taskId, emitter);
        });
        emitter.onError(e -> {
            log.debug("[AllocationSSE] 连接错误，taskId: {}", taskId);
            removeEmitter(taskId, emitter);
        });

        log.info("[AllocationSSE] 新订阅，taskId: {}, 当前订阅数: {}", taskId, list.size());
    }

    private void removeEmitter(Long taskId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(taskId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(taskId);
            }
        }
    }

    /**
     * 推送进度更新
     */
    public void pushProgress(Long taskId, AllocationProgressVO progress) {
        sendEvent(taskId, "progress", progress);
    }

    /**
     * 推送完成事件
     */
    public void pushComplete(Long taskId, AllocationProgressVO progress) {
        sendEvent(taskId, "complete", progress);
        closeAllEmitters(taskId);
    }

    /**
     * 推送错误事件
     */
    public void pushError(Long taskId, String errorMessage) {
        AllocationProgressVO error = new AllocationProgressVO();
        error.setTaskId(taskId);
        error.setStatus(5);
        error.setStatusName("执行失败");
        error.setErrorMessage(errorMessage);
        error.setCompleted(true);
        sendEvent(taskId, "error", error);
        closeAllEmitters(taskId);
    }

    /**
     * 推送当前状态（用于 SSE 刚连接时立即推送当前进度）
     */
    public void pushCurrent(Long taskId, AllocationProgressVO progress) {
        sendEvent(taskId, "current", progress);
    }

    private void sendEvent(Long taskId, String eventName, Object data) {
        List<SseEmitter> list = emitters.get(taskId);
        if (list == null || list.isEmpty()) return;

        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("[AllocationSSE] JSON 序列化失败，taskId: {}", taskId, e);
            return;
        }

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(jsonData));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }

        for (SseEmitter dead : deadEmitters) {
            list.remove(dead);
        }
    }

    private void closeAllEmitters(Long taskId) {
        List<SseEmitter> list = emitters.remove(taskId);
        if (list != null) {
            for (SseEmitter emitter : list) {
                try { emitter.complete(); } catch (Exception ignored) {}
            }
            log.info("[AllocationSSE] 已关闭所有连接，taskId: {}, 连接数: {}", taskId, list.size());
        }
    }

    public boolean hasSubscribers(Long taskId) {
        List<SseEmitter> list = emitters.get(taskId);
        return list != null && !list.isEmpty();
    }
}
