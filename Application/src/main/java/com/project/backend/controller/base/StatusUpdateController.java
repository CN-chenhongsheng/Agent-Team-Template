package com.project.backend.controller.base;

import com.project.core.annotation.Log;
import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * 状态更新控制器接口
 * 提供状态更新方法
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
public interface StatusUpdateController {

    /**
     * 获取实体名称（用于日志输出）
     * 
     * @return 实体名称，如"校区"或"院系"
     */
    String getEntityName();

    /**
     * 调用Service的状态更新方法
     * 
     * @param id 主键ID
     * @param status 状态值（通常1启用 0停用）
     * @return 是否成功
     */
    boolean callUpdateStatus(Long id, Integer status);

    /**
     * 获取状态更新成功的消息
     * 
     * @param status 状态
     * @return 成功消息，如"校区已启用"或"校区已停用"
     */
    default String getStatusUpdateMessage(Integer status) {
        String entityName = getEntityName();
        if (status == 1) {
            return entityName + "已启用";
        } else {
            return entityName + "已停用";
        }
    }

    /**
     * 修改状态
     */
    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "修改状态")
    @Parameter(name = "id", description = "主键ID", required = true)
    @Parameter(name = "status", description = "状态：1启用 0停用", required = true)
    @Log(title = "修改状态", businessType = 2)
    default R<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        callUpdateStatus(id, status);
        String message = getStatusUpdateMessage(status);
        return R.ok(message, null);
    }
}
