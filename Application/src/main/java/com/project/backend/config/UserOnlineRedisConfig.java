package com.project.backend.config;

import com.project.backend.system.service.impl.UserOnlineServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;

/**
 * 用户在线状态广播配置
 * 通过 Redis 发布订阅让所有实例都能收到状态变更
 *
 * @author 陈鸿昇
 * @since 2026-11-05
 */
@Configuration
@RequiredArgsConstructor
public class UserOnlineRedisConfig {

    private final RedisConnectionFactory redisConnectionFactory;
    private final UserOnlineServiceImpl userOnlineService;

    @Bean
    public RedisMessageListenerContainer userOnlineMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(
                (message, pattern) -> userOnlineService.handleStatusMessage(
                        new String(message.getBody(), StandardCharsets.UTF_8)),
                new ChannelTopic(UserOnlineServiceImpl.STATUS_CHANNEL));
        return container;
    }
}
