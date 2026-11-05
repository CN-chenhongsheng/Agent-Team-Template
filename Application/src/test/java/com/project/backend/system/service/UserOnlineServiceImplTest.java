package com.project.backend.system.service;

import com.project.backend.system.service.impl.UserOnlineServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户在线状态服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户在线状态服务单元测试")
class UserOnlineServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @InjectMocks
    private UserOnlineServiceImpl userOnlineService;

    @Test
    @DisplayName("用户上线写入在线集合并发布广播消息")
    void userOnlinePublishesStatus() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        userOnlineService.userOnline(1L);

        verify(zSetOperations).add(eq("sys:user:online"), eq("1"), anyDouble());
        verify(redisTemplate).convertAndSend("sys:user:online:channel", "1:true");
    }

    @Test
    @DisplayName("用户下线移除在线集合并发布广播消息")
    void userOfflinePublishesStatus() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        userOnlineService.userOffline(1L);

        verify(zSetOperations).remove("sys:user:online", "1");
        verify(redisTemplate).convertAndSend("sys:user:online:channel", "1:false");
    }

    @Test
    @DisplayName("非法广播消息不会抛出异常")
    void invalidStatusMessageIsIgnored() {
        userOnlineService.handleStatusMessage("not-a-number:true");
        userOnlineService.handleStatusMessage("1:garbage");
        userOnlineService.handleStatusMessage("1");
        userOnlineService.handleStatusMessage(null);
    }
}
