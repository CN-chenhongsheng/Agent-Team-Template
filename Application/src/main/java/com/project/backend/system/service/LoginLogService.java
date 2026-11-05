package com.project.backend.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.system.dto.LoginLogQueryDTO;
import com.project.backend.system.entity.LoginLog;
import com.project.backend.system.vo.LoginLogVO;
import com.project.core.result.PageResult;

/**
 * 登入日志Service
 *
 * @author 陈鸿昇
 * @since 2026-03-08
 */
public interface LoginLogService extends IService<LoginLog> {

    /**
     * 分页查询登入日志列表
     */
    PageResult<LoginLogVO> pageList(LoginLogQueryDTO queryDTO);

    /**
     * 根据ID查询登入日志详情
     */
    LoginLogVO getDetailById(Long id);

    /**
     * 批量删除登入日志
     */
    boolean batchDelete(Long[] ids);

    /**
     * 清空登入日志
     */
    boolean clean();

    /**
     * 记录登入日志
     *
     * @param username  用户名
     * @param loginType 登录类型
     * @param status    登录状态
     * @param message   提示消息
     * @param ip        客户端IP（需在请求线程中提取）
     * @param userAgent User-Agent（需在请求线程中提取）
     */
    void recordLoginLog(String username, String loginType, Integer status, String message,
                        String ip, String userAgent);
}
