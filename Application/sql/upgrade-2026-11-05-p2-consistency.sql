/*
 升级脚本：统一状态字典与字段注释
 背景：角色/菜单/字典/字典数据统一改用 sys_common_status；
       登录状态、操作状态、菜单类型、显示隐藏改为字典驱动；
       修正 SQL 注释与实体枚举值不一致的问题
 执行：在已有数据库上执行本脚本（幂等，可重复执行）
 日期：2026-11-05
*/

-- 1. 新增字典类型（不存在时插入）
INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `status`, `remark`, `create_time`, `deleted`)
SELECT '登录类型', 'sys_login_type', 1, '登录类型字典', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_code` = 'sys_login_type' AND `deleted` = 0);

INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `status`, `remark`, `create_time`, `deleted`)
SELECT '登录状态', 'sys_login_status', 1, '登录状态：0-失败 1-成功 2-登出', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_code` = 'sys_login_status' AND `deleted` = 0);

INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `status`, `remark`, `create_time`, `deleted`)
SELECT '操作状态', 'sys_oper_status', 1, '操作日志状态：0-正常 1-异常', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_code` = 'sys_oper_status' AND `deleted` = 0);

INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `status`, `remark`, `create_time`, `deleted`)
SELECT '菜单类型', 'sys_menu_type', 1, '菜单类型：M-目录 C-菜单 F-按钮', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_code` = 'sys_menu_type' AND `deleted` = 0);

INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `status`, `remark`, `create_time`, `deleted`)
SELECT '显示隐藏', 'sys_show_hide', 1, '通用显示/隐藏状态：1-显示 0-隐藏', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_code` = 'sys_show_hide' AND `deleted` = 0);

-- 2. 新增字典数据（不存在时插入）
INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `css_class`, `list_class`, `sort`, `is_default`, `status`, `remark`, `create_time`, `deleted`)
SELECT 'sys_login_type', '密码登录', 'password', '', 'primary', 1, 0, 1, '密码登录', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_code` = 'sys_login_type' AND `value` = 'password' AND `deleted` = 0);

INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `css_class`, `list_class`, `sort`, `is_default`, `status`, `remark`, `create_time`, `deleted`)
SELECT 'sys_login_status', '失败', '0', '', 'danger', 1, 0, 1, '登录失败', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_code` = 'sys_login_status' AND `value` = '0' AND `deleted` = 0);

INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `css_class`, `list_class`, `sort`, `is_default`, `status`, `remark`, `create_time`, `deleted`)
SELECT 'sys_login_status', '成功', '1', '', 'success', 2, 0, 1, '登录成功', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_code` = 'sys_login_status' AND `value` = '1' AND `deleted` = 0);

INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `css_class`, `list_class`, `sort`, `is_default`, `status`, `remark`, `create_time`, `deleted`)
SELECT 'sys_login_status', '登出', '2', '', 'info', 3, 0, 1, '主动登出', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_code` = 'sys_login_status' AND `value` = '2' AND `deleted` = 0);

INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `css_class`, `list_class`, `sort`, `is_default`, `status`, `remark`, `create_time`, `deleted`)
SELECT 'sys_oper_status', '正常', '0', '', 'success', 1, 0, 1, '操作正常', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_code` = 'sys_oper_status' AND `value` = '0' AND `deleted` = 0);

INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `css_class`, `list_class`, `sort`, `is_default`, `status`, `remark`, `create_time`, `deleted`)
SELECT 'sys_oper_status', '异常', '1', '', 'danger', 2, 0, 1, '操作异常', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_code` = 'sys_oper_status' AND `value` = '1' AND `deleted` = 0);

INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `css_class`, `list_class`, `sort`, `is_default`, `status`, `remark`, `create_time`, `deleted`)
SELECT 'sys_menu_type', '目录', 'M', '', 'info', 1, 0, 1, '目录类型菜单', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_code` = 'sys_menu_type' AND `value` = 'M' AND `deleted` = 0);

INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `css_class`, `list_class`, `sort`, `is_default`, `status`, `remark`, `create_time`, `deleted`)
SELECT 'sys_menu_type', '菜单', 'C', '', 'primary', 2, 0, 1, '菜单类型菜单', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_code` = 'sys_menu_type' AND `value` = 'C' AND `deleted` = 0);

INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `css_class`, `list_class`, `sort`, `is_default`, `status`, `remark`, `create_time`, `deleted`)
SELECT 'sys_menu_type', '按钮', 'F', '', 'warning', 3, 0, 1, '按钮类型权限', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_code` = 'sys_menu_type' AND `value` = 'F' AND `deleted` = 0);

INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `css_class`, `list_class`, `sort`, `is_default`, `status`, `remark`, `create_time`, `deleted`)
SELECT 'sys_show_hide', '显示', '1', '', 'success', 1, 0, 1, '显示', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_code` = 'sys_show_hide' AND `value` = '1' AND `deleted` = 0);

INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `css_class`, `list_class`, `sort`, `is_default`, `status`, `remark`, `create_time`, `deleted`)
SELECT 'sys_show_hide', '隐藏', '0', '', 'info', 2, 0, 1, '隐藏', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_code` = 'sys_show_hide' AND `value` = '0' AND `deleted` = 0);

-- 3. 修正字段注释，使其与后端实际取值一致
ALTER TABLE `sys_menu`
    MODIFY COLUMN `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单类型：M-目录 C-菜单 F-按钮',
    MODIFY COLUMN `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-停用 1-正常';

ALTER TABLE `sys_oper_log`
    MODIFY COLUMN `business_type` int NULL DEFAULT 0 COMMENT '业务类型：0-其它 1-新增 2-修改 3-删除',
    MODIFY COLUMN `device_type` int NULL DEFAULT NULL COMMENT '设备类型：1-桌面设备 2-移动设备 3-爬虫/Bot';
