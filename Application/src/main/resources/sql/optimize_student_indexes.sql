-- ==============================================================================
-- 学生表分页查询性能优化 SQL 脚本
-- ==============================================================================
-- 功能：优化 sys_student 表的索引，以支持大数据量（900w+）的分页查询
-- 执行前：备份数据库
-- 注意：索引操作会锁表，建议在业务低谷时执行
-- ==============================================================================

-- 1. 添加创建时间和删除标记复合索引（用于分页排序）
-- 用途：支持 ORDER BY create_time DESC 且 WHERE deleted = 0 的查询
ALTER TABLE `sys_student` ADD INDEX `idx_create_time_deleted` (`deleted` ASC, `create_time` DESC) USING BTREE COMMENT '分页排序索引';

-- 2. 添加状态、删除标记和创建时间复合索引（用于状态过滤和排序）
-- 用途：支持按状态过滤且按创建时间排序的查询
ALTER TABLE `sys_student` ADD INDEX `idx_status_deleted_created` (`status` ASC, `deleted` ASC, `create_time` DESC) USING BTREE COMMENT '状态过滤排序索引';

-- 3. 添加组织结构复合索引（用于按校区、院系过滤）
-- 用途：支持按校区、院系、专业的多条件过滤
ALTER TABLE `sys_student` ADD INDEX `idx_campus_dept_major` (`campus_code` ASC, `dept_code` ASC, `major_code` ASC, `deleted` ASC) USING BTREE COMMENT '组织结构过滤索引';

-- 4. 删除冗余索引（与主键索引重复功能）
-- 注意：如果索引已删除会报错，可使用 DROP INDEX IF EXISTS（MySQL 5.7+）
ALTER TABLE `sys_student` DROP INDEX `idx_class` IF EXISTS;
ALTER TABLE `sys_student` DROP INDEX `idx_major` IF EXISTS;

-- 5. 查看表中所有索引
-- 执行结果用于验证优化效果
SELECT
    TABLE_NAME,
    INDEX_NAME,
    SEQ_IN_INDEX,
    COLUMN_NAME,
    COLLATION,
    CARDINALITY
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sys_student'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- 6. 查看表统计信息
SELECT
    TABLE_NAME,
    TABLE_ROWS,
    DATA_LENGTH / 1024 / 1024 AS 'DATA_SIZE_MB',
    INDEX_LENGTH / 1024 / 1024 AS 'INDEX_SIZE_MB'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sys_student';

-- ==============================================================================
-- 常见查询的解释计划检查
-- ==============================================================================

-- 检查分页查询的执行计划（应该使用 idx_create_time_deleted）
-- EXPLAIN SELECT * FROM `sys_student` WHERE `deleted` = 0 ORDER BY `create_time` DESC LIMIT 0, 10;

-- 检查按状态过滤的查询（应该使用 idx_status_deleted_created）
-- EXPLAIN SELECT * FROM `sys_student` WHERE `status` = 1 AND `deleted` = 0 ORDER BY `create_time` DESC LIMIT 0, 10;

-- 检查按组织结构过滤的查询（应该使用 idx_campus_dept_major）
-- EXPLAIN SELECT * FROM `sys_student` WHERE `campus_code` = 'BJ' AND `deleted` = 0 LIMIT 0, 10;
