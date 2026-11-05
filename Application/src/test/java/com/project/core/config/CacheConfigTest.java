package com.project.core.config;

import com.project.backend.system.vo.DictDataVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis 缓存序列化测试
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Redis 缓存序列化测试")
class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("字典缓存可正常序列化与反序列化")
    void dictCacheRoundTrip() {
        Cache cache = cacheManager.getCache("dict:data");
        assertThat(cache).isNotNull();

        DictDataVO item = new DictDataVO();
        item.setId(1L);
        item.setDictCode("sys_common_status");
        item.setLabel("正常");
        item.setValue("1");
        item.setCreateTime(LocalDateTime.of(2026, 11, 5, 12, 0, 0));

        List<DictDataVO> data = new ArrayList<>();
        data.add(item);
        cache.put("round-trip", data);

        Cache.ValueWrapper wrapper = cache.get("round-trip");
        assertThat(wrapper).isNotNull();
        Object cached = wrapper.get();
        assertThat(cached).isInstanceOf(List.class);

        List<?> cachedList = (List<?>) cached;
        assertThat(cachedList).hasSize(1);
        assertThat(cachedList.get(0)).isInstanceOf(DictDataVO.class);

        DictDataVO restored = (DictDataVO) cachedList.get(0);
        assertThat(restored.getDictCode()).isEqualTo("sys_common_status");
        assertThat(restored.getLabel()).isEqualTo("正常");
        assertThat(restored.getCreateTime()).isEqualTo(item.getCreateTime());

        cache.evict("round-trip");
    }
}
