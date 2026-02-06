package com.project.backend.controller.base;

import com.project.core.annotation.Log;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 基础CRUD控制器
 * 提供通用的增删改查方法
 * 
 * @param <VO> 视图对象类型
 * @param <QueryDTO> 查询DTO类型
 * @param <SaveDTO> 保存DTO类型（必须包含getId和setId方法）
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Slf4j
public abstract class BaseCrudController<VO, QueryDTO, SaveDTO> {

    /**
     * 获取实体名称（用于日志输出）
     * 
     * @return 实体名称，如"校区"或"院系"
     */
    public abstract String getEntityName();

    /**
     * 调用Service的分页查询方法
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    protected abstract PageResult<VO> callPageList(QueryDTO queryDTO);

    /**
     * 调用Service的根据ID查询详情方法
     * 
     * @param id 主键ID
     * @return 详情对象
     */
    protected abstract VO callGetDetailById(Long id);

    /**
     * 调用Service的保存方法（新增或编辑）
     * 
     * @param saveDTO 保存DTO
     * @return 是否成功
     */
    protected abstract boolean callSave(SaveDTO saveDTO);

    /**
     * 调用Service的删除方法
     * 
     * @param id 主键ID
     * @return 是否成功
     */
    protected abstract boolean callDelete(Long id);

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询列表")
    public R<PageResult<VO>> page(QueryDTO queryDTO) {
        log.info("分页查询{}列表，参数：{}", getEntityName(), queryDTO);
        PageResult<VO> result = callPageList(queryDTO);
        return R.ok(result);
    }

    /**
     * 根据ID查询详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询详情")
    @Parameter(name = "id", description = "主键ID", required = true)
    public R<VO> getDetail(@PathVariable Long id) {
        log.info("查询{}详情，ID：{}", getEntityName(), id);
        VO result = callGetDetailById(id);
        return R.ok(result);
    }

    /**
     * 新增
     */
    @PostMapping
    @Operation(summary = "新增")
    @Log(title = "新增", businessType = 1)
    public R<Void> add(@Valid @RequestBody SaveDTO saveDTO) {
        log.info("新增{}，参数：{}", getEntityName(), saveDTO);
        // 确保ID为空
        setIdIfExists(saveDTO, null);
        boolean success = callSave(saveDTO);
        if (success) {
            return R.ok(getEntityName() + "新增成功", null);
        } else {
            return R.fail(getEntityName() + "新增失败");
        }
    }

    /**
     * 编辑
     */
    @PutMapping("/{id}")
    @Operation(summary = "编辑")
    @Parameter(name = "id", description = "主键ID", required = true)
    @Log(title = "编辑", businessType = 2)
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SaveDTO saveDTO) {
        log.info("编辑{}，ID：{}，参数：{}", getEntityName(), id, saveDTO);
        setIdIfExists(saveDTO, id);
        boolean success = callSave(saveDTO);
        if (success) {
            return R.ok(getEntityName() + "编辑成功", null);
        } else {
            return R.fail(getEntityName() + "编辑失败");
        }
    }

    /**
     * 编辑（从请求体中读取ID）
     */
    @PutMapping
    @Operation(summary = "编辑（从请求体中读取ID）")
    @Log(title = "编辑", businessType = 2)
    public R<Void> updateFromBody(@Valid @RequestBody SaveDTO saveDTO) {
        Long id = getIdFromSaveDTO(saveDTO);
        if (id == null) {
            return R.fail("ID不能为空");
        }
        log.info("编辑{}，ID：{}，参数：{}", getEntityName(), id, saveDTO);
        setIdIfExists(saveDTO, id);
        boolean success = callSave(saveDTO);
        if (success) {
            return R.ok(getEntityName() + "编辑成功", null);
        } else {
            return R.fail(getEntityName() + "编辑失败");
        }
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    @Parameter(name = "id", description = "主键ID", required = true)
    @Log(title = "删除", businessType = 3)
    public R<Void> delete(@PathVariable Long id) {
        log.info("删除{}，ID：{}", getEntityName(), id);
        boolean success = callDelete(id);
        if (success) {
            return R.ok(getEntityName() + "删除成功", null);
        } else {
            return R.fail(getEntityName() + "删除失败");
        }
    }

    /**
     * 如果SaveDTO有setId方法，则设置ID
     * 使用反射来设置ID，避免强依赖具体的SaveDTO类型
     */
    private void setIdIfExists(SaveDTO saveDTO, Long id) {
        try {
            if (saveDTO != null) {
                // 使用反射调用setId方法
                java.lang.reflect.Method setIdMethod = saveDTO.getClass().getMethod("setId", Long.class);
                setIdMethod.invoke(saveDTO, id);
            }
        } catch (Exception e) {
            // 如果没有setId方法，忽略异常（某些DTO可能不需要ID）
            log.debug("SaveDTO {} 没有setId方法，跳过设置ID", saveDTO.getClass().getSimpleName());
        }
    }

    /**
     * 从SaveDTO中获取ID
     * 使用反射来获取ID，避免强依赖具体的SaveDTO类型
     */
    private Long getIdFromSaveDTO(SaveDTO saveDTO) {
        try {
            if (saveDTO != null) {
                java.lang.reflect.Method getIdMethod = saveDTO.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(saveDTO);
                if (id != null) {
                    if (id instanceof Number) {
                        return ((Number) id).longValue();
                    } else if (id instanceof String) {
                        return Long.parseLong((String) id);
                    }
                }
            }
        } catch (Exception e) {
            // 如果没有getId方法，返回null
            log.debug("SaveDTO {} 没有getId方法", saveDTO.getClass().getSimpleName());
        }
        return null;
    }
}
