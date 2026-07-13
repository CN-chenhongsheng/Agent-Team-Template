package com.project.backend.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.backend.system.entity.DictType;
import com.project.backend.system.mapper.DictDataMapper;
import com.project.backend.system.mapper.DictTypeMapper;
import com.project.backend.system.service.impl.DictTypeServiceImpl;
import com.project.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * 字典类型服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("字典类型服务单元测试")
class DictTypeServiceImplTest {

    @Mock
    private DictDataMapper dictDataMapper;

    @Mock
    private DictTypeMapper dictTypeMapper;

    @Spy
    @InjectMocks
    private DictTypeServiceImpl dictTypeService;

    @Test
    @DisplayName("存在字典数据时不允许删除字典类型")
    void deleteDictTypeWithChildrenShouldFail() {
        DictType dictType = new DictType();
        dictType.setId(1L);
        dictType.setDictCode("sys_user_status");

        doReturn(dictType).when(dictTypeService).getById(1L);
        when(dictDataMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        assertThatThrownBy(() -> dictTypeService.deleteDictType(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("存在字典数据");
    }
}
