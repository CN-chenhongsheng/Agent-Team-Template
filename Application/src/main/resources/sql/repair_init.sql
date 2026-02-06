-- =============================================
-- 报修功能数据库初始化脚本（完整 sys_repair 表）
-- 学生/房间/床位信息通过关联表查询，不冗余存储
-- @author 陈鸿昇
-- @since 2026-01-29
-- =============================================

-- 1. 报修表（完整 DDL）
DROP TABLE IF EXISTS `sys_repair`;

CREATE TABLE `sys_repair` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID（关联 sys_student）',
    `room_id` BIGINT COMMENT '房间ID（关联 sys_room）',
    `bed_id` BIGINT COMMENT '床位ID（关联 sys_bed）',
    `repair_type` INT NOT NULL COMMENT '维修类型：1-水电 2-门窗 3-家具 4-网络 5-其他',
    `fault_description` TEXT NOT NULL COMMENT '故障描述',
    `fault_images` JSON COMMENT '故障图片（JSON数组）',
    `urgent_level` INT NOT NULL DEFAULT 1 COMMENT '紧急程度：1-一般 2-紧急 3-非常紧急',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-待接单 2-已接单 3-维修中 4-已完成 5-已取消',
    `repair_person_id` BIGINT COMMENT '维修人员ID',
    `repair_person_name` VARCHAR(50) COMMENT '维修人员姓名',
    `appointment_time` DATETIME COMMENT '预约时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `repair_result` TEXT COMMENT '维修结果描述',
    `repair_images` JSON COMMENT '维修后图片（JSON数组）',
    `rating` INT COMMENT '评分：1-5星',
    `rating_comment` VARCHAR(500) COMMENT '评价内容',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_status` (`status`),
    KEY `idx_repair_type` (`repair_type`),
    KEY `idx_repair_person_id` (`repair_person_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报修管理表';

-- 2. 报修相关字典：先删后插，避免重复执行报错
DELETE FROM `sys_dict_data` WHERE `dict_code` IN ('repair_type', 'repair_status', 'repair_urgent_level', 'repair_rating');
DELETE FROM `sys_dict_type` WHERE `dict_code` IN ('repair_type', 'repair_status', 'repair_urgent_level', 'repair_rating');

-- 添加字典类型（与项目 sys_dict_type 表结构一致：dict_name, dict_code, status, remark）
INSERT INTO `sys_dict_type` (`dict_name`, `dict_code`, `status`, `remark`) VALUES
    ('维修类型', 'repair_type', 1, '报修的维修类型'),
    ('报修状态', 'repair_status', 1, '报修单的状态：1-待接单 2-已接单 3-维修中 4-已完成 5-已取消'),
    ('紧急程度', 'repair_urgent_level', 1, '报修的紧急程度：1-一般 2-紧急 3-非常紧急'),
    ('维修评分', 'repair_rating', 1, '报修完成后的评分：1-5星');

-- 3. 添加字典数据 - 维修类型（与项目 sys_dict_data 表结构一致：dict_code, label, value, sort, remark, status）
INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `sort`, `remark`, `status`) VALUES
    ('repair_type', '水电', '1', 1, '水管、电路等问题', 1),
    ('repair_type', '门窗', '2', 2, '门锁、窗户等问题', 1),
    ('repair_type', '家具', '3', 3, '床、桌椅等问题', 1),
    ('repair_type', '网络', '4', 4, '网络连接问题', 1),
    ('repair_type', '其他', '5', 5, '其他类型问题', 1);

-- 4. 添加字典数据 - 报修状态
INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `sort`, `remark`, `status`) VALUES
    ('repair_status', '待接单', '1', 1, '报修已提交，等待维修人员接单', 1),
    ('repair_status', '已接单', '2', 2, '维修人员已接单', 1),
    ('repair_status', '维修中', '3', 3, '维修人员正在维修', 1),
    ('repair_status', '已完成', '4', 4, '维修已完成', 1),
    ('repair_status', '已取消', '5', 5, '报修已取消', 1);

-- 5. 添加字典数据 - 紧急程度
INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `sort`, `remark`, `status`) VALUES
    ('repair_urgent_level', '一般', '1', 1, '一般问题，可等待处理', 1),
    ('repair_urgent_level', '紧急', '2', 2, '紧急问题，需要尽快处理', 1),
    ('repair_urgent_level', '非常紧急', '3', 3, '非常紧急，需要立即处理', 1);

-- 6. 添加字典数据 - 维修评分
INSERT INTO `sys_dict_data` (`dict_code`, `label`, `value`, `sort`, `remark`, `status`) VALUES
    ('repair_rating', '1星', '1', 1, '非常不满意', 1),
    ('repair_rating', '2星', '2', 2, '不满意', 1),
    ('repair_rating', '3星', '3', 3, '一般', 1),
    ('repair_rating', '4星', '4', 4, '满意', 1),
    ('repair_rating', '5星', '5', 5, '非常满意', 1);
