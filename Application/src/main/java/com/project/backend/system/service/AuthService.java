package com.project.backend.system.service;

import com.project.backend.system.dto.LoginDTO;
import com.project.backend.system.dto.StudentLoginDTO;
import com.project.backend.system.dto.WxLoginDTO;
import com.project.backend.system.vo.LoginVO;
import com.project.backend.system.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证服务接口
 *
 * @author 陈鸿昇
 * @since 2025-12-30
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 登录结果
     */
    LoginVO login(LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response);

    /**
     * 学生登录
     *
     * @param studentLoginDTO 学生登录参数
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 登录结果
     */
    LoginVO studentLogin(StudentLoginDTO studentLoginDTO, HttpServletRequest request, HttpServletResponse response);

    /**
     * 微信小程序登录
     *
     * @param wxLoginDTO 微信登录参数
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 登录结果
     */
    LoginVO wxLogin(WxLoginDTO wxLoginDTO, HttpServletRequest request, HttpServletResponse response);

    /**
     * 刷新 Access Token
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 新的 Access Token
     */
    String refreshToken(HttpServletRequest request, HttpServletResponse response);

    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @param response HTTP响应
     */
    void logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    UserInfoVO getCurrentUserInfo();
}
