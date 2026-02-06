package com.project.backend.controller.base;

import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 树形列表控制器接口
 * 提供树形结构数据的查询方法
 * 
 * @param <VO> 视图对象类型（树节点类型）
 * @param <QueryDTO> 查询DTO类型
 * @author 陈鸿昇
 * @since 2025-01-01
 */
public interface TreeController<VO, QueryDTO> {

    /**
     * 获取实体名称（用于日志输出）
     * 
     * @return 实体名称，如"校区"或"院系"
     */
    String getEntityName();

    /**
     * 调用Service的树形列表查询方法
     * 
     * @param queryDTO 查询条件
     * @return 树形列表
     */
    List<VO> callTreeList(QueryDTO queryDTO);

    /**
     * 查询树形列表
     */
    @GetMapping("/tree")
    @Operation(summary = "查询树形列表")
    default R<List<VO>> tree(QueryDTO queryDTO) {
        List<VO> result = callTreeList(queryDTO);
        return R.ok(result);
    }
}
