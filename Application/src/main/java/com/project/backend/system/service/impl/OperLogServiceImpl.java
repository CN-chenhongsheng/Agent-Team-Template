package com.project.backend.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.system.dto.OperLogQueryDTO;
import com.project.backend.system.entity.OperLog;
import com.project.backend.system.mapper.OperLogMapper;
import com.project.backend.system.service.OperLogService;
import com.project.backend.util.DictUtils;
import com.project.backend.system.vo.OperLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@Service
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements OperLogService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 异步保存操作日志
     */
    @Override
    @Async
    public void saveOperLog(OperLog operLog) {
        try {
            save(operLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    @Override
    public PageResult<OperLogVO> pageList(OperLogQueryDTO queryDTO) {
        LambdaQueryWrapper<OperLog> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        wrapper.like(StrUtil.isNotBlank(queryDTO.getTitle()), OperLog::getTitle, queryDTO.getTitle())
               .like(StrUtil.isNotBlank(queryDTO.getOperName()), OperLog::getOperName, queryDTO.getOperName())
               .eq(queryDTO.getBusinessType() != null, OperLog::getBusinessType, queryDTO.getBusinessType())
               .eq(queryDTO.getStatus() != null, OperLog::getStatus, queryDTO.getStatus());

        // 时间范围查询
        if (StrUtil.isNotBlank(queryDTO.getOperTimeStart())) {
            LocalDateTime startTime = LocalDateTime.parse(queryDTO.getOperTimeStart() + " 00:00:00", DATE_TIME_FORMATTER);
            wrapper.ge(OperLog::getOperTime, startTime);
        }
        if (StrUtil.isNotBlank(queryDTO.getOperTimeEnd())) {
            LocalDateTime endTime = LocalDateTime.parse(queryDTO.getOperTimeEnd() + " 23:59:59", DATE_TIME_FORMATTER);
            wrapper.le(OperLog::getOperTime, endTime);
        }

        wrapper.orderByDesc(OperLog::getOperTime);

        // 分页查询
        Page<OperLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        // 转换为VO
        List<OperLogVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public OperLogVO getDetailById(Long id) {
        OperLog operLog = getById(id);
        if (operLog == null) {
            throw new BusinessException("操作日志不存在");
        }
        return convertToVO(operLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clean() {
        return remove(new LambdaQueryWrapper<>());
    }

    /**
     * 转换为VO
     */
    private OperLogVO convertToVO(OperLog operLog) {
        OperLogVO vo = new OperLogVO();
        vo.setId(operLog.getId());
        vo.setTitle(operLog.getTitle());
        vo.setBusinessType(operLog.getBusinessType());
        vo.setBusinessTypeText(getBusinessTypeText(operLog.getBusinessType()));
        vo.setMethod(operLog.getMethod());
        vo.setRequestMethod(operLog.getRequestMethod());
        vo.setOperatorType(operLog.getOperatorType());
        vo.setOperName(operLog.getOperName());
        vo.setDeviceType(operLog.getDeviceType());
        vo.setDeviceTypeText(getDeviceTypeText(operLog.getDeviceType()));
        vo.setOperUrl(operLog.getOperUrl());
        vo.setOperIp(operLog.getOperIp());
        vo.setOperLocation(operLog.getOperLocation());
        vo.setOperParam(operLog.getOperParam());
        vo.setJsonResult(operLog.getJsonResult());
        vo.setStatus(operLog.getStatus());
        vo.setStatusText(operLog.getStatus() == 0 ? "正常" : "异常");
        vo.setErrorMsg(operLog.getErrorMsg());
        vo.setOperTime(operLog.getOperTime());
        vo.setCostTime(operLog.getCostTime());
        return vo;
    }

    /**
     * 获取业务类型文本
     */
    private String getBusinessTypeText(Integer businessType) {
        return DictUtils.getLabel("sys_oper_business_type", businessType, "其它");
    }

    /**
     * 获取设备类型文本
     */
    private String getDeviceTypeText(Integer deviceType) {
        if (deviceType == null) {
            return "-";
        }
        return DictUtils.getLabel("sys_device_type", deviceType, "未知");
    }
}
