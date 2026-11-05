package com.project.core.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * HTTP请求工具类
 *
 * @author 陈鸿昇
 * @since 2026-01-28
 */
public class RequestUtils {

    /**
     * IP 请求头列表
     */
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };

    private RequestUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求对象
     * @return IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                return extractFirstIp(ip);
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 根据 IP 地址判断网络区域
     * 仅区分本地、内网、外网，精确定位可接入 IP2Region 等离线库
     *
     * @param ip IP地址
     * @return 网络区域描述
     */
    public static String getIpLocation(String ip) {
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            return "未知";
        }
        if ("127.0.0.1".equals(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)
                || "localhost".equalsIgnoreCase(ip)) {
            return "本地";
        }
        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return "内网";
        }
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length > 1) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return "内网";
                    }
                } catch (NumberFormatException ignored) {
                    // 非法 IP 按外网处理
                }
            }
        }
        return "外网";
    }

    /**
     * 从当前请求上下文获取客户端IP地址
     *
     * @return IP地址，如果无法获取则返回 "unknown"
     */
    public static String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        return getClientIp(attributes.getRequest());
    }

    /**
     * 判断IP是否有效
     *
     * @param ip IP地址
     * @return 是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 提取第一个IP（处理多个IP的情况）
     *
     * @param ip IP地址字符串（可能包含多个IP，逗号分隔）
     * @return 第一个IP地址
     */
    private static String extractFirstIp(String ip) {
        return ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
