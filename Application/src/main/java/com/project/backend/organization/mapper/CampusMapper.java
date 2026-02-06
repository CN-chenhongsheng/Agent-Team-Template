package com.project.backend.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.organization.entity.Campus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 校区Mapper
 *
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Mapper
public interface CampusMapper extends BaseMapper<Campus> {

    /**
     * 根据校区编码查询校区
     *
     * @param campusCode 校区编码
     * @return 校区信息
     */
    default Campus selectByCampusCode(String campusCode) {
        return selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Campus>()
                .eq(Campus::getCampusCode, campusCode)
        );
    }
}
