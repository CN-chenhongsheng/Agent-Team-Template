package com.project.backend.system.controller;

import com.project.core.annotation.Log;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import com.project.backend.system.dto.RoleUserQueryDTO;
import com.project.backend.system.dto.UserQueryDTO;
import com.project.backend.system.dto.UserResetPasswordDTO;
import com.project.backend.system.dto.UserSaveDTO;
import com.project.backend.system.service.UserService;
import com.project.backend.system.vo.UserPermissionVO;
import com.project.backend.system.vo.UserSimpleVO;
import com.project.backend.system.vo.UserVO;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 系统用户控制器
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/user")
@RequiredArgsConstructor
@Tag(name = "系统用户管理", description = "用户增删改查、重置密码、状态管理等")
public class UserController {

    private final UserService userService;

    @GetMapping("/page")
    @Operation(summary = "查询用户列表（分页）", description = "支持按用户名、昵称、手机号、学院、状态查询")
    public R<PageResult<UserVO>> list(UserQueryDTO queryDTO) {
        return R.ok(userService.pageList(queryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户详情")
    @Parameter(name = "id", description = "用户ID", required = true)
    public R<UserVO> getDetail(@PathVariable Long id) {
        return R.ok(userService.getDetailById(id));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    @Log(title = "新增用户", businessType = 1)
    public R<Void> add(@Valid @RequestBody UserSaveDTO saveDTO) {
        saveDTO.setId(null);
        boolean success = userService.saveUser(saveDTO);
        return success ? R.ok("用户新增成功", null) : R.fail("用户新增失败");
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑用户")
    @Parameter(name = "id", description = "用户ID", required = true)
    @Log(title = "编辑用户", businessType = 2)
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UserSaveDTO saveDTO) {
        saveDTO.setId(id);
        boolean success = userService.saveUser(saveDTO);
        return success ? R.ok("用户编辑成功", null) : R.fail("用户编辑失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @Parameter(name = "id", description = "用户ID", required = true)
    @Log(title = "删除用户", businessType = 3)
    public R<Void> delete(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        return success ? R.ok("用户删除成功", null) : R.fail("用户删除失败");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户")
    @Log(title = "批量删除用户", businessType = 3)
    public R<Void> batchDelete(@RequestBody Long[] ids) {
        boolean success = userService.batchDelete(ids);
        return success ? R.ok("用户批量删除成功", null) : R.fail("用户批量删除失败");
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置用户密码")
    @Parameter(name = "id", description = "用户ID", required = true)
    @Log(title = "重置用户密码", businessType = 0)
    public R<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody UserResetPasswordDTO resetDTO) {
        resetDTO.setUserId(id);
        boolean success = userService.resetPassword(resetDTO);
        return success ? R.ok("密码重置成功", null) : R.fail("密码重置失败");
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "修改用户状态")
    @Parameter(name = "id", description = "用户ID", required = true)
    @Parameter(name = "status", description = "状态：1正常 0停用", required = true)
    @Log(title = "修改用户状态", businessType = 2)
    public R<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        boolean success = userService.updateStatus(id, status);
        return success ? R.ok(status == 1 ? "用户已启用" : "用户已停用", null)
                : R.fail(status == 1 ? "用户启用失败" : "用户停用失败");
    }

    @PostMapping("/by-roles")
    @Operation(summary = "根据角色代码列表查询用户列表", description = "支持多个角色代码，返回Map格式，key为角色代码，value为用户列表")
    public R<Map<String, List<UserSimpleVO>>> getUsersByRoleCodes(@Valid @RequestBody RoleUserQueryDTO queryDTO) {
        return R.ok(userService.getUsersByRoleCodes(queryDTO));
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取用户权限列表", description = "返回用户已分配的权限列表（包含菜单状态）")
    @Parameter(name = "id", description = "用户ID", required = true)
    public R<List<UserPermissionVO>> getUserPermissions(@PathVariable Long id) {
        return R.ok(userService.getUserPermissions(id));
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "分配用户权限", description = "分配用户菜单权限，权限范围必须是用户所有角色的权限并集")
    @Parameter(name = "id", description = "用户ID", required = true)
    @Log(title = "分配用户权限", businessType = 0)
    public R<Void> assignUserPermissions(@PathVariable Long id, @RequestBody Long[] menuIds) {
        boolean success = userService.assignMenus(id, menuIds);
        return success ? R.ok("权限分配成功", null) : R.fail("权限分配失败");
    }

    @GetMapping("/{id}/available-menus")
    @Operation(summary = "获取用户可选的菜单列表", description = "返回用户所有角色的权限并集，用于权限分配界面")
    @Parameter(name = "id", description = "用户ID", required = true)
    public R<List<Long>> getUserAvailableMenus(@PathVariable Long id) {
        return R.ok(userService.getUserAvailableMenuIds(id));
    }
}
