package com.project.core.i18n;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

/**
 * 后端消息翻译工具
 * <p>
 * 以中文源消息作为翻译键，英文请求时查表返回英文，找不到翻译时回退到源消息。
 * 仅处理 HTTP 请求线程内的消息，避免影响单元测试和异步日志。
 * </p>
 *
 * @author 陈鸿昇
 * @since 2026-11-05
 */
public final class I18nUtils {

    private static volatile Map<String, String> translations = Map.of();

    private I18nUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 设置英文翻译表
     *
     * @param messages 中文源消息 -> 英文翻译
     */
    public static void setTranslations(Map<String, String> messages) {
        translations = messages == null ? Map.of() : Map.copyOf(messages);
    }

    /**
     * 翻译完整消息
     *
     * @param message 中文源消息
     * @return 英文请求返回英文，否则返回原文
     */
    public static String translate(String message) {
        if (message == null || !isEnglishRequest()) {
            return message;
        }
        return translations.getOrDefault(message, message);
    }

    /**
     * 翻译带参数的消息模板并格式化
     *
     * @param template 中文模板，参数使用 {0}、{1}
     * @param args     参数
     * @return 格式化后的消息
     */
    public static String format(String template, Object... args) {
        if (template == null) {
            return null;
        }
        String pattern = isEnglishRequest()
                ? translations.getOrDefault(template, template)
                : template;
        return MessageFormat.format(pattern, args);
    }

    private static boolean isEnglishRequest() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return false;
        }
        Locale locale = LocaleContextHolder.getLocale();
        return locale != null && "en".equalsIgnoreCase(locale.getLanguage());
    }
}
