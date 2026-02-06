package com.project.backend.system.controller;

import com.project.core.result.PageResult;
import com.project.core.result.R;
import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.system.dto.DictTypeQueryDTO;
import com.project.backend.system.dto.DictTypeSaveDTO;
import com.project.backend.system.service.DictDataService;
import com.project.backend.system.service.DictTypeService;
import com.project.backend.system.vo.DictDataVO;
import com.project.backend.system.vo.DictTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
@Tag(name = "字典类型管理", description = "字典类型的增删改查")
public class DictTypeController extends BaseCrudController<DictTypeVO, DictTypeQueryDTO, DictTypeSaveDTO> {

    private final DictTypeService dictTypeService;
    private final DictDataService dictDataService;

    @Override
    public String getEntityName() {
        return "字典类型";
    }

    @Override
    protected PageResult<DictTypeVO> callPageList(DictTypeQueryDTO queryDTO) {
        return dictTypeService.pageList(queryDTO);
    }

    @Override
    protected DictTypeVO callGetDetailById(Long id) {
        return dictTypeService.getDetailById(id);
    }

    @Override
    protected boolean callSave(DictTypeSaveDTO saveDTO) {
        return dictTypeService.saveDictType(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return dictTypeService.deleteDictType(id);
    }

    /**
     * 批量获取字典数据
     *
     * @param dictCodes 字典编码列表
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

        Map<String, List<DictDataVO>> result = dictDataService.listByDictCodes(dictCodes);
        if (result != null) {
            return R.ok(result);
        } else {
            return R.fail("字典数据列表为空");
        }
    }
}
