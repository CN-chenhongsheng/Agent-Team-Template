package com.project.backend.notice.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.project.backend.notice.dto.NoticeQueryDTO;
import com.project.backend.notice.dto.NoticeSaveDTO;
import com.project.backend.notice.service.NoticeService;
import com.project.backend.notice.vo.NoticeVO;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 通知公告Controller（小程序端）
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Slf4j
@RestController
@RequestMapping("/v1/service/notice")
@RequiredArgsConstructor
@Tag(name = "通知公告管理", description = "提供通知公告查询、标记已读等功能")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 获取通知列表
     *
     * @param queryDTO 查询条件
     * @return 通知列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取通知列表", description = "获取已发布的通知列表，支持分页和条件筛选")
    public R<PageResult<NoticeVO>> getNoticeList(
            @Parameter(description = "查询条件")
            @ModelAttribute NoticeQueryDTO queryDTO) {
        // 从 Token 中获取当前登录学生的 ID
        Long studentId = StpUtil.getLoginIdAsLong();
        log.info("获取通知列表，学生ID：{}，查询条件：{}", studentId, queryDTO);

        // 设置用户ID到查询条件（用于判断已读状态）
        queryDTO.setUserId(studentId);

        PageResult<NoticeVO> pageResult = noticeService.pageList(queryDTO);
        return R.ok(pageResult);
    }

    /**
     * 获取通知详情
     *
     * @param id 通知ID
     * @return 通知详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取通知详情", description = "根据ID获取通知详情信息")
    public R<NoticeVO> getNoticeDetail(
            @Parameter(description = "通知ID", required = true)
            @PathVariable Long id) {
        // 从 Token 中获取当前登录学生的 ID
        Long studentId = StpUtil.getLoginIdAsLong();
        log.info("获取通知详情，学生ID：{}，通知ID：{}", studentId, id);

        NoticeVO vo = noticeService.getDetailById(studentId, id);
        return R.ok(vo);
    }

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @PostMapping("/mark-read/{id}")
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读状态")
    public R<Void> markAsRead(
            @Parameter(description = "通知ID", required = true)
            @PathVariable Long id) {
        // 从 Token 中获取当前登录学生的 ID
        Long studentId = StpUtil.getLoginIdAsLong();
        log.info("标记通知为已读，学生ID：{}，通知ID：{}", studentId, id);

        boolean success = noticeService.markAsRead(studentId, id);
        return success ? R.ok() : R.fail("标记失败");
    }

    /**
     * 获取未读通知数量
     *
     * @return 未读数量
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量", description = "获取当前用户未读通知的数量")
    public R<Integer> getUnreadCount() {
        // 从 Token 中获取当前登录学生的 ID
        Long studentId = StpUtil.getLoginIdAsLong();
        log.info("获取未读通知数量，学生ID：{}", studentId);

        Integer count = noticeService.getUnreadCount(studentId);
        return R.ok(count);
    }
}
