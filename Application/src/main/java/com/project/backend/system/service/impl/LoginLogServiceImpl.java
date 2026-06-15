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
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import jakarta.servlet.http.HttpServletRequest;
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
            LocalDateTime startTime = LocalDateTime.parse(queryDTO.getStartTime() + " 00:00:00", DATE_TIME_FORMATTER);
            wrapper.ge(LoginLog::getLoginTime, startTime);
        }
        if (StrUtil.isNotBlank(queryDTO.getEndTime())) {
            LocalDateTime endTime = LocalDateTime.parse(queryDTO.getEndTime() + " 23:59:59", DATE_TIME_FORMATTER);
            wrapper.le(LoginLog::getLoginTime, endTime);
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
    public void recordLoginLog(String username, String loginType, Integer status, String message, HttpServletRequest request) {
        try {
            log.info("开始记录登入日志 - 用户: {}, 类型: {}, 状态: {}", username, loginType, status);
            
            LoginLog loginLog = new LoginLog();
            loginLog.setUsername(username);
            loginLog.setLoginType(loginType);
            loginLog.setLoginStatus(status);
            loginLog.setMessage(message);
            loginLog.setLoginTime(LocalDateTime.now());

            String ipAddress = getClientIp(request);
            loginLog.setIpAddress(ipAddress);
            loginLog.setLoginLocation(getLocationByIp(ipAddress));

            String userAgentStr = request.getHeader("User-Agent");
            if (StrUtil.isNotBlank(userAgentStr)) {
                UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
                loginLog.setBrowser(userAgent.getBrowser().getName() + " " + userAgent.getVersion());
                loginLog.setOs(userAgent.getOs().getName());
            }

            save(loginLog);
            log.info("登入日志记录成功 - 用户: {}, IP: {}, 地点: {}", username, ipAddress, loginLog.getLoginLocation());
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
        vo.setLoginLocation(loginLog.getLoginLocation());
        vo.setBrowser(loginLog.getBrowser());
        vo.setOs(loginLog.getOs());
        vo.setMessage(loginLog.getMessage());
        vo.setLoginTime(loginLog.getLoginTime() != null ? loginLog.getLoginTime().format(DATE_TIME_FORMATTER) : null);
        return vo;
    }

    private String getLoginTypeText(String loginType) {
        if (loginType == null) return "-";
        return switch (loginType) {
            case "password" -> "密码登录";
            case "wechat" -> "微信登录";
            default -> loginType;
        };
    }

    private String getLoginStatusText(Integer status) {
        if (status == null) return "-";
        return switch (status) {
            case 0 -> "失败";
            case 1 -> "成功";
            case 2 -> "登出";
            default -> "未知";
        };
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 根据IP地址获取地理位置
     * 简单实现：判断内网/外网
     */
    private String getLocationByIp(String ip) {
        if (StrUtil.isBlank(ip)) {
            return "未知";
        }

        // 本地回环地址
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip)) {
            return "本地";
        }

        // 内网IP
        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
            return "内网";
        }

        // 外网IP - 可以集成IP2Region等库进行精确定位
        return "外网";
    }
}
