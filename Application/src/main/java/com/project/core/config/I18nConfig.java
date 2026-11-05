package com.project.core.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core.i18n.I18nUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 国际化配置
 * 通过 Accept-Language 识别请求语言，英文请求使用 messages-en.json 翻译
 *
 * @author 陈鸿昇
 * @since 2026-11-05
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class I18nConfig {

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void loadTranslations() {
        ClassPathResource resource = new ClassPathResource("i18n/messages-en.json");
        if (!resource.exists()) {
            log.warn("未找到英文翻译文件：{}", resource.getPath());
            return;
        }
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, String> messages = objectMapper.readValue(
                    inputStream, new TypeReference<Map<String, String>>() {
                    });
            I18nUtils.setTranslations(messages);
            log.info("已加载 {} 条英文翻译", messages.size());
        } catch (Exception e) {
            log.error("加载英文翻译文件失败", e);
        }
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        resolver.setSupportedLocales(List.of(Locale.SIMPLIFIED_CHINESE, Locale.ENGLISH));
        return resolver;
    }
}
