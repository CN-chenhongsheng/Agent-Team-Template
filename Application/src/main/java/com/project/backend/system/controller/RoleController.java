package com.project.backend.system.controller;

import com.project.core.annotation.Log;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import com.project.backend.system.dto.RoleQueryDTO;
import com.project.backend.system.dto.RoleSaveDTO;
import com.project.backend.system.service.RoleService;
import com.project.backend.system.vo.RolePermissionVO;
import com.project.backend.system.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色控制器
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/role")
@RequiredArgsConstructor
@Tag(name = "系统角色管理", description = "角色增删改查、权限分配等")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/page")
    @Operation(summary = "查询角色列表（分页）", description = "支持按角色编码、角色名称、状态查询")
    public R<PageResult<RoleVO>> list(RoleQueryDTO queryDTO) {
        return R.ok(roleService.pageList(queryDTO));
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有角色（不分页）", description = "用于下拉选择等场景")
    public R<List<RoleVO>> listAll() {
        return R.ok(roleService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询角色详情")
    @Parameter(name = "id", description = "角色ID", required = true)
    public R<RoleVO> getDetail(@PathVariable Long id) {
        return R.ok(roleService.getDetailById(id));
    }

    @PostMapping
    @Operation(summary = "新增角色")
    @Log(title = "新增角色", businessType = 1)
    public R<Void> add(@Valid @RequestBody RoleSaveDTO saveDTO) {
        saveDTO.setId(null);
        boolean success = roleService.saveRole(saveDTO);
        return success ? R.ok("角色新增成功", null) : R.fail("角色新增失败");
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑角色")
    @Parameter(name = "id", description = "角色ID", required = true)
    @Log(title = "编辑角色", businessType = 2)
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveDTO saveDTO) {
        saveDTO.setId(id);
        boolean success = roleService.saveRole(saveDTO);
        return success ? R.ok("角色编辑成功", null) : R.fail("角色编辑失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @Parameter(name = "id", description = "角色ID", required = true)
    @Log(title = "删除角色", businessType = 3)
    public R<Void> delete(@PathVariable Long id) {
        boolean success = roleService.deleteRole(id);
        return success ? R.ok("角色删除成功", null) : R.fail("角色删除失败");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除角色")
    @Log(title = "批量删除角色", businessType = 3)
    public R<Void> batchDelete(@RequestBody Long[] ids) {
        boolean success = roleService.batchDelete(ids);
        return success ? R.ok("角色批量删除成功", null) : R.fail("角色批量删除失败");
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "分配角色菜单权限")
    @Parameter(name = "id", description = "角色ID", required = true)
    @Log(title = "分配角色权限", businessType = 0)
    public R<Void> assignPermissions(@PathVariable Long id, @RequestBody Long[] menuIds) {
        boolean success = roleService.assignMenus(id, menuIds);
        return success ? R.ok("权限分配成功", null) : R.fail("权限分配失败");
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取角色的菜单权限列表（包含菜单状态）")
    @Parameter(name = "id", description = "角色ID", required = true)
    public R<List<RolePermissionVO>> getPermissions(@PathVariable Long id) {
        return R.ok(roleService.getRolePermissions(id));
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "修改角色状态")
    @Parameter(name = "id", description = "角色ID", required = true)
    @Parameter(name = "status", description = "状态：1正常 0停用", required = true)
    @Log(title = "修改角色状态", businessType = 2)
    public R<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        boolean success = roleService.updateStatus(id, status);
        return success ? R.ok(status == 1 ? "角色已启用" : "角色已停用", null)
                : R.fail(status == 1 ? "角色启用失败" : "角色停用失败");
    }
}
