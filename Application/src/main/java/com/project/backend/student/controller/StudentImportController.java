package com.project.backend.student.controller;

import com.project.backend.organization.service.SchoolHierarchyService;
import com.project.backend.organization.vo.SchoolHierarchyVO;
import com.project.backend.room.service.DormHierarchyService;
import com.project.backend.room.vo.DormHierarchyVO;
import com.project.backend.student.dto.imports.ImportFileRequest;
import com.project.backend.student.dto.imports.ImportResult;
import com.project.backend.student.dto.imports.ImportTaskVO;
import com.project.backend.student.dto.imports.TaskIdResponse;
import com.project.backend.student.service.ImportProgressService;
import com.project.backend.student.service.StudentImportService;
import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 学生导入控制器
 * 提供学生批量导入所需的树形结构数据及导入接口
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/v1/student/import")
@RequiredArgsConstructor
@Tag(name = "学生导入", description = "学生批量导入相关接口")
public class StudentImportController {

    private final SchoolHierarchyService schoolHierarchyService;
    private final DormHierarchyService dormHierarchyService;
    private final StudentImportService studentImportService;
    private final ImportProgressService importProgressService;

    /**
     * 获取组织架构树
     * 包含校区、院系、专业、班级的完整层级结构
     * 用于生成Excel导入模板的级联下拉
     */
    @GetMapping("/org-tree")
    @Operation(summary = "获取组织架构树", description = "返回校区-院系-专业-班级的层级结构，用于Excel级联下拉")
    public R<SchoolHierarchyVO> getOrgTree() {
        SchoolHierarchyVO hierarchy = schoolHierarchyService.getFullHierarchy();
        return R.ok(hierarchy);
    }

    /**
     * 获取住宿结构树
     * 包含校区、楼层、房间、床位的完整层级结构
     * 用于生成Excel导入模板的级联下拉
     */
    @GetMapping("/dorm-tree")
    @Operation(summary = "获取住宿结构树", description = "返回校区-楼层-房间-床位的层级结构，用于Excel级联下拉")
    public R<DormHierarchyVO> getDormTree() {
        DormHierarchyVO hierarchy = dormHierarchyService.getFullHierarchy();
        return R.ok(hierarchy);
    }

    /**
     * 根据文件 URL 执行导入
     * 行数 ≤ 阈值时同步返回 ImportResult；行数 > 阈值时返回 taskId，前端轮询 GET /import/task/{taskId}
     */
    @PostMapping("")
    @Operation(summary = "执行导入", description = "传入分片上传 merge 返回的 fileUrl，同步或异步导入并返回结果或 taskId")
    public R<Object> importFromFile(@RequestBody ImportFileRequest request) {
        log.info("收到导入请求，fileUrl: {}", request != null ? request.getFileUrl() : null);
        if (request == null || request.getFileUrl() == null || request.getFileUrl().isBlank()) {
            log.warn("fileUrl 为空");
            return R.fail("fileUrl 不能为空");
        }
        try {
            Object data = studentImportService.importFromExcel(request.getFileUrl(), request.getTotalRows());
            if (data instanceof ImportResult) {
                return R.ok(data);
            }
            if (data instanceof TaskIdResponse) {
                return R.ok(data);
            }
            return R.ok(data);
        } catch (Exception e) {
            log.error("导入失败: {}", e.getMessage(), e);
            return R.fail("导入失败: " + e.getMessage());
        }
    }

    /**
     * 查询异步导入任务状态与结果
     */
    @GetMapping("/task/{taskId}")
    @Operation(summary = "查询导入任务", description = "轮询获取异步导入任务状态及完成时的 ImportResult")
    public R<ImportTaskVO> getImportTask(@PathVariable String taskId) {
        ImportTaskVO vo = studentImportService.getTask(taskId);
        if (vo == null) {
            return R.fail("任务不存在或已过期");
        }
        return R.ok(vo);
    }

    /**
     * SSE 订阅导入进度
     * 实时推送导入进度到前端，无需轮询
     *
     * @param taskId 任务 ID
     * @return SSE 发射器
     */
    @GetMapping(value = "/progress/{taskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE 订阅导入进度", description = "实时推送导入进度，推荐替代轮询方式")
    public SseEmitter subscribeImportProgress(@PathVariable String taskId) {
        log.info("收到 SSE 订阅请求，taskId: {}", taskId);

        // 30 分钟超时，足够处理大文件导入
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // 注册到进度推送服务
        importProgressService.subscribe(taskId, emitter);

        // 检查任务是否已完成，如果已完成则立即推送结果
        ImportTaskVO vo = studentImportService.getTask(taskId);
        if (vo != null) {
            if ("success".equals(vo.getStatus()) || "failed".equals(vo.getStatus())) {
                // 任务已完成，立即推送完成事件
                importProgressService.pushComplete(taskId, vo.getStatus(), vo.getResult());
            } else if ("processing".equals(vo.getStatus())) {
                // 任务正在处理中，推送当前进度
                int percent = vo.getProgressPercent() != null ? vo.getProgressPercent() : 0;
                ImportResult result = vo.getResult();
                int processed = result != null && result.getTotalRows() != null ? result.getTotalRows() : 0;
                int total = result != null && result.getTotalRows() != null ? result.getTotalRows() : 0;
                importProgressService.pushProgress(taskId, percent, processed, total);
            }
        }

        return emitter;
    }
}
