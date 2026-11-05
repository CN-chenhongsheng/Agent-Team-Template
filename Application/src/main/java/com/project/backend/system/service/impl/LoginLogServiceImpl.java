package com.project.backend.system.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.system.dto.LoginLogQueryDTO;
import com.project.backend.system.entity.LoginLog;
import com.project.backend.system.mapper.LoginLogMapper;
import com.project.backend.system.service.LoginLogService;
import com.project.backend.system.vo.LoginLogVO;
import com.project.backend.util.DictUtils;
import com.project.core.exception.BusinessException;
import com.project.core.i18n.I18nUtils;
import com.project.core.result.PageResult;
import com.project.core.util.DateTimeUtils;
import com.project.core.util.RequestUtils;
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
 * 登入日志Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-03-08
 */
@Slf4j
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public PageResult<LoginLogVO> pageList(LoginLogQueryDTO queryDTO) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StrUtil.isNotBlank(queryDTO.getUsername()), LoginLog::getUsername, queryDTO.getUsername())
               .eq(StrUtil.isNotBlank(queryDTO.getLoginType()), LoginLog::getLoginType, queryDTO.getLoginType())
               .eq(queryDTO.getLoginStatus() != null, LoginLog::getLoginStatus, queryDTO.getLoginStatus());

        if (StrUtil.isNotBlank(queryDTO.getStartTime())) {
            wrapper.ge(LoginLog::getLoginTime, DateTimeUtils.parseDayStart(queryDTO.getStartTime()));
        }
        if (StrUtil.isNotBlank(queryDTO.getEndTime())) {
            wrapper.le(LoginLog::getLoginTime, DateTimeUtils.parseDayEnd(queryDTO.getEndTime()));
        }

        wrapper.orderByDesc(LoginLog::getLoginTime);

        Page<LoginLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<LoginLogVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public LoginLogVO getDetailById(Long id) {
        LoginLog loginLog = getById(id);
        if (loginLog == null) {
            throw new BusinessException("登入日志不存在");
        }
        return convertToVO(loginLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clean() {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(LoginLog::getId);
        return remove(wrapper);
    }

    @Override
    @Async
    public void recordLoginLog(String username, String loginType, Integer status, String message,
                               String ip, String userAgent) {
        try {
            log.info("开始记录登入日志 - 用户: {}, 类型: {}, 状态: {}", username, loginType, status);

            LoginLog loginLog = new LoginLog();
            loginLog.setUsername(username);
            loginLog.setLoginType(loginType);
            loginLog.setLoginStatus(status);
            loginLog.setMessage(message);
            loginLog.setLoginTime(LocalDateTime.now());

            loginLog.setIpAddress(ip);
            loginLog.setLoginLocation(RequestUtils.getIpLocation(ip));

            if (StrUtil.isNotBlank(userAgent)) {
                UserAgent parsedUserAgent = UserAgentUtil.parse(userAgent);
                loginLog.setBrowser(parsedUserAgent.getBrowser().getName() + " " + parsedUserAgent.getVersion());
                loginLog.setOs(parsedUserAgent.getOs().getName());
            }

            save(loginLog);
            log.info("登入日志记录成功 - 用户: {}, IP: {}, 地点: {}", username, ip, loginLog.getLoginLocation());
        } catch (Exception e) {
            log.error("记录登入日志失败", e);
        }
    }

    private LoginLogVO convertToVO(LoginLog loginLog) {
        LoginLogVO vo = new LoginLogVO();
        vo.setId(loginLog.getId());
        vo.setUsername(loginLog.getUsername());
        vo.setLoginType(loginLog.getLoginType());
        vo.setLoginTypeText(getLoginTypeText(loginLog.getLoginType()));
        vo.setLoginStatus(loginLog.getLoginStatus());
        vo.setLoginStatusText(getLoginStatusText(loginLog.getLoginStatus()));
        vo.setIpAddress(loginLog.getIpAddress());
        vo.setLoginLocation(I18nUtils.translate(loginLog.getLoginLocation()));
        vo.setBrowser(loginLog.getBrowser());
        vo.setOs(loginLog.getOs());
        vo.setMessage(I18nUtils.translate(loginLog.getMessage()));
        vo.setLoginTime(loginLog.getLoginTime() != null ? loginLog.getLoginTime().format(DATE_TIME_FORMATTER) : null);
        return vo;
    }

    private String getLoginTypeText(String loginType) {
        if (loginType == null) return "-";
        return "password".equals(loginType)
                ? DictUtils.getLabel("sys_login_type", loginType, "密码登录")
                : loginType;
    }

    private String getLoginStatusText(Integer status) {
        if (status == null) return "-";
        String fallback = switch (status) {
            case 0 -> "失败";
            case 1 -> "成功";
            case 2 -> "登出";
            default -> "未知";
        };
        return DictUtils.getLabel("sys_login_status", status, fallback);
    }
}
