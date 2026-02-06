package com.project.backend.notice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.notice.dto.NoticeQueryDTO;
import com.project.backend.notice.dto.NoticeSaveDTO;
import com.project.backend.notice.entity.Notice;
import com.project.backend.notice.entity.NoticeRead;
import com.project.backend.notice.mapper.NoticeMapper;
import com.project.backend.notice.mapper.NoticeReadMapper;
import com.project.backend.notice.service.NoticeService;
import com.project.backend.notice.vo.NoticeVO;
import com.project.backend.util.DictUtils;
import com.project.core.context.UserContext;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知公告Service实现
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private final NoticeReadMapper noticeReadMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResult<NoticeVO> pageList(NoticeQueryDTO queryDTO) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getTitle()), Notice::getTitle, queryDTO.getTitle())
               .like(StrUtil.isNotBlank(queryDTO.getPublisherName()), Notice::getPublisherName, queryDTO.getPublisherName())
               .eq(queryDTO.getNoticeType() != null, Notice::getNoticeType, queryDTO.getNoticeType())
               .eq(queryDTO.getStatus() != null, Notice::getStatus, queryDTO.getStatus())
               .ge(queryDTO.getPublishTimeStart() != null, Notice::getPublishTime, queryDTO.getPublishTimeStart())
               .le(queryDTO.getPublishTimeEnd() != null, Notice::getPublishTime, queryDTO.getPublishTimeEnd())
               // 只查询已发布的通知（小程序端使用）
               .eq(queryDTO.getUserId() != null, Notice::getStatus, 1)
               .orderByDesc(Notice::getIsTop)
               .orderByDesc(Notice::getPublishTime)
               .orderByDesc(Notice::getCreateTime);

        Page<Notice> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<Notice> notices = page.getRecords();
        List<NoticeVO> voList = convertToVOList(notices, queryDTO.getUserId());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeVO getDetailById(Long studentId, Long noticeId) {
        Notice notice = getById(noticeId);
        if (notice == null) {
            throw new BusinessException("通知不存在");
        }

        // 转换为VO
        List<NoticeVO> voList = convertToVOList(List.of(notice), studentId);
        return voList.isEmpty() ? null : voList.getFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveNotice(NoticeSaveDTO saveDTO) {
        Notice notice = new Notice();
        BeanUtil.copyProperties(saveDTO, notice);

        // 处理附件列表（转为JSON字符串）
        if (saveDTO.getAttachments() != null && !saveDTO.getAttachments().isEmpty()) {
            notice.setAttachments(JSONUtil.toJsonStr(saveDTO.getAttachments()));
        }

        // 处理目标楼层列表（转为JSON字符串）
        if (saveDTO.getTargetFloors() != null && !saveDTO.getTargetFloors().isEmpty()) {
            notice.setTargetFloors(JSONUtil.toJsonStr(saveDTO.getTargetFloors()));
        }

        // 设置发布信息
        if (saveDTO.getStatus() != null && saveDTO.getStatus() == 1 && notice.getPublishTime() == null) {
            notice.setPublishTime(LocalDateTime.now());
            notice.setPublisherId(UserContext.getUserId());
            notice.setPublisherName(UserContext.getUsername());
        }

        // 初始化阅读次数
        if (notice.getReadCount() == null) {
            notice.setReadCount(0);
        }

        return saveOrUpdate(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotice(Long id) {
        Notice notice = getById(id);
        if (notice == null) {
            throw new BusinessException("通知不存在");
        }

        // 删除通知的阅读记录
        LambdaQueryWrapper<NoticeRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(NoticeRead::getNoticeId, id);
        noticeReadMapper.delete(readWrapper);

        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的通知");
        }

        // 删除阅读记录
        LambdaQueryWrapper<NoticeRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.in(NoticeRead::getNoticeId, Arrays.asList(ids));
        noticeReadMapper.delete(readWrapper);

        return removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long studentId, Long noticeId) {
        if (studentId == null || noticeId == null) {
            throw new BusinessException("参数错误");
        }

        // 检查通知是否存在
        Notice notice = getById(noticeId);
        if (notice == null) {
            throw new BusinessException("通知不存在");
        }

        // 检查是否已读
        LambdaQueryWrapper<NoticeRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(NoticeRead::getNoticeId, noticeId)
                   .eq(NoticeRead::getUserId, studentId);
        Long count = noticeReadMapper.selectCount(readWrapper);

        if (count > 0) {
            // 已读，不需要重复标记
            return true;
        }

        // 创建阅读记录
        NoticeRead noticeRead = new NoticeRead();
        noticeRead.setNoticeId(noticeId);
        noticeRead.setUserId(studentId);
        noticeRead.setReadTime(LocalDateTime.now());
        int inserted = noticeReadMapper.insert(noticeRead);

        // 更新通知的阅读次数
        if (inserted > 0) {
            notice.setReadCount(notice.getReadCount() + 1);
            updateById(notice);
        }

        return inserted > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUnreadCount(Long studentId) {
        if (studentId == null) {
            return 0;
        }

        // 查询所有已发布的通知
        LambdaQueryWrapper<Notice> noticeWrapper = new LambdaQueryWrapper<>();
        noticeWrapper.eq(Notice::getStatus, 1);
        List<Notice> allNotices = list(noticeWrapper);

        if (allNotices.isEmpty()) {
            return 0;
        }

        // 查询用户已读的通知ID
        LambdaQueryWrapper<NoticeRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(NoticeRead::getUserId, studentId);
        List<NoticeRead> readRecords = noticeReadMapper.selectList(readWrapper);

        Set<Long> readNoticeIds = readRecords.stream()
                .map(NoticeRead::getNoticeId)
                .collect(Collectors.toSet());

        // 计算未读数量
        return (int) allNotices.stream()
                .filter(notice -> !readNoticeIds.contains(notice.getId()))
                .count();
    }

    /**
     * 转换为VO列表
     *
     * @param notices 通知列表
     * @param userId 用户ID（用于查询已读状态，可为null）
     * @return VO列表
     */
    private List<NoticeVO> convertToVOList(List<Notice> notices, Long userId) {
        if (notices == null || notices.isEmpty()) {
            return Collections.emptyList();
        }

        // 如果传入了用户ID，查询该用户的已读记录
        Set<Long> readNoticeIds = Collections.emptySet();
        if (userId != null) {
            LambdaQueryWrapper<NoticeRead> readWrapper = new LambdaQueryWrapper<>();
            readWrapper.eq(NoticeRead::getUserId, userId);
            List<NoticeRead> readRecords = noticeReadMapper.selectList(readWrapper);
            readNoticeIds = readRecords.stream()
                    .map(NoticeRead::getNoticeId)
                    .collect(Collectors.toSet());
        }

        final Set<Long> finalReadNoticeIds = readNoticeIds;

        return notices.stream().map(notice -> {
            NoticeVO vo = new NoticeVO();
            BeanUtil.copyProperties(notice, vo);

            // 转换通知类型文本
            if (notice.getNoticeType() != null) {
                vo.setNoticeTypeText(DictUtils.getLabel("notice_type", notice.getNoticeType()));
            }

            // 转换状态文本
            if (notice.getStatus() != null) {
                vo.setStatusText(DictUtils.getLabel("notice_status", notice.getStatus()));
            }

            // 解析附件JSON
            if (StrUtil.isNotBlank(notice.getAttachments())) {
                try {
                    vo.setAttachments(JSONUtil.toList(notice.getAttachments(), String.class));
                } catch (Exception e) {
                    log.warn("解析附件JSON失败：{}", notice.getAttachments(), e);
                    vo.setAttachments(Collections.emptyList());
                }
            } else {
                vo.setAttachments(Collections.emptyList());
            }

            // 解析目标楼层JSON
            if (StrUtil.isNotBlank(notice.getTargetFloors())) {
                try {
                    vo.setTargetFloors(JSONUtil.toList(notice.getTargetFloors(), String.class));
                } catch (Exception e) {
                    log.warn("解析目标楼层JSON失败：{}", notice.getTargetFloors(), e);
                    vo.setTargetFloors(Collections.emptyList());
                }
            } else {
                vo.setTargetFloors(Collections.emptyList());
            }

            // 设置已读状态
            if (userId != null) {
                vo.setIsRead(finalReadNoticeIds.contains(notice.getId()));
            } else {
                vo.setIsRead(false);
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
