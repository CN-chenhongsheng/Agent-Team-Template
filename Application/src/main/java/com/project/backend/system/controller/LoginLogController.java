package com.project.backend.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.project.backend.system.dto.LoginLogQueryDTO;
import com.project.backend.system.service.LoginLogService;
import com.project.backend.system.vo.LoginLogVO;
import com.project.core.annotation.Log;
import com.project.core.annotation.PermissionAction;
import com.project.core.annotation.PermissionModule;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 登入日志控制器
 *
 * @author 陈鸿昇
 * @since 2026-03-08
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/login-log")
@RequiredArgsConstructor
@PermissionModule(value = "system:loginlog")
@Tag(name = "登入日志管理", description = "登入日志的查询、删除等")
public class LoginLogController {

    private final LoginLogService loginLogService;

    @GetMapping("/page")
    @PermissionAction("view")
    @Operation(summary = "分页查询登入日志列表")
    public R<PageResult<LoginLogVO>> page(LoginLogQueryDTO queryDTO) {
        log.info("分页查询登入日志，参数：{}", queryDTO);
        PageResult<LoginLogVO> result = loginLogService.pageList(queryDTO);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    @SaCheckPermission(value = "system:loginlog:detail", orRole = "SUPER_ADMIN")
    @Operation(summary = "根据ID查询登入日志详情")
    @Parameter(name = "id", description = "日志ID", required = true)
    public R<LoginLogVO> getDetail(@PathVariable Long id) {
        log.info("查询登入日志详情，ID：{}", id);
        LoginLogVO loginLogVO = loginLogService.getDetailById(id);
        return R.ok(loginLogVO);
    }

    @DeleteMapping("/batch")
    @PermissionAction("delete")
    @Operation(summary = "批量删除登入日志")
    @Log(title = "批量删除登入日志", businessType = 3)
    public R<Void> batchDelete(@RequestBody Long[] ids) {
        log.info("批量删除登入日志，ID：{}", (Object) ids);
        boolean success = loginLogService.batchDelete(ids);
        return success ? R.ok("登入日志批量删除成功", null) : R.fail("登入日志批量删除失败");
    }

    @DeleteMapping("/clean")
    @SaCheckPermission(value = "system:loginlog:clean", orRole = "SUPER_ADMIN")
    @Operation(summary = "清空登入日志")
    @Log(title = "清空登入日志", businessType = 3)
    public R<Void> clean() {
        log.info("清空登入日志");
        boolean success = loginLogService.clean();
        return success ? R.ok("登入日志清空成功", null) : R.fail("登入日志清空失败");
    }
}
