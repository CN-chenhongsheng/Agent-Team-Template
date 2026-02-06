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

    @GetMapping("/list")
    @Operation(summary = "查询角色列表（分页）", description = "支持按角色编码、角色名称、状态查询")
    public R<PageResult<RoleVO>> list(RoleQueryDTO queryDTO) {
        log.info("查询角色列表，参数：{}", queryDTO);
        PageResult<RoleVO> result = roleService.pageList(queryDTO);
        if (result != null) {
            return R.ok(result);
        } else {
            return R.fail("角色列表为空");
        }
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有角色（不分页）", description = "用于下拉选择等场景")
    public R<List<RoleVO>> listAll() {
        log.info("查询所有角色");
        List<RoleVO> list = roleService.listAll();
        if (list != null) {
            return R.ok(list);
        } else {
            return R.fail("角色列表为空");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询角色详情")
    @Parameter(name = "id", description = "角色ID", required = true)
    public R<RoleVO> getDetail(@PathVariable Long id) {
        log.info("查询角色详情，ID：{}", id);
        RoleVO roleVO = roleService.getDetailById(id);
        if (roleVO != null) {
            return R.ok(roleVO);
        } else {
            return R.fail("角色不存在");
        }
    }

    @PostMapping
    @Operation(summary = "新增角色")
    @Log(title = "新增角色", businessType = 1)
    public R<Void> add(@Valid @RequestBody RoleSaveDTO saveDTO) {
        log.info("新增角色，参数：{}", saveDTO);
        saveDTO.setId(null); // 确保ID为空
        boolean success = roleService.saveRole(saveDTO);
        if (success) {
            return R.ok("角色新增成功", null);
        } else {
            return R.fail("角色新增失败");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑角色")
    @Parameter(name = "id", description = "角色ID", required = true)
    @Log(title = "编辑角色", businessType = 2)
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveDTO saveDTO) {
        log.info("编辑角色，ID：{}，参数：{}", id, saveDTO);
        saveDTO.setId(id);
        boolean success = roleService.saveRole(saveDTO);
        if (success) {
            return R.ok("角色编辑成功", null);
        } else {
            return R.fail("角色编辑失败");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @Parameter(name = "id", description = "角色ID", required = true)
    @Log(title = "删除角色", businessType = 3)
    public R<Void> delete(@PathVariable Long id) {
        log.info("删除角色，ID：{}", id);
        boolean success = roleService.deleteRole(id);
        if (success) {
            return R.ok("角色删除成功", null);
        } else {
            return R.fail("角色删除失败");
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除角色")
    @Log(title = "批量删除角色", businessType = 3)
    public R<Void> batchDelete(@RequestBody Long[] ids) {
        log.info("批量删除角色，IDs：{}", (Object) ids);
        boolean success = roleService.batchDelete(ids);
        if (success) {
            return R.ok("角色批量删除成功", null);
        } else {
            return R.fail("角色批量删除失败");
        }
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "分配角色菜单权限")
    @Parameter(name = "id", description = "角色ID", required = true)
    @Log(title = "分配角色权限", businessType = 0)
    public R<Void> assignPermissions(@PathVariable Long id, @RequestBody Long[] menuIds) {
        log.info("分配角色菜单权限，角色ID：{}，菜单ID：{}", id, (Object) menuIds);
        boolean success = roleService.assignMenus(id, menuIds);
        if (success) {
            return R.ok("权限分配成功", null);
        } else {
            return R.fail("权限分配失败");
        }
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取角色的菜单权限列表（包含菜单状态）")
    @Parameter(name = "id", description = "角色ID", required = true)
    public R<List<RolePermissionVO>> getPermissions(@PathVariable Long id) {
        log.info("获取角色菜单权限，角色ID：{}", id);
        List<RolePermissionVO> permissions = roleService.getRolePermissions(id);
        if (permissions != null) {
            return R.ok(permissions);
        } else {
            return R.fail("角色权限列表为空");
        }
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "修改角色状态")
    @Parameter(name = "id", description = "角色ID", required = true)
    @Parameter(name = "status", description = "状态：1正常 0停用", required = true)
    @Log(title = "修改角色状态", businessType = 2)
    public R<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        log.info("修改角色状态，ID：{}，状态：{}", id, status);
        boolean success = roleService.updateStatus(id, status);
        if (success) {
            return R.ok(status == 1 ? "角色已启用" : "角色已停用", null);
        } else {
            return R.fail(status == 1 ? "角色启用失败" : "角色停用失败");
        }
    }
}
