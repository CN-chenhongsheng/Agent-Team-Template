package com.project.backend.controller.base;

import com.project.core.annotation.Log;
import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 批量删除控制器接口
 * 提供批量删除方法
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
public interface BatchDeleteController {

    /**
     * 获取实体名称（用于日志输出）
     * 
     * @return 实体名称，如"校区"或"院系"
     */
    String getEntityName();

    /**
     * 调用Service的批量删除方法
     * 
     * @param ids 主键ID数组
     * @return 是否成功
     */
    boolean callBatchDelete(Long[] ids);

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    @Log(title = "批量删除", businessType = 3)
    default R<Void> batchDelete(@RequestBody Long[] ids) {
        boolean success = callBatchDelete(ids);
        if (success) {
            return R.ok(getEntityName() + "批量删除成功", null);
        } else {
            return R.fail(getEntityName() + "批量删除失败");
        }
    }
}
