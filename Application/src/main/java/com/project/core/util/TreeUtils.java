package com.project.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 树形结构工具类
 * 提供通用的树形结构构建方法
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
public class TreeUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private TreeUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 构建树形结构（基于 parentId）
     * 适用于扁平列表 + parentId 的树形结构场景
     *
     * @param list            扁平列表
     * @param rootParentId    根节点的 parentId（通常为 0L 或 null）
     * @param parentIdGetter  获取 parentId 的函数
     * @param idGetter        获取 id 的函数
     * @param childrenSetter  设置 children 的函数
     * @param <T>             节点类型
     * @return 树形结构列表（根节点列表）
     */
    public static <T> List<T> buildTree(
            List<T> list,
            Long rootParentId,
            Function<T, Long> parentIdGetter,
            Function<T, Long> idGetter,
            BiConsumer<T, List<T>> childrenSetter) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> roots = new ArrayList<>();
        for (T node : list) {
            Long parentId = parentIdGetter.apply(node);
            if ((rootParentId == null && parentId == null) ||
                (rootParentId != null && rootParentId.equals(parentId))) {
                // 找到根节点，递归构建子树
                buildChildren(node, list, parentIdGetter, idGetter, childrenSetter);
                roots.add(node);
            }
        }
        return roots;
    }

    /**
     * 递归构建子节点
     *
     * @param parent          父节点
     * @param allNodes        所有节点列表
     * @param parentIdGetter  获取 parentId 的函数
     * @param idGetter        获取 id 的函数
     * @param childrenSetter  设置 children 的函数
     * @param <T>             节点类型
     */
    private static <T> void buildChildren(
            T parent,
            List<T> allNodes,
            Function<T, Long> parentIdGetter,
            Function<T, Long> idGetter,
            BiConsumer<T, List<T>> childrenSetter) {
        Long parentId = idGetter.apply(parent);
        List<T> children = new ArrayList<>();

        for (T node : allNodes) {
            Long nodeParentId = parentIdGetter.apply(node);
            if ((parentId == null && nodeParentId == null) ||
                (parentId != null && parentId.equals(nodeParentId))) {
                // 找到子节点，递归构建其子树
                buildChildren(node, allNodes, parentIdGetter, idGetter, childrenSetter);
                children.add(node);
            }
        }

        if (!children.isEmpty()) {
            childrenSetter.accept(parent, children);
        }
    }
}
