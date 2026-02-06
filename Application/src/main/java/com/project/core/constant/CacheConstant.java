package com.project.core.constant;

import java.util.concurrent.TimeUnit;

/**
 * 缓存常量
 *
 * @author 陈鸿昇
 * @since 2025-01-05
 */
public class CacheConstant {

    /**
     * 缓存命名空间
     */
    public static final String CACHE_NAMESPACE = "project:";

    /**
     * 登录用户缓存
     * 缓存时间30分钟
     */
    public static final String LOGIN_USER_KEY = CACHE_NAMESPACE + "login_user:";
    public static final long LOGIN_USER_TTL = 30;
    public static final TimeUnit LOGIN_USER_TIME_UNIT = TimeUnit.MINUTES;

    /**
     * 用户信息缓存
     * 缓存时间30分钟
     */
    public static final String USER_INFO_KEY = CACHE_NAMESPACE + "user_info:";
    public static final long USER_INFO_TTL = 30;
    public static final TimeUnit USER_INFO_TIME_UNIT = TimeUnit.MINUTES;

    /**
     * 角色权限缓存
     * 缓存时间1小时
     */
    public static final String ROLE_PERMISSION_KEY = CACHE_NAMESPACE + "role_permission:";
    public static final long ROLE_PERMISSION_TTL = 1;
    public static final TimeUnit ROLE_PERMISSION_TIME_UNIT = TimeUnit.HOURS;

    /**
     * 用户菜单缓存
     * 缓存时间30分钟
     */
    public static final String USER_MENU_KEY = CACHE_NAMESPACE + "user_menu:";
    public static final long USER_MENU_TTL = 30;
    public static final TimeUnit USER_MENU_TIME_UNIT = TimeUnit.MINUTES;

    /**
     * 字典数据缓存
     * 缓存时间：永久（直到数据变更主动清除）
     */
    public static final String DICT_DATA_KEY = CACHE_NAMESPACE + "dict_data:";
    public static final long DICT_DATA_TTL = Long.MAX_VALUE;

    /**
     * 校区信息缓存
     * 缓存时间1小时
     */
    public static final String CAMPUS_KEY = CACHE_NAMESPACE + "campus:";
    public static final long CAMPUS_TTL = 1;
    public static final TimeUnit CAMPUS_TIME_UNIT = TimeUnit.HOURS;

    /**
     * 院系信息缓存
     * 缓存时间1小时
     */
    public static final String DEPARTMENT_KEY = CACHE_NAMESPACE + "department:";
    public static final long DEPARTMENT_TTL = 1;
    public static final TimeUnit DEPARTMENT_TIME_UNIT = TimeUnit.HOURS;

    /**
     * 床位状态缓存
     * 缓存时间10分钟
     */
    public static final String BED_STATUS_KEY = CACHE_NAMESPACE + "bed_status:";
    public static final long BED_STATUS_TTL = 10;
    public static final TimeUnit BED_STATUS_TIME_UNIT = TimeUnit.MINUTES;

    private CacheConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
