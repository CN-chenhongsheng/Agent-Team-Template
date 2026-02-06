package com.project.backend.student.service.impl;

import com.project.backend.common.imports.core.ImportContext;
import com.project.backend.util.DictUtils;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 学生导入上下文（级联 Map、已存在学号、默认密码等）
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@Builder
public class StudentImportContext implements ImportContext {

    /** 校区名称(trim) -> campusCode */
    private final Map<String, String> campusNameToCode;
    /** campusCode|deptName -> deptCode */
    private final Map<String, String> orgDeptKeyToCode;
    /** deptCode|majorName -> majorCode */
    private final Map<String, String> orgMajorKeyToCode;
    /** majorCode|className -> classId (String) */
    private final Map<String, Long> orgClassKeyToId;
    /** majorCode|className -> classCode */
    private final Map<String, String> orgClassKeyToCode;
    /** campusName_deptName -> deptCode (Excel 级联格式) */
    private final Map<String, String> orgDeptExcelKeyToCode;
    /** campusName_deptName_majorName -> majorCode (Excel 级联格式) */
    private final Map<String, String> orgMajorExcelKeyToCode;
    /** campusName_deptName_majorName_className -> classId (Excel 级联格式) */
    private final Map<String, Long> orgClassExcelKeyToId;
    /** campusName_deptName_majorName_className -> classCode (Excel 级联格式) */
    private final Map<String, String> orgClassExcelKeyToCode;
    /** campusCode|floorName -> floorId */
    private final Map<String, Long> dormFloorKeyToId;
    /** campusCode|floorName -> floorCode */
    private final Map<String, String> dormFloorKeyToCode;
    /** floorId|roomNumber -> roomId */
    private final Map<String, Long> dormRoomKeyToId;
    /** floorId|roomNumber -> roomCode */
    private final Map<String, String> dormRoomKeyToCode;
    /** roomId|bedNumber -> bedId */
    private final Map<String, Long> dormBedKeyToId;
    /** roomId|bedNumber -> bedCode */
    private final Map<String, String> dormBedKeyToCode;
    /** campusName_floorName -> floorId (Excel 级联格式) */
    private final Map<String, Long> dormFloorExcelKeyToId;
    /** campusName_floorName -> floorCode (Excel 级联格式) */
    private final Map<String, String> dormFloorExcelKeyToCode;
    /** campusName_floorName_roomNumber -> roomId (Excel 级联格式) */
    private final Map<String, Long> dormRoomExcelKeyToId;
    /** campusName_floorName_roomNumber -> roomCode (Excel 级联格式) */
    private final Map<String, String> dormRoomExcelKeyToCode;
    /** campusName_floorName_roomNumber_bedNumber -> bedId (Excel 级联格式) */
    private final Map<String, Long> dormBedExcelKeyToId;
    /** campusName_floorName_roomNumber_bedNumber -> bedCode (Excel 级联格式) */
    private final Map<String, String> dormBedExcelKeyToCode;
    /** 已存在的学号（DB + 本批已成功），线程安全 */
    private final Set<String> existingStudentNos;
    /** 默认密码（已加密） */
    private final String defaultPasswordEncrypted;
    /** 异步任务ID（用于更新进度，同步导入时为null） */
    private final String taskId;
    /** 预估总行数（用于计算进度，同步导入时为null） */
    private final Integer estimatedTotalRows;

    /**
     * 字典 label -> value 缓存：dictCode -> (label -> value)
     * 供多线程解析阶段复用，避免反复访问字典表
     */
    @Builder.Default
    private final Map<String, Map<String, String>> dictLabelCache = new ConcurrentHashMap<>();

    /**
     * 从缓存中获取字典值，如果不存在则调用 DictUtils 并写入缓存
     *
     * @param dictCode 字典编码
     * @param label    显示值
     * @return 字典 value（可能为 null）
     */
    public String getDictValueCached(String dictCode, String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        Map<String, String> labelMap = dictLabelCache.computeIfAbsent(dictCode, k -> new ConcurrentHashMap<>());
        return labelMap.computeIfAbsent(label, l -> DictUtils.getValueByLabel(dictCode, l));
    }
}
