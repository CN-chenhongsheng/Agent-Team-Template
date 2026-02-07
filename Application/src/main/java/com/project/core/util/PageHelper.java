package com.project.core.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.core.dto.BaseQueryDTO;
import com.project.core.result.PageResult;

import java.util.List;

/**
 * 分页辅助工具类
 * <p>
 * 封装 MyBatis-Plus Page 对象的创建和 PageResult 的构建，减少 Service 层模板代码
 * </p>
 *
 * @author 陈鸿昇
 * @since 2026-02-07
 */
public final class PageHelper {

    private PageHelper() {
        // 工具类禁止实例化
    }

    /**
     * 根据 QueryDTO 创建 MyBatis-Plus Page 对象
     *
     * @param queryDTO 分页查询 DTO（需继承 BaseQueryDTO）
     * @param <T>      实体类型
     * @return Page 对象
     */
    public static <T> Page<T> createPage(BaseQueryDTO queryDTO) {
        long current = (queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0) ? queryDTO.getPageNum() : 1;
        long size = (queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0) ? queryDTO.getPageSize() : 10;
        return new Page<>(current, size);
    }

    /**
     * 根据页码和每页条数创建 MyBatis-Plus Page 对象
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @param <T>      实体类型
     * @return Page 对象
     */
    public static <T> Page<T> createPage(Long pageNum, Long pageSize) {
        long current = (pageNum != null && pageNum > 0) ? pageNum : 1;
        long size = (pageSize != null && pageSize > 0) ? pageSize : 10;
        return new Page<>(current, size);
    }

    /**
     * 从 MyBatis-Plus Page 对象和转换后的 VO 列表构建 PageResult
     *
     * @param page MyBatis-Plus 分页对象
     * @param list 转换后的 VO 列表
     * @param <T>  VO 类型
     * @return PageResult
     */
    public static <T> PageResult<T> buildResult(Page<?> page, List<T> list) {
        return PageResult.build(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 从 MyBatis-Plus Page 对象直接构建 PageResult（不做 VO 转换）
     *
     * @param page MyBatis-Plus 分页对象
     * @param <T>  实体类型
     * @return PageResult
     */
    public static <T> PageResult<T> buildResult(Page<T> page) {
        return PageResult.build(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}
