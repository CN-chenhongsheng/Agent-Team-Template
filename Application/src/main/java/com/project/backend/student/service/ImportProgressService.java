package com.project.backend.student.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.backend.student.dto.imports.ImportResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 导入进度 SSE 推送服务
 * 支持实时推送导入进度到前端
 *
 * @author 陈鸿昇
 * @since 2026-02-05
 */
@Slf4j
@Service
public class ImportProgressService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * taskId -> emitter 列表（支持多客户端订阅同一任务）
     */
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 订阅任务进度
     *
     * @param taskId  任务 ID
     * @param emitter SSE 发射器
     */
    public void subscribe(String taskId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.computeIfAbsent(taskId, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        // 设置回调：连接完成/超时/错误时移除
        emitter.onCompletion(() -> {
            log.debug("SSE 连接完成，taskId: {}", taskId);
            removeEmitter(taskId, emitter);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE 连接超时，taskId: {}", taskId);
            removeEmitter(taskId, emitter);
        });
        emitter.onError(e -> {
            log.debug("SSE 连接错误，taskId: {}, error: {}", taskId, e.getMessage());
            removeEmitter(taskId, emitter);
        });

        log.info("新的 SSE 订阅，taskId: {}, 当前订阅数: {}", taskId, list.size());
    }

    /**
     * 移除指定的 emitter
     */
    private void removeEmitter(String taskId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(taskId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(taskId);
            }
        }
    }

    /**
     * 推送阶段变化
     *
     * @param taskId    任务 ID
     * @param stage     阶段名称（parsing/importing/complete）
     * @param message   阶段描述
     * @param totalRows 总行数（可选，在 parsing 阶段统计完成后传入）
     */
    public void pushStage(String taskId, String stage, String message, Integer totalRows) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("stage", stage);
        data.put("message", message);
        if (totalRows != null) {
            data.put("totalRows", totalRows);
        }
        sendEvent(taskId, "stage", data);
    }

    /**
     * 推送导入进度
     *
     * @param taskId       任务 ID
     * @param percent      进度百分比（0-100）
     * @param processed    已处理行数
     * @param total        总行数
     * @param successCount 成功数（可选）
     * @param failCount    失败数（可选）
     */
    public void pushProgress(String taskId, int percent, int processed, int total,
                             Integer successCount, Integer failCount) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("percent", percent);
        data.put("processed", processed);
        data.put("total", total);
        if (successCount != null) {
            data.put("successCount", successCount);
        }
        if (failCount != null) {
            data.put("failCount", failCount);
        }
        sendEvent(taskId, "progress", data);
    }

    /**
     * 推送导入进度（简化版，不含成功/失败数）
     */
    public void pushProgress(String taskId, int percent, int processed, int total) {
        pushProgress(taskId, percent, processed, total, null, null);
    }

    /**
     * 推送完成/失败
     *
     * @param taskId 任务 ID
     * @param status 状态（success/failed）
     * @param result 导入结果
     */
    public void pushComplete(String taskId, String status, ImportResult result) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("status", status);
        if (result != null) {
            data.put("result", result);
        }
        sendEvent(taskId, "complete", data);

        // 完成后关闭所有连接
        closeAllEmitters(taskId);
    }

    /**
     * 推送错误
     *
     * @param taskId  任务 ID
     * @param message 错误信息
     */
    public void pushError(String taskId, String message) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("message", message);
        sendEvent(taskId, "error", data);

        // 错误后关闭所有连接
        closeAllEmitters(taskId);
    }

    /**
     * 发送 SSE 事件
     */
    private void sendEvent(String taskId, String eventName, Object data) {
        List<SseEmitter> list = emitters.get(taskId);
        if (list == null || list.isEmpty()) {
            log.debug("没有订阅者，taskId: {}, event: {}", taskId, eventName);
            return;
        }

        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("JSON 序列化失败，taskId: {}, event: {}", taskId, eventName, e);
            return;
        }

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(jsonData));
            } catch (IOException e) {
                log.debug("发送 SSE 事件失败，标记为待移除，taskId: {}, event: {}", taskId, eventName);
                deadEmitters.add(emitter);
            }
        }

        // 移除失效的 emitter
        for (SseEmitter dead : deadEmitters) {
            list.remove(dead);
        }

        log.debug("SSE 事件已发送，taskId: {}, event: {}, 订阅数: {}", taskId, eventName, list.size());
    }

    /**
     * 关闭指定任务的所有 SSE 连接
     */
    private void closeAllEmitters(String taskId) {
        List<SseEmitter> list = emitters.remove(taskId);
        if (list != null) {
            for (SseEmitter emitter : list) {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.debug("关闭 SSE 连接时发生异常，taskId: {}", taskId);
                }
            }
            log.info("已关闭所有 SSE 连接，taskId: {}, 连接数: {}", taskId, list.size());
        }
    }

    /**
     * 检查是否有订阅者
     *
     * @param taskId 任务 ID
     * @return 是否有订阅者
     */
    public boolean hasSubscribers(String taskId) {
        List<SseEmitter> list = emitters.get(taskId);
        return list != null && !list.isEmpty();
    }
}
