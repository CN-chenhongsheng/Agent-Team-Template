package com.project.backend.notice.controller;

import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import com.project.backend.notice.dto.NoticeQueryDTO;
import com.project.backend.notice.dto.NoticeSaveDTO;
import com.project.backend.notice.service.NoticeService;
import com.project.backend.notice.vo.NoticeVO;
import com.project.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知公告Controller（管理端）
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/notice")
@RequiredArgsConstructor
@Tag(name = "通知公告管理（管理端）", description = "提供通知公告的完整CRUD操作")
public class NoticeAdminController extends BaseCrudController<NoticeVO, NoticeQueryDTO, NoticeSaveDTO>
        implements BatchDeleteController {

    private final NoticeService noticeService;

    @Override
    public String getEntityName() {
        return "通知公告";
    }

    @Override
    protected PageResult<NoticeVO> callPageList(NoticeQueryDTO queryDTO) {
        // 管理端不需要传入userId，因为不关心已读状态
        return noticeService.pageList(queryDTO);
    }

    @Override
    protected NoticeVO callGetDetailById(Long id) {
        // 管理端不需要传入userId
        return noticeService.getDetailById(null, id);
    }

    @Override
    protected boolean callSave(NoticeSaveDTO saveDTO) {
        return noticeService.saveNotice(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return noticeService.deleteNotice(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return noticeService.batchDelete(ids);
    }
}
