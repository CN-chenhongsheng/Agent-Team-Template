package com.project.backend.notice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.notice.dto.NoticeQueryDTO;
import com.project.backend.notice.dto.NoticeSaveDTO;
import com.project.backend.notice.entity.Notice;
import com.project.backend.notice.vo.NoticeVO;
import com.project.core.result.PageResult;

/**
 * 通知公告Service
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 分页查询通知列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<NoticeVO> pageList(NoticeQueryDTO queryDTO);

    /**
     * 根据ID获取通知详情
     *
     * @param studentId 学生ID（用于判断是否已读）
     * @param noticeId 通知ID
     * @return 通知详情
     */
    NoticeVO getDetailById(Long studentId, Long noticeId);

    /**
     * 保存通知（新增或编辑）
     *
     * @param saveDTO 通知保存DTO
     * @return 是否成功
     */
    boolean saveNotice(NoticeSaveDTO saveDTO);

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @return 是否成功
     */
    boolean deleteNotice(Long id);

    /**
     * 批量删除通知
     *
     * @param ids 通知ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 标记通知为已读
     *
     * @param studentId 学生ID
     * @param noticeId 通知ID
     * @return 是否成功
     */
    boolean markAsRead(Long studentId, Long noticeId);

    /**
     * 获取学生未读通知数量
     *
     * @param studentId 学生ID
     * @return 未读数量
     */
    Integer getUnreadCount(Long studentId);
}
