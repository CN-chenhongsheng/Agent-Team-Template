-- =============================================
-- 报修表迁移：删除冗余字段（执行前请备份）
-- 适用于已存在旧版 sys_repair 的库，新建库请直接执行 repair_init.sql
-- @author 陈鸿昇
-- @since 2026-01-29
-- =============================================

-- 一次性执行；若列已删除会报错，可忽略
ALTER TABLE `sys_repair`
    DROP COLUMN `student_name`,
    DROP COLUMN `student_no`,
    DROP COLUMN `room_code`,
    DROP COLUMN `bed_code`;
