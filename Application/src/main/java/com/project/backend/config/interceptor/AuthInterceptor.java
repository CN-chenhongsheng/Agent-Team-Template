package com.project.backend.config.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.project.core.constant.CacheConstant;
import com.project.core.context.UserContext;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.system.entity.User;
import com.project.backend.system.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * 认证拦截器
 * 验证 Token 并将用户信息存入 ThreadLocal
 * 支持管理员（sys_user）和学生（sys_student）两种类型的认证
 * 使用 Redis 缓存用户信息，避免每次请求查库
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 缓存键前缀：用户类型标记
     * 格式：project:login_user:{userId} -> "admin:{username}:{nickname}:{avatar}" 或 "student:{studentNo}:{studentName}:"
     */
    private static final String CACHE_SEPARATOR = "\u001F"; // Unit Separator，不会出现在正常数据中
    private static final Duration CACHE_TTL = Duration.ofMinutes(CacheConstant.LOGIN_USER_TTL);

    /**
     * 白名单路径（不需要登录验证）
     */
    private static final String[] WHITELIST = {
            "/v1/auth/login",           // 管理员登录
            "/v1/auth/wx-login",        // 微信登录
            "/v1/auth/refresh",         // Token刷新
            "/v1/auth/logout",          // 登出
            "/doc.html",                // Swagger 文档
            "/webjars",                 // Swagger 静态资源
            "/v3/api-docs",             // Swagger API 文档
            "/favicon.ico"              // 网站图标
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();

        // 检查是否为白名单路径
        if (isWhitelist(requestPath)) {
            return true;
        }

        try {
            String token = extractToken(request);

            // 如果没有 token，返回 401
            if (token == null || token.isEmpty()) {
                log.warn("请求中未找到 Token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 验证 Token 并获取用户ID
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                log.warn("Token 无效或已过期，Token 前10字符: {}", token.length() > 10 ? token.substring(0, 10) : token);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            Long userId = Long.valueOf(loginId.toString());
            log.debug("Token 验证成功，用户ID: {}", userId);

            // 优先从 Redis 缓存获取用户信息
            String cacheKey = CacheConstant.LOGIN_USER_KEY + userId;
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                // 缓存命中：解析并设置 UserContext
                return restoreUserFromCache(cached, userId, response);
            }

            // 缓存未命中：查库并写入缓存
            return loadUserFromDbAndCache(userId, cacheKey, response);

        } catch (Exception e) {
            log.warn("Token 验证失败：{}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    /**
     * 从请求中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String token = null;

        // 1. 优先从 Authorization 请求头读取 Bearer Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            if (authHeader.startsWith("Bearer ") || authHeader.startsWith("bearer ")) {
                token = authHeader.substring(7).trim();
            } else {
                token = authHeader.trim();
            }
        }

        // 2. 如果请求头中没有 token，从 URL 参数读取（用于 SSE）
        if (token == null) {
            String tokenParam = request.getParameter("token");
            if (tokenParam != null && !tokenParam.isEmpty()) {
                token = tokenParam;
            }
        }

        return token;
    }

    /**
     * 从缓存恢复用户信息到 UserContext
     */
    private boolean restoreUserFromCache(String cached, Long userId, HttpServletResponse response) {
        String[] parts = cached.split(CACHE_SEPARATOR, -1);
        if (parts.length < 4) {
            // 缓存格式错误，删除后走查库逻辑
            stringRedisTemplate.delete(CacheConstant.LOGIN_USER_KEY + userId);
            return loadUserFromDbAndCache(userId, CacheConstant.LOGIN_USER_KEY + userId, response);
        }

        String userType = parts[0];
        String username = parts[1];
        String nickname = parts[2];
        String avatar = parts[3].isEmpty() ? null : parts[3];
        int status = parts.length > 4 ? Integer.parseInt(parts[4]) : 1;

        // 检查用户状态
        if (status == 0) {
            log.warn("用户已被停用（缓存），用户ID：{}", userId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        UserContext.LoginUser loginUser = new UserContext.LoginUser();
        loginUser.setUserId(userId);
        loginUser.setUsername(username);
        loginUser.setNickname(nickname);
        loginUser.setAvatar(avatar);
        UserContext.setUser(loginUser);

        log.debug("{}信息已从缓存恢复，用户ID：{}", "admin".equals(userType) ? "管理员" : "学生", userId);
        return true;
    }

    /**
     * 从数据库加载用户信息并写入缓存
     */
    private boolean loadUserFromDbAndCache(Long userId, String cacheKey, HttpServletResponse response) {
        UserContext.LoginUser loginUser = new UserContext.LoginUser();

        // 先查询管理员/宿管员
        User user = userMapper.selectById(userId);
        if (user != null) {
            if (user.getStatus() == 0) {
                log.warn("用户已被停用，用户ID：{}", userId);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            loginUser.setUserId(user.getId());
            loginUser.setUsername(user.getUsername());
            loginUser.setNickname(user.getNickname());
            loginUser.setAvatar(user.getAvatar());
            UserContext.setUser(loginUser);

            // 写入缓存
            String cacheValue = String.join(CACHE_SEPARATOR,
                    "admin",
                    nullSafe(user.getUsername()),
                    nullSafe(user.getNickname()),
                    nullSafe(user.getAvatar()),
                    String.valueOf(user.getStatus()));
            stringRedisTemplate.opsForValue().set(cacheKey, cacheValue, CACHE_TTL);

            log.debug("管理员信息已存入 ThreadLocal 和缓存，用户ID：{}", userId);
            return true;
        }

        // 查询学生
        Student student = studentMapper.selectById(userId);
        if (student == null) {
            log.warn("用户不存在，用户ID：{}（既不是管理员也不是学生）", userId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        if (student.getStatus() == 0) {
            log.warn("学生已被停用，学生ID：{}", userId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        loginUser.setUserId(student.getId());
        loginUser.setUsername(student.getStudentNo());
        loginUser.setNickname(student.getStudentName());
        loginUser.setAvatar(null);
        UserContext.setUser(loginUser);

        // 写入缓存
        String cacheValue = String.join(CACHE_SEPARATOR,
                "student",
                nullSafe(student.getStudentNo()),
                nullSafe(student.getStudentName()),
                "",
                String.valueOf(student.getStatus()));
        stringRedisTemplate.opsForValue().set(cacheKey, cacheValue, CACHE_TTL);

        log.debug("学生信息已存入 ThreadLocal 和缓存，学生ID：{}", userId);
        return true;
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清理 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelist(String path) {
        for (String whitePath : WHITELIST) {
            if (path.startsWith(whitePath)) {
                return true;
            }
        }
        return false;
    }
}
