package com.project.backend.system.controller;

import com.project.core.annotation.Log;
import com.project.core.result.R;
import com.project.backend.system.dto.MenuQueryDTO;
import com.project.backend.system.dto.MenuSaveDTO;
import com.project.backend.system.service.MenuService;
import com.project.backend.system.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统菜单控制器
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/menu")
@RequiredArgsConstructor
@Tag(name = "系统菜单管理", description = "菜单树形列表、增删改查等")
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @Operation(summary = "查询菜单树形列表", description = "返回树形结构的菜单列表")
    public R<List<MenuVO>> treeList(
            @Parameter(description = "菜单名称") @RequestParam(required = false) String menuName,
            @Parameter(description = "菜单类型：M目录 C菜单 F按钮") @RequestParam(required = false) String menuType,
            @Parameter(description = "状态：1正常 0停用") @RequestParam(required = false) Integer status
    ) {
        log.info("查询菜单树形列表，menuName：{}，menuType：{}，status：{}", menuName, menuType, status);

        // 构建查询DTO
        MenuQueryDTO queryDTO = new MenuQueryDTO();
        queryDTO.setMenuName(menuName);
        queryDTO.setMenuType(menuType);
        queryDTO.setStatus(status);

        List<MenuVO> tree = menuService.treeList(queryDTO);
        if (tree != null) {
            return R.ok(tree);
        } else {
            return R.fail("菜单树形列表为空");
        }
    }

    @GetMapping("/tree-select")
    @Operation(summary = "获取菜单树形选择器", description = "用于上级菜单选择，包含顶级菜单")
    public R<List<MenuVO>> getTreeSelect() {
        log.info("获取菜单树形选择器");
        List<MenuVO> tree = menuService.getMenuTreeSelect();
        if (tree != null) {
            return R.ok(tree);
        } else {
            return R.fail("菜单树形选择器为空");
        }
    }

    @GetMapping("/tree-permission")
    @Operation(summary = "获取菜单树用于权限分配", description = "包含所有类型（目录、菜单、按钮），不包含顶级菜单")
    public R<List<MenuVO>> getTreeForPermission() {
        log.info("获取菜单树用于权限分配");
        List<MenuVO> tree = menuService.getMenuTreeForPermission();
        if (tree != null) {
            return R.ok(tree);
        } else {
            return R.fail("菜单树用于权限分配为空");
        }
    }

    @GetMapping("/user-tree")
    @Operation(summary = "获取当前用户菜单树", description = "根据当前登录用户的角色权限返回有权限查看的菜单树")
    public R<List<MenuVO>> getUserMenuTree() {
        log.info("获取当前用户菜单树");
        List<MenuVO> tree = menuService.getUserMenuTree();
        return R.ok(tree);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询菜单详情")
    @Parameter(name = "id", description = "菜单ID", required = true)
    public R<MenuVO> getDetail(@PathVariable Long id) {
        log.info("查询菜单详情，ID：{}", id);
        MenuVO menuVO = menuService.getDetailById(id);
        if (menuVO != null) {
            return R.ok(menuVO);
        } else {
            return R.fail("菜单不存在");
        }
    }

    @PostMapping
    @Operation(summary = "新增菜单")
    @Log(title = "新增菜单", businessType = 1)
    public R<Void> add(@Valid @RequestBody MenuSaveDTO saveDTO) {
        log.info("新增菜单，参数：{}", saveDTO);
        saveDTO.setId(null); // 确保ID为空
        boolean success = menuService.saveMenu(saveDTO);
        if (success) {
            return R.ok("菜单新增成功", null);
        } else {
            return R.fail("菜单新增失败");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑菜单")
    @Parameter(name = "id", description = "菜单ID", required = true)
    @Log(title = "编辑菜单", businessType = 2)
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody MenuSaveDTO saveDTO) {
        log.info("编辑菜单，ID：{}，参数：{}", id, saveDTO);
        saveDTO.setId(id);
        boolean success = menuService.saveMenu(saveDTO);
        if (success) {
            return R.ok("菜单编辑成功", null);
        } else {
            return R.fail("菜单编辑失败");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    @Parameter(name = "id", description = "菜单ID", required = true)
    @Log(title = "删除菜单", businessType = 3)
    public R<Void> delete(@PathVariable Long id) {
        log.info("删除菜单，ID：{}", id);
        boolean success = menuService.deleteMenu(id);
        if (success) {
            return R.ok("菜单删除成功", null);
        } else {
            return R.fail("菜单删除失败");
        }
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "修改菜单状态")
    @Parameter(name = "id", description = "菜单ID", required = true)
    @Parameter(name = "status", description = "状态：1正常 0停用", required = true)
    @Log(title = "修改菜单状态", businessType = 2)
    public R<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        log.info("修改菜单状态，ID：{}，状态：{}", id, status);
        boolean success = menuService.updateStatus(id, status);
        if (success) {
            return R.ok(status == 1 ? "菜单已启用" : "菜单已停用", null);
        } else {
            return R.fail(status == 1 ? "菜单启用失败" : "菜单停用失败");
        }
    }
}
