package com.project.backend.common.service;

import com.project.backend.approval.service.ApprovalService;
import com.project.backend.approval.vo.ApprovalAssigneeVO;
import com.project.backend.approval.vo.ApprovalInstanceVO;
import com.project.backend.approval.vo.ApprovalNodeVO;
import com.project.backend.approval.vo.ApprovalRecordVO;
import com.project.core.vo.ApprovalProgress;
import com.project.core.vo.ApprovalProgressNode;
import com.project.backend.util.DictUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审批进度构建器
 * 统一构建审批进度信息，供各业务模块复用
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalProgressBuilder {

    private final ApprovalService approvalService;

    /**
     * 构建审批进度信息
     *
     * @param approvalInstanceId 审批实例ID
     * @param status             业务状态（如 check_in_status、check_out_status 等）
     * @param statusDictCode     状态字典编码（用于获取状态文本）
     * @return 审批进度信息
     */
    public ApprovalProgress buildProgress(Long approvalInstanceId, Integer status, String statusDictCode) {
        ApprovalProgress progress = new ApprovalProgress();
        progress.setStatus(status);
        progress.setStatusText(DictUtils.getLabel(statusDictCode, status, "未知"));

        ApprovalInstanceVO instance = null;
        try {
            instance = approvalService.getInstanceDetail(approvalInstanceId);
        } catch (Exception e) {
            log.error("获取审批实例详情失败: {}", e.getMessage());
        }

        if (instance != null) {
            progress.setApplicantName(instance.getApplicantName());
            progress.setStartTime(instance.getStartTime());
            progress.setCurrentNodeName(instance.getCurrentNodeName());

            List<ApprovalProgressNode> nodeTimeline = buildNodeTimeline(instance);
            progress.setNodeTimeline(nodeTimeline);
            progress.setTotalNodes(nodeTimeline.size());
            progress.setCompletedNodes((int) nodeTimeline.stream()
                    .filter(node -> node.getStatus() != null && node.getStatus() == 2)
                    .count());

            if (status != null && status != 1) {
                progress.setProgressPercent(100);
            } else if (nodeTimeline.isEmpty()) {
                progress.setProgressPercent(0);
            } else {
                int percent = (int) Math.round(progress.getCompletedNodes() * 100.0 / nodeTimeline.size());
                progress.setProgressPercent(Math.min(100, Math.max(0, percent)));
            }

            if (status != null && status == 1) {
                String nextApproverName = getNextApproverName(instance);
                progress.setNextApproverName(nextApproverName);

                String nodeName = instance.getCurrentNodeName() != null ? instance.getCurrentNodeName() : "待审批";
                String approverName = nextApproverName != null ? nextApproverName : "未指定";
                progress.setProgressText(String.format("%s(%s)", nodeName, approverName));
            } else {
                progress.setProgressText(DictUtils.getLabel(statusDictCode, status, "未知进度"));
            }
        } else {
            if (status != null && status != 1) {
                progress.setProgressPercent(100);
            } else {
                progress.setProgressPercent(0);
            }
            progress.setProgressText("未知进度");
        }

        return progress;
    }

    /**
     * 构建节点时间线
     *
     * @param instance 审批实例
     * @return 节点时间线列表
     */
    public List<ApprovalProgressNode> buildNodeTimeline(ApprovalInstanceVO instance) {
        if (instance.getNodes() == null || instance.getNodes().isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, ApprovalRecordVO> recordMap = buildRecordMap(instance.getRecords());

        return instance.getNodes().stream()
                .sorted(Comparator.comparing(
                        ApprovalNodeVO::getNodeOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .map(node -> {
                    ApprovalProgressNode timelineNode = new ApprovalProgressNode();
                    timelineNode.setNodeId(node.getId());
                    timelineNode.setNodeName(node.getNodeName());
                    timelineNode.setAssigneeNames(getNodeAssigneeNames(node));

                    ApprovalRecordVO record = recordMap.get(node.getId());
                    if (record != null) {
                        if (record.getAction() != null && record.getAction() == 2) {
                            timelineNode.setStatus(3);
                            timelineNode.setStatusText(DictUtils.getLabel("approval_action", 2, "已拒绝"));
                        } else {
                            timelineNode.setStatus(2);
                            timelineNode.setStatusText(DictUtils.getLabel("approval_action", 1, "已通过"));
                        }
                        timelineNode.setActionText(record.getActionText());
                        timelineNode.setApproveTime(record.getApproveTime());
                    } else {
                        timelineNode.setStatus(1);
                        timelineNode.setStatusText("待处理");
                    }

                    return timelineNode;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取下一审批人姓名
     *
     * @param instance 审批实例
     * @return 下一审批人姓名（多个用顿号分隔）
     */
    private String getNextApproverName(ApprovalInstanceVO instance) {
        if (instance.getNodes() == null || instance.getNodes().isEmpty()) {
            return null;
        }

        // 查找当前节点
        ApprovalNodeVO currentNode = instance.getNodes().stream()
                .filter(node -> node.getId().equals(instance.getCurrentNodeId()))
                .findFirst()
                .orElse(null);

        if (currentNode != null && currentNode.getAssignees() != null && !currentNode.getAssignees().isEmpty()) {
            // 返回第一个审批人姓名（如果是多人会签，可以展示多个）
            List<String> approverNames = currentNode.getAssignees().stream()
                    .map(ApprovalAssigneeVO::getAssigneeName)
                    .collect(Collectors.toList());
            return String.join("、", approverNames);
        }

        return null;
    }

    /**
     * 构建审批记录 Map（按节点ID索引）
     *
     * @param records 审批记录列表
     * @return 节点ID -> 审批记录的 Map
     */
    private Map<Long, ApprovalRecordVO> buildRecordMap(List<ApprovalRecordVO> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }

        return records.stream()
                .filter(record -> record.getNodeId() != null)
                .collect(Collectors.toMap(
                        ApprovalRecordVO::getNodeId,
                        record -> record,
                        (first, second) -> {
                            // 如果有多个记录，保留最新的（按审批时间）
                            if (first.getApproveTime() != null && second.getApproveTime() != null) {
                                return first.getApproveTime().isAfter(second.getApproveTime()) ? first : second;
                            }
                            return first.getApproveTime() != null ? first : second;
                        }
                ));
    }

    /**
     * 获取节点审批人姓名（多个用顿号分隔）
     *
     * @param node 审批节点
     * @return 审批人姓名（多个用顿号分隔）
     */
    private String getNodeAssigneeNames(ApprovalNodeVO node) {
        if (node.getAssignees() == null || node.getAssignees().isEmpty()) {
            return null;
        }
        return node.getAssignees().stream()
                .map(ApprovalAssigneeVO::getAssigneeName)
                .collect(Collectors.joining("、"));
    }
}
