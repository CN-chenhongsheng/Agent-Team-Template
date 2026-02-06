package com.project.backend.config.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.project.core.context.UserContext;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.system.entity.User;
import com.project.backend.system.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 验证 Token 并将用户信息存入 ThreadLocal
 * 支持管理员（sys_user）和学生（sys_student）两种类型的认证
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
            String token = null;

            // 1. 优先从 Authorization 请求头读取 Bearer Token
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && !authHeader.isEmpty()) {
                // 支持 Bearer 格式：Bearer <token>
                if (authHeader.startsWith("Bearer ") || authHeader.startsWith("bearer ")) {
                    token = authHeader.substring(7).trim();
                    log.debug("从 Authorization 请求头读取 Bearer Token");
                } else {
                    // 兼容旧格式：直接是 token 值
                    token = authHeader.trim();
                    log.debug("从 Authorization 请求头读取 Token（非 Bearer 格式）");
                }
            }

            // 2. 如果请求头中没有 token，且 URL 参数中有 token，则从 URL 参数读取（用于 SSE）
            String tokenParam = request.getParameter("token");
            if (token == null && tokenParam != null && !tokenParam.isEmpty()) {
                token = tokenParam;
                log.debug("从 URL 参数读取 token（用于 SSE）");
            }

            // 3. 如果仍然没有 token，返回 401
            if (token == null || token.isEmpty()) {
                log.warn("请求中未找到 Token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 4. 验证 Token 并获取用户ID
            try {
                // 使用提取的 token 值进行验证（不包含 Bearer 前缀）
                // 注意：这里直接使用 token 值，不包含 "Bearer " 前缀
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId == null) {
                    log.warn("Token 无效或已过期，Token 前10字符: {}", token != null && token.length() > 10 ? token.substring(0, 10) : token);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return false;
                }
                Long userId = Long.valueOf(loginId.toString());
                log.debug("Token 验证成功，用户ID: {}", userId);

                // 5. 查询用户信息：先查询管理员/宿管员（sys_user），如果不存在则查询学生（sys_student）
                User user = userMapper.selectById(userId);
                UserContext.LoginUser loginUser = new UserContext.LoginUser();

                if (user != null) {
                    // 管理员/宿管员登录
                    // 6. 检查用户状态
                    if (user.getStatus() == 0) {
                        log.warn("用户已被停用，用户ID：{}", userId);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return false;
                    }

                    // 7. 将用户信息存入 ThreadLocal
                    loginUser.setUserId(user.getId());
                    loginUser.setUsername(user.getUsername());
                    loginUser.setNickname(user.getNickname());
                    loginUser.setAvatar(user.getAvatar());
                    UserContext.setUser(loginUser);

                    log.debug("管理员/宿管员信息已存入 ThreadLocal，用户ID：{}，用户名：{}", userId, user.getUsername());
                } else {
                    // 学生登录，查询学生信息
                    Student student = studentMapper.selectById(userId);
                    if (student == null) {
                        log.warn("用户不存在，用户ID：{}（既不是管理员也不是学生）", userId);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return false;
                    }

                    // 6. 检查学生状态
                    if (student.getStatus() == 0) {
                        log.warn("学生已被停用，学生ID：{}", userId);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return false;
                    }

                    // 7. 将学生信息存入 ThreadLocal
                    loginUser.setUserId(student.getId());
                    loginUser.setUsername(student.getStudentNo());
                    loginUser.setNickname(student.getStudentName());
                    loginUser.setAvatar(null); // 学生暂无头像
                    UserContext.setUser(loginUser);

                    log.debug("学生信息已存入 ThreadLocal，学生ID：{}，学号：{}", userId, student.getStudentNo());
                }

                return true;
            } catch (Exception e) {
                log.warn("Token 验证失败：{}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        } catch (Exception e) {
            log.warn("Token 验证失败：{}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
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
