package com.project.backend.room.controller;

import com.project.core.result.PageResult;
import com.project.core.result.R;
import com.project.backend.controller.base.BaseCrudController;
import com.project.backend.controller.base.BatchDeleteController;
import com.project.backend.controller.base.StatusUpdateController;
import com.project.backend.room.dto.floor.FloorQueryDTO;
import com.project.backend.room.dto.floor.FloorSaveDTO;
import com.project.backend.room.service.FloorService;
import com.project.backend.room.vo.FloorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 楼层管理控制器
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/floor")
@RequiredArgsConstructor
@Tag(name = "楼层管理", description = "楼层增删改查")
public class FloorController extends BaseCrudController<FloorVO, FloorQueryDTO, FloorSaveDTO>
        implements BatchDeleteController, StatusUpdateController {

    private final FloorService floorService;

    @Override
    public String getEntityName() {
        return "楼层";
    }

    @Override
    protected PageResult<FloorVO> callPageList(FloorQueryDTO queryDTO) {
        return floorService.pageList(queryDTO);
    }

    @Override
    protected FloorVO callGetDetailById(Long id) {
        return floorService.getDetailById(id);
    }

    @Override
    protected boolean callSave(FloorSaveDTO saveDTO) {
        return floorService.saveFloor(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return floorService.deleteFloor(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return floorService.batchDelete(ids);
    }

    @Override
    public boolean callUpdateStatus(Long id, Integer status) {
        return floorService.updateStatus(id, status);
    }

    /**
     * 检查楼层是否被房间关联
     * 
     * @param id 楼层ID
     * @return true-有房间关联，false-无房间关联
     */
    @GetMapping("/{id}/check-rooms")
    @Operation(summary = "检查楼层是否被房间关联")
    @Parameter(name = "id", description = "楼层ID", required = true)
    public R<Boolean> checkFloorHasRooms(@PathVariable Long id) {
        boolean hasRooms = floorService.checkFloorHasRooms(id);
        return R.ok(hasRooms);
    }
}
