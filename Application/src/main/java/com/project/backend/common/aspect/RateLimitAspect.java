package com.project.backend.common.aspect;

import com.project.core.annotation.RateLimit;
import com.project.core.exception.BusinessException;
import com.project.core.util.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 接口限流切面
 * 使用Redis + Lua脚本实现分布式限流
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Slf4j
@Aspect
@Component
@Order(1) // 在日志切面之前执行
public class RateLimitAspect {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Lua脚本：限流算法（令牌桶算法）
     */
    private static final String LIMIT_LUA_SCRIPT =
            "local key = KEYS[1]\n" +
            "local count = tonumber(ARGV[1])\n" +
            "local time = tonumber(ARGV[2])\n" +
            "local current = redis.call('get', key)\n" +
            "if current and tonumber(current) >= count then\n" +
            "    return 0\n" +
            "end\n" +
            "current = redis.call('incr', key)\n" +
            "if tonumber(current) == 1 then\n" +
            "    redis.call('expire', key, time)\n" +
            "end\n" +
            "return 1";

    @Pointcut("@annotation(com.project.core.annotation.RateLimit)")
    public void rateLimitPointCut() {
    }

    @Around("rateLimitPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        if (rateLimit != null) {
            String key = getRateLimitKey(rateLimit, point);
            int count = rateLimit.count();
            int time = rateLimit.time();

            // 执行Lua脚本
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(LIMIT_LUA_SCRIPT);
            script.setResultType(Long.class);

            Long result = redisTemplate.execute(script,
                    Collections.singletonList(key),
                    String.valueOf(count),
                    String.valueOf(time));

            if (result == null || result == 0) {
                log.warn("接口限流触发，key: {}, count: {}, time: {}", key, count, time);
                throw new BusinessException(rateLimit.message());
            }
        }

        return point.proceed();
    }

    /**
     * 获取限流key
     */
    private String getRateLimitKey(RateLimit rateLimit, ProceedingJoinPoint point) {
        String key = rateLimit.key();

        if (key.isEmpty()) {
            // 如果没有指定key，使用类名:方法:IP 作为key
            String className = point.getTarget().getClass().getName();
            String methodName = point.getSignature().getName();
            String ip = RequestUtils.getClientIp();
            key = String.format("rate:limit:%s:%s:%s", className, methodName, ip);
        } else {
            // 如果指定了key，添加前缀
            key = "rate:limit:" + key;
        }

        return key;
    }
}
