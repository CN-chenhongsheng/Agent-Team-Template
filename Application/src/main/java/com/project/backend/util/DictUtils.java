package com.project.backend.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.backend.system.entity.DictData;
import com.project.backend.system.mapper.DictDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 字典工具
 * 提供字典值到标签的转换功能，支持本地缓存
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Slf4j
@Component
public class DictUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static DictDataMapper dictDataMapper;

    /**
     * 字典缓存
     * Key: dictCode, Value: Map<value, label>
     */
    private static final Map<String, Map<String, String>> DICT_CACHE = new ConcurrentHashMap<>();

    /**
     * 常用字典列表（启动时预热）
     */
    private static final String[] COMMON_DICT_CODES = {
            "sys_user_sex",
            "sys_user_status",
            "sys_common_status",
            "sys_oper_business_type",
            "sys_device_type",
            "academic_status",
            // 审批相关字典
            "check_in_type",
            "approval_business_type",
            "approval_action",
            "approval_instance_status",
            "approval_assignee_type"
    };

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 初始化：获取 Mapper Bean 并预热常用字典
     */
    @PostConstruct
    public void init() {
        dictDataMapper = applicationContext.getBean(DictDataMapper.class);
        // 预热常用字典
        for (String dictCode : COMMON_DICT_CODES) {
            try {
                loadDictToCache(dictCode);
                log.info("预热字典缓存成功: {}", dictCode);
            } catch (Exception e) {
                log.warn("预热字典缓存失败: {}, 错误: {}", dictCode, e.getMessage());
            }
        }
    }

    /**
     * 根据字典编码和值获取标签
     * 
     * @param dictCode 字典编码
     * @param value 字典值
     * @return 字典标签，未找到返回 null
     */
    public static String getLabel(String dictCode, Object value) {
        return getLabel(dictCode, value, null);
    }

    /**
     * 根据字典编码和值获取标签（带默认值）
     *
     * @param dictCode 字典编码
     * @param value 字典值
     * @param defaultLabel 默认标签（未找到时返回）
     * @return 字典标签
     */
    public static String getLabel(String dictCode, Object value, String defaultLabel) {
        if (dictCode == null || value == null) {
            return defaultLabel;
        }

        Map<String, String> dictMap = getOrLoadDictMap(dictCode);
        return dictMap.getOrDefault(String.valueOf(value), defaultLabel);
    }

    /**
     * 获取某字典编码的所有数据
     *
     * @param dictCode 字典编码
     * @return 字典数据列表（value -> label 映射）
     */
    public static Map<String, String> getDictMap(String dictCode) {
        return dictCode != null ? getOrLoadDictMap(dictCode) : Map.of();
    }

    /**
     * 根据字典编码和标签获取值（用于 Excel 导入：label -> value）
     *
     * @param dictCode 字典编码
     * @param label    字典标签（如「男」「在读」）
     * @return 字典值，未找到返回 null
     */
    public static String getValueByLabel(String dictCode, String label) {
        return getValueByLabel(dictCode, label, null);
    }

    /**
     * 根据字典编码和标签获取值（带默认值）
     *
     * @param dictCode    字典编码
     * @param label       字典标签
     * @param defaultValue 未找到时返回的默认值
     * @return 字典值
     */
    public static String getValueByLabel(String dictCode, String label, String defaultValue) {
        if (dictCode == null || label == null || label.isBlank()) {
            return defaultValue;
        }
        Map<String, String> valueToLabel = getOrLoadDictMap(dictCode);
        for (Map.Entry<String, String> e : valueToLabel.entrySet()) {
            if (label.trim().equals(e.getValue())) {
                return e.getKey();
            }
        }
        return defaultValue;
    }

    /**
     * 获取某字典编码的所有 value 列表
     *
     * @param dictCode 字典编码
     * @return 所有字典值列表
     */
    public static List<String> getValues(String dictCode) {
        if (dictCode == null) {
            return List.of();
        }
        Map<String, String> dictMap = getOrLoadDictMap(dictCode);
        return List.copyOf(dictMap.keySet());
    }

    /**
     * 从缓存获取字典，如果不存在则加载
     *
     * @param dictCode 字典编码
     * @return 字典映射
     */
    private static Map<String, String> getOrLoadDictMap(String dictCode) {
        return DICT_CACHE.computeIfAbsent(dictCode, DictUtils::loadDictToCache);
    }

    /**
     * 从数据库加载字典到缓存
     *
     * @param dictCode 字典编码
     * @return 字典映射
     */
    private static Map<String, String> loadDictToCache(String dictCode) {
        if (dictDataMapper == null) {
            log.warn("DictDataMapper 未初始化，无法加载字典: {}", dictCode);
            return Map.of();
        }

        try {
            List<DictData> dataList = dictDataMapper.selectList(
                    new LambdaQueryWrapper<DictData>()
                            .eq(DictData::getDictCode, dictCode)
                            .eq(DictData::getStatus, 1)
                            .orderByAsc(DictData::getSort)
                            .orderByAsc(DictData::getId)
            );

            Map<String, String> dictMap = dataList.stream()
                    .collect(Collectors.toMap(
                            DictData::getValue,
                            DictData::getLabel,
                            (v1, v2) -> v1
                    ));

            log.debug("加载字典到缓存: {}, 数据: {}", dictCode, dictMap.size());
            return dictMap;
        } catch (Exception e) {
            log.error("加载字典失败: {}", dictCode, e);
            return Map.of();
        }
    }

    /**
     * 刷新指定字典缓存
     * 
     * @param dictCode 字典编码
     */
    public static void refreshCache(String dictCode) {
        if (dictCode == null) {
            return;
        }

        DICT_CACHE.remove(dictCode);
        loadDictToCache(dictCode);
        log.info("刷新字典缓存: {}", dictCode);
    }

    /**
     * 清空所有字典缓存
     */
    public static void clearCache() {
        DICT_CACHE.clear();
        log.info("清空所有字典缓存");
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存的字典编码列表
     */
    public static List<String> getCachedDictCodes() {
        return DICT_CACHE.keySet().stream().sorted().collect(Collectors.toList());
    }
}
