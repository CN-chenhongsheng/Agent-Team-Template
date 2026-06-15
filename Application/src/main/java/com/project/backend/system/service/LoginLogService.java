package com.project.backend.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.system.dto.LoginLogQueryDTO;
import com.project.backend.system.entity.LoginLog;
import com.project.backend.system.vo.LoginLogVO;
import com.project.core.result.PageResult;
import jakarta.servlet.http.HttpServletRequest;

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
     */
    void recordLoginLog(String username, String loginType, Integer status, String message, HttpServletRequest request);
}
