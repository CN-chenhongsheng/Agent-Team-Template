package com.project.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 树形结构工具测试
 */
@DisplayName("树形结构工具测试")
class TreeUtilsTest {

    @Test
    @DisplayName("正常父子关系可以构建树")
    void buildTree_buildsNormalTree() {
        Node root = new Node(1L, 0L);
        Node child = new Node(2L, 1L);
        Node grandChild = new Node(3L, 2L);

        List<Node> result = TreeUtils.buildTree(
                List.of(root, child, grandChild), 0L, Node::getParentId, Node::getId, Node::setChildren);

        assertThat(result).containsExactly(root);
        assertThat(root.getChildren()).containsExactly(child);
        assertThat(child.getChildren()).containsExactly(grandChild);
    }

    @Test
    @DisplayName("存在循环引用时不会无限递归")
    void buildTree_breaksCycles() {
        Node root = new Node(1L, 0L);
        Node child = new Node(2L, 1L);
        Node selfCycle = new Node(3L, 3L);
        Node cycleA = new Node(4L, 5L);
        Node cycleB = new Node(5L, 4L);

        List<Node> result = TreeUtils.buildTree(
                List.of(root, child, selfCycle, cycleA, cycleB),
                0L, Node::getParentId, Node::getId, Node::setChildren);

        assertThat(result).containsExactly(root);
        assertThat(root.getChildren()).containsExactly(child);
    }

    private static class Node {
        private final Long id;
        private Long parentId;
        private List<Node> children = new ArrayList<>();

        private Node(Long id, Long parentId) {
            this.id = id;
            this.parentId = parentId;
        }

        private Long getId() {
            return id;
        }

        private Long getParentId() {
            return parentId;
        }

        private void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        private List<Node> getChildren() {
            return children;
        }

        private void setChildren(List<Node> children) {
            this.children = children;
        }
    }
}
