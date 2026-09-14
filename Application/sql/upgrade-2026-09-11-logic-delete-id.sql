/*
 升级脚本：逻辑删除值由固定 1 改为记录主键ID
 背景：sys_user.uk_username、sys_role.uk_role_code 等唯一索引包含 deleted 列，
       逻辑删除统一写 deleted=1，同一用户名/编码「删除→重建→再删除」时触发唯一键冲突
       （Duplicate entry 'xxx-1' for key 'uk_xxx'）
 方案：逻辑删除时写入 deleted=id，每条已删除记录的值全局唯一，不再冲突
 执行：在需要升级的数据库上执行本脚本（幂等，可重复执行）
 日期：2026-09-11
*/

-- 1. deleted 列类型 tinyint -> bigint（需要存储主键ID）
ALTER TABLE `sys_user`      MODIFY COLUMN `deleted` bigint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 非0-已删除（值为记录主键ID）';
ALTER TABLE `sys_role`      MODIFY COLUMN `deleted` bigint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 非0-已删除（值为记录主键ID）';
ALTER TABLE `sys_menu`      MODIFY COLUMN `deleted` bigint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 非0-已删除（值为记录主键ID）';
ALTER TABLE `sys_dict_data` MODIFY COLUMN `deleted` bigint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 非0-已删除（值为记录主键ID）';
ALTER TABLE `sys_dict_type` MODIFY COLUMN `deleted` bigint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 非0-已删除（值为记录主键ID）';
ALTER TABLE `sys_user_role` MODIFY COLUMN `deleted` bigint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 非0-已删除（值为记录主键ID）';
ALTER TABLE `sys_user_menu` MODIFY COLUMN `deleted` bigint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 非0-已删除（值为记录主键ID）';
ALTER TABLE `sys_role_menu` MODIFY COLUMN `deleted` bigint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 非0-已删除（值为记录主键ID）';

-- 2. 历史已删除记录规范化：deleted = 主键ID
UPDATE `sys_user`      SET `deleted` = `id` WHERE `deleted` = 1;
UPDATE `sys_role`      SET `deleted` = `id` WHERE `deleted` = 1;
UPDATE `sys_menu`      SET `deleted` = `id` WHERE `deleted` = 1;
UPDATE `sys_dict_data` SET `deleted` = `id` WHERE `deleted` = 1;
UPDATE `sys_dict_type` SET `deleted` = `id` WHERE `deleted` = 1;
UPDATE `sys_user_role` SET `deleted` = `id` WHERE `deleted` = 1;
UPDATE `sys_user_menu` SET `deleted` = `id` WHERE `deleted` = 1;
UPDATE `sys_role_menu` SET `deleted` = `id` WHERE `deleted` = 1;
