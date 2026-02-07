package com.project.backend.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.core.exception.BusinessException;
import com.project.backend.system.dto.LoginDTO;
import com.project.backend.system.dto.StudentLoginDTO;
import com.project.backend.system.dto.WxLoginDTO;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.student.vo.StudentVO;
import com.project.backend.system.entity.User;
import com.project.backend.system.mapper.MenuMapper;
import com.project.backend.system.mapper.RoleMapper;
import com.project.backend.system.mapper.UserMapper;
import com.project.backend.system.service.AuthService;
import com.project.backend.system.service.UserOnlineService;
import com.project.core.util.RefreshTokenUtil;
import com.project.backend.system.vo.LoginVO;
import com.project.backend.system.vo.UserInfoVO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务实现类
 *
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserOnlineService userOnlineService;
    private final StudentMapper studentMapper;

    /**
     * Refresh Token Cookie 名称
     */
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    /**
     * Refresh Token Cookie 有效期（7天，单位：秒）
     */
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

    /**
     * 是否在生产环境（用于设置 Secure Cookie）
     */
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Override
    public LoginVO login(LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        log.info("用户登录: {}", loginDTO.getUsername());

        // 1. 查询并验证用户
        User user = validateUserCredentials(loginDTO);

        // 2. 执行登录并生成令牌
        String accessToken = performLogin(user.getId());

        // 3. 生成并设置Refresh Token
        generateRefreshToken(user.getId(), response);

        // 4. 更新最后登录时间
        updateLastLoginTime(user);

        // 5. 构建返回数据
        return buildLoginResponse(user, accessToken);
    }

    /**
     * 验证用户凭据
     */
    private User validateUserCredentials(LoginDTO loginDTO) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginDTO.getUsername())
        );

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被停用，请联系管理员");
        }

        return user;
    }

    /**
     * 执行登录并生成Access Token
     */
    private String performLogin(Long userId) {
        try {
            StpUtil.login(userId);
            userOnlineService.userOnline(userId);
            return StpUtil.getTokenValue();
        } catch (Exception e) {
            log.error("登录失败", e);
            throw new BusinessException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 生成并设置Refresh Token
     */
    private void generateRefreshToken(Long userId, HttpServletResponse response) {
        try {
            String refreshToken = RefreshTokenUtil.login(userId);
            setRefreshTokenCookie(response, refreshToken);
        } catch (Exception e) {
            log.warn("Refresh Token生成失败，但登录继续", e);
        }
    }

    /**
     * 更新最后登录时间
     */
    private void updateLastLoginTime(User user) {
        try {
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
        } catch (Exception e) {
            log.warn("更新登录时间失败", e);
        }
    }

    /**
     * 构建登录响应
     */
    private LoginVO buildLoginResponse(User user, String accessToken) {
        return LoginVO.builder()
                .token(accessToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public String refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("开始刷�?Access Token");

        // 1. 获取并验证Refresh Token
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException("会话已过期，请重新登录");
        }

        // 2. 验证 Token 并获取用户ID
        Long userId = RefreshTokenUtil.getLoginIdByToken(refreshToken);
        if (userId == null) {
            clearRefreshTokenCookie(response);
            throw new BusinessException("会话已过期，请重新登录");
        }

        // 3. 验证用户状态
        validateUserStatus(userId, response);

        // 4. 生成新的 Access Token
        return generateNewAccessToken(userId);
    }

    /**
     * 验证用户状态
     */
    private void validateUserStatus(Long userId, HttpServletResponse response) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            clearRefreshTokenCookie(response);
            throw new BusinessException("用户不存在");
        }

        if (user.getStatus() == 0) {
            clearRefreshTokenCookie(response);
            throw new BusinessException("账号已被停用，请联系管理员");
        }
    }

    /**
     * 生成新的 Access Token
     */
    private String generateNewAccessToken(Long userId) {
        try {
            // 先登出旧Access Token（如果存在）
            try {
                StpUtil.logout(userId);
            } catch (Exception ignored) {
                // 忽略异常，可能没有旧token
            }

            StpUtil.login(userId);
            log.info("Access Token 刷新成功，用户ID: {}", userId);
            return StpUtil.getTokenValue();
        } catch (Exception e) {
            log.error("生成Access Token 失败", e);
            throw new BusinessException("生成�?Access Token 失败: " + e.getMessage());
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 获取用户ID（优先从 Access Token，其次从 Refresh Token）
        Long userId = getUserIdForLogout(request);

        // 清除 Access Token
        try {
            StpUtil.logout();
        } catch (Exception e) {
            log.warn("清除 Access Token 失败: {}", e.getMessage());
        }

        // 清除 Refresh Token
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            RefreshTokenUtil.logout(refreshToken);
        }

        // 清除 Refresh Token Cookie
        clearRefreshTokenCookie(response);

        // 更新用户在线状态
        if (userId != null) {
            userOnlineService.userOffline(userId);
        }

        log.info("用户登出成功，用户ID: {}", userId);
    }

    /**
     * 获取用户ID用于登出（优先从 Access Token，其次从 Refresh Token）
     */
    private Long getUserIdForLogout(HttpServletRequest request) {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            log.debug("无法从Access Token 获取用户ID，尝试从 Refresh Token 获取");
            String refreshToken = getRefreshTokenFromCookie(request);
            return refreshToken != null ? RefreshTokenUtil.getLoginIdByToken(refreshToken) : null;
        }
    }


    @Override
    public UserInfoVO getCurrentUserInfo() {
        // 1. 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 2. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 查询用户角色
        List<String> roles = roleMapper.selectRoleCodesByUserId(userId);

        // 4. 查询用户权限
        List<String> permissions = menuMapper.selectPermissionsByUserId(userId);

        // 5. 构建返回数据
        return UserInfoVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .manageScope(user.getManageScope())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    /**
     * 设置 Refresh Token Cookie
     *
     * @param response HTTP响应
     * @param refreshToken Refresh Token 值
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        String cookieHeader = buildCookieHeader(refreshToken, REFRESH_TOKEN_COOKIE_MAX_AGE);
        response.setHeader("Set-Cookie", cookieHeader);
        log.info("设置 Refresh Token Cookie: {} (Max-Age={}, SameSite={})",
                REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN_COOKIE_MAX_AGE,
                isProdEnvironment() ? "Strict" : "Lax");
    }

    /**
     * 从Cookie 中获取Refresh Token
     *
     * @param request HTTP请求
     * @return Refresh Token 值，如果不存在返回null
     */
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 清除 Refresh Token Cookie
     *
     * @param response HTTP响应
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        String cookieHeader = buildCookieHeader("", 0);
        response.setHeader("Set-Cookie", cookieHeader);
        log.debug("清除 Refresh Token Cookie: {}", REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * 构建 Cookie Header 字符串
     *
     * @param value Cookie 值
     * @param maxAge Cookie 有效期（秒）
     * @return Cookie Header 字符串
     */
    private String buildCookieHeader(String value, int maxAge) {
        return String.format("%s=%s; Path=/api; Max-Age=%d; HttpOnly; SameSite=%s%s",
                REFRESH_TOKEN_COOKIE_NAME,
                value,
                maxAge,
                isProdEnvironment() ? "Strict" : "Lax",
                isProdEnvironment() ? "; Secure" : "");
    }

    /**
     * 判断是否为生产环境
     */
    private boolean isProdEnvironment() {
        return "prod".equals(activeProfile);
    }

    @Override
    public LoginVO studentLogin(StudentLoginDTO studentLoginDTO, HttpServletRequest request, HttpServletResponse response) {
        log.info("学生登录: {}", studentLoginDTO.getStudentNo());

        // 1. 查询并验证学生
        Student student = validateStudentCredentials(studentLoginDTO);

        // 2. 执行登录并生成令牌
        String accessToken = performLogin(student.getId());

        // 3. 生成并设置Refresh Token
        generateRefreshToken(student.getId(), response);

        // 4. 构建返回数据
        return buildStudentLoginResponse(student, accessToken);
    }

    @Override
    public LoginVO wxLogin(WxLoginDTO wxLoginDTO, HttpServletRequest request, HttpServletResponse response) {
        log.info("微信小程序登录, code={}", wxLoginDTO.getCode());

        // 生产环境禁止使用 code 直接作为 openid，必须调用微信 API
        if (isProdEnvironment()) {
            // TODO: 实现真实微信 API 调用
            // String openid = getOpenIdFromWxCode(wxLoginDTO.getCode());
            throw new BusinessException("微信登录接口尚未对接微信API，请使用学号密码登录");
        }

        // 开发/测试环境：使用 code 作为 openid（仅限非生产环境）
        log.warn("【非生产环境】微信登录使用 code 直接作为 openid，请勿在生产环境使用");
        String openid = wxLoginDTO.getCode();

        // 根据 openid 查询学生
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getOpenid, openid)
        );

        if (student == null) {
            throw new BusinessException("未找到绑定的学生信息，请先绑定学号");
        }

        if (student.getStatus() == 0) {
            throw new BusinessException("账号已被停用，请联系管理员");
        }

        // 执行登录并生成令牌
        String accessToken = performLogin(student.getId());

        // 生成并设置Refresh Token
        generateRefreshToken(student.getId(), response);

        // 构建返回数据
        return buildStudentLoginResponse(student, accessToken);
    }

    /**
     * 验证学生凭据
     */
    private Student validateStudentCredentials(StudentLoginDTO loginDTO) {
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getStudentNo, loginDTO.getStudentNo())
        );

        if (student == null) {
            throw new BusinessException("学号或密码错误");
        }

        // 验证密码
        if (student.getPassword() == null || student.getPassword().isBlank()) {
            throw new BusinessException("密码未设置，请联系管理员");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), student.getPassword())) {
            throw new BusinessException("学号或密码错误");
        }

        if (student.getStatus() == 0) {
            throw new BusinessException("账号已被停用，请联系管理员");
        }

        return student;
    }

    /**
     * 构建学生登录响应
     */
    private LoginVO buildStudentLoginResponse(Student student, String accessToken) {
        // 转换学生信息VO
        StudentVO studentVO = new StudentVO();
        BeanUtil.copyProperties(student, studentVO);

        return LoginVO.builder()
                .token(accessToken)
                .userId(student.getId())
                .username(student.getStudentNo())
                .nickname(student.getStudentName())
                .avatar(null) // 学生暂无头像
                .role("student")
                .studentInfo(studentVO)
                .build();
    }
}
