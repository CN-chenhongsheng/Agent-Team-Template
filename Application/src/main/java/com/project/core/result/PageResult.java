package com.project.core.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应结果
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Long pageNum;

    @Schema(description = "每页条数")
    private Long pageSize;

    @Schema(description = "总页数")
    private Long totalPages;

    /**
     * 构建分页结果（带空值保护）
     */
    public static <T> PageResult<T> build(List<T> list, Long total, Long pageNum, Long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list != null ? list : Collections.emptyList());
        result.setTotal(total != null ? total : 0L);
        result.setPageNum(pageNum != null ? pageNum : 1L);
        result.setPageSize(pageSize != null && pageSize > 0 ? pageSize : 10L);
        result.setTotalPages(result.getTotal() == 0 ? 0L
                : (result.getTotal() + result.getPageSize() - 1) / result.getPageSize());
        return result;
    }

    /**
     * 从 MyBatis-Plus Page 对象构建分页结果
     *
     * @param page MyBatis-Plus 分页对象
     * @param list 转换后的 VO 列表
     */
    public static <T> PageResult<T> fromPage(Page<?> page, List<T> list) {
        return build(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 从 MyBatis-Plus Page 对象构建分页结果（直接使用 Page 中的 records）
     *
     * @param page MyBatis-Plus 分页对象
     */
    @SuppressWarnings("unchecked")
    public static <T> PageResult<T> fromPage(Page<T> page) {
        return build(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 返回空分页结果
     */
    public static <T> PageResult<T> empty() {
        return build(Collections.emptyList(), 0L, 1L, 10L);
    }

    /**
     * 返回指定分页参数的空分页结果
     */
    public static <T> PageResult<T> empty(Long pageNum, Long pageSize) {
        return build(Collections.emptyList(), 0L, pageNum, pageSize);
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return pageNum != null && totalPages != null && pageNum < totalPages;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return pageNum != null && pageNum > 1;
    }
}
