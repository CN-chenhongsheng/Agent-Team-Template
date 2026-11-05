package com.project.backend.system.service;

import com.project.backend.system.dto.UserImportDTO;
import com.project.backend.system.vo.UserImportResultVO;

/**
 * 用户导入服务
 *
 * @author 陈鸿昇
 * @since 2026-11-05
 */
public interface UserImportService {

    /**
     * 从上传后的 Excel 文件导入用户
     *
     * @param importDTO 导入请求
     * @return 导入结果
     */
    UserImportResultVO importUsers(UserImportDTO importDTO);
}
