package com.project.backend.system.controller;

import com.project.backend.system.dto.DictTypeQueryDTO;
import com.project.backend.system.dto.DictTypeSaveDTO;
import com.project.backend.system.service.DictDataService;
import com.project.backend.system.service.DictTypeService;
import com.project.backend.system.vo.DictDataVO;
import com.project.backend.system.vo.DictTypeVO;
import com.project.core.annotation.Log;
import com.project.core.annotation.PermissionAction;
import com.project.core.annotation.PermissionModule;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 字典类型控制器
 *
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/dict/type")
@RequiredArgsConstructor
@PermissionModule(
        value = "system:dict:type",
        view = "system:dict:view",
        add = "system:dict:type:add",
        edit = "system:dict:type:edit",
        delete = "system:dict:type:delete"
)
@Tag(name = "字典类型管理", description = "字典类型的增删改查")
public class DictTypeController {

    private final DictTypeService dictTypeService;
    private final DictDataService dictDataService;

    /**
     * 分页查询字典类型列表
     */
    @GetMapping("/page")
    @PermissionAction("view")
    @Operation(summary = "分页查询字典类型列表")
    public R<PageResult<DictTypeVO>> page(DictTypeQueryDTO queryDTO) {
        return R.ok(dictTypeService.pageList(queryDTO));
    }

    /**
     * 根据ID查询字典类型详情
     */
    @GetMapping("/{id}")
    @PermissionAction("view")
    @Operation(summary = "根据ID查询字典类型详情")
    @Parameter(name = "id", description = "主键ID", required = true)
    public R<DictTypeVO> getDetail(@PathVariable Long id) {
        return R.ok(dictTypeService.getDetailById(id));
    }

    /**
     * 新增字典类型
     */
    @PostMapping
    @PermissionAction("add")
    @Operation(summary = "新增字典类型")
    @Log(title = "新增字典类型", businessType = 1)
    public R<Void> add(@Valid @RequestBody DictTypeSaveDTO saveDTO) {
        saveDTO.setId(null);
        boolean success = dictTypeService.saveDictType(saveDTO);
        return success ? R.ok("字典类型新增成功", null) : R.fail("字典类型新增失败");
    }

    /**
     * 编辑字典类型
     */
    @PutMapping("/{id}")
    @PermissionAction("edit")
    @Operation(summary = "编辑字典类型")
    @Parameter(name = "id", description = "主键ID", required = true)
    @Log(title = "编辑字典类型", businessType = 2)
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody DictTypeSaveDTO saveDTO) {
        saveDTO.setId(id);
        boolean success = dictTypeService.saveDictType(saveDTO);
        return success ? R.ok("字典类型编辑成功", null) : R.fail("字典类型编辑失败");
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/{id}")
    @PermissionAction("delete")
    @Operation(summary = "删除字典类型")
    @Parameter(name = "id", description = "主键ID", required = true)
    @Log(title = "删除字典类型", businessType = 3)
    public R<Void> delete(@PathVariable Long id) {
        boolean success = dictTypeService.deleteDictType(id);
        return success ? R.ok("字典类型删除成功", null) : R.fail("字典类型删除失败");
    }

    /**
     * 批量获取字典数据
     *
     * @param request 字典编码列表
     * @return 字典数据Map，key为字典编码，value为对应的字典数据列表
     */
    @PostMapping("/batch")
    @Operation(summary = "批量获取字典数据", description = "根据字典编码列表批量获取字典数据，返回格式为 {dictCode1: [...], dictCode2: [...]}")
    public R<Map<String, List<DictDataVO>>> batchGetDictData(
            @Parameter(description = "字典编码列表", required = true)
            @RequestBody Map<String, List<String>> request) {
        List<String> dictCodes = request.get("dictCodes");
        log.info("批量获取字典数据，dictCodes：{}", dictCodes);

        if (dictCodes == null || dictCodes.isEmpty()) {
            return R.ok(Map.of());
        }

        return R.ok(dictDataService.listByDictCodes(dictCodes));
    }
}
