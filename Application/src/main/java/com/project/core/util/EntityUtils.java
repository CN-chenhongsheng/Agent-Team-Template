package com.project.core.util;

import com.project.core.exception.BusinessException;

import java.util.function.Supplier;

/**
 * 实体工具类
 * 提供实体存在性校验等通用方法
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
public class EntityUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private EntityUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 要求实体不为 null，否则抛出业务异常
     *
     * @param entity     实体对象
     * @param entityName 实体名称（用于错误消息，如"任务"、"学生"等）
     * @param <T>        实体类型
     * @return 非 null 的实体对象
     * @throws BusinessException 如果实体为 null
     */
    public static <T> T requireNonNull(T entity, String entityName) {
        if (entity == null) {
            throw new BusinessException(entityName + "不存在");
        }
        return entity;
    }

    /**
     * 通过 ID 获取实体，要求实体不为 null，否则抛出业务异常
     * 适用于需要先调用 getter 方法获取实体，然后校验的场景
     *
     * @param getter     获取实体的 Supplier（如 () -> getById(id)）
     * @param entityName 实体名称（用于错误消息，如"任务"、"学生"等）
     * @param <T>        实体类型
     * @return 非 null 的实体对象
     * @throws BusinessException 如果实体为 null
     */
    public static <T> T requireById(Supplier<T> getter, String entityName) {
        T entity = getter.get();
        return requireNonNull(entity, entityName);
    }
}
