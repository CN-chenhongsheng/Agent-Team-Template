/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : project_management

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 06/02/2026 15:55:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_academic_year
-- ----------------------------
DROP TABLE IF EXISTS `sys_academic_year`;
CREATE TABLE `sys_academic_year`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `year_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学年编码',
  `year_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学年名称',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态：0-未开始 1-进行中 2-已结束',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_year_code`(`year_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学年表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_academic_year
-- ----------------------------
INSERT INTO `sys_academic_year` VALUES (11, '2021-2022', '2021-2022学年', '2021-09-01', '2022-08-31', 2, '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_academic_year` VALUES (12, '2022-2023', '2022-2023学年', '2022-09-01', '2023-08-31', 2, '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_academic_year` VALUES (13, '2023-2024', '2023-2024学年', '2023-09-01', '2024-08-31', 2, '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_academic_year` VALUES (14, '2024-2025', '2024-2025学年', '2024-09-01', '2025-08-31', 0, '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', 1, 0);
INSERT INTO `sys_academic_year` VALUES (15, '2025-2026', '2025-2026学年', '2025-09-01', '2026-08-31', 1, '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);

-- ----------------------------
-- Table structure for sys_allocation_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_allocation_config`;
CREATE TABLE `sys_allocation_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名称',
  `smoking_constraint` tinyint NULL DEFAULT 1 COMMENT '吸烟硬约束：吸烟者不能与不接受吸烟者同住',
  `gender_constraint` tinyint NULL DEFAULT 1 COMMENT '性别硬约束：不同性别不能同住',
  `sleep_hard_constraint` tinyint NULL DEFAULT 0 COMMENT '作息硬约束：作息差异≥3档不能同住',
  `sleep_weight` int NULL DEFAULT 30 COMMENT '睡眠维度权重（作息、打呼噜、睡眠敏感等）',
  `smoking_weight` int NULL DEFAULT 20 COMMENT '吸烟维度权重',
  `cleanliness_weight` int NULL DEFAULT 15 COMMENT '整洁维度权重',
  `social_weight` int NULL DEFAULT 15 COMMENT '社交维度权重（社交偏好、访客、电话等）',
  `study_weight` int NULL DEFAULT 10 COMMENT '学习维度权重',
  `entertainment_weight` int NULL DEFAULT 10 COMMENT '娱乐维度权重（电脑、游戏、音乐等）',
  `algorithm_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'kmeans' COMMENT '算法类型：greedy-贪心算法 kmeans-聚类算法 annealing-模拟退火',
  `same_dept_bonus` int NULL DEFAULT 5 COMMENT '同院系加分',
  `same_major_bonus` int NULL DEFAULT 10 COMMENT '同专业加分',
  `same_class_bonus` int NULL DEFAULT 15 COMMENT '同班级加分',
  `min_match_score` int NULL DEFAULT 60 COMMENT '最低匹配分阈值（低于此分需人工审核）',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用 0-停用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_algorithm`(`algorithm_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分配规则配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_allocation_config
-- ----------------------------
INSERT INTO `sys_allocation_config` VALUES (1, '默认配置模板', 1, 1, 0, 30, 20, 15, 15, 10, 10, 'kmeans', 5, 10, 15, 60, 1, '系统默认配置，可作为创建新配置的模板', '2026-02-02 22:49:37', NULL, '2026-02-02 22:49:37', 1, 0);
INSERT INTO `sys_allocation_config` VALUES (2, '严格匹配配置', 1, 1, 1, 35, 25, 15, 10, 10, 5, 'annealing', 5, 10, 15, 70, 1, '启用作息硬约束，追求更高匹配度', '2026-02-02 22:49:37', NULL, '2026-02-02 22:49:37', NULL, 0);
INSERT INTO `sys_allocation_config` VALUES (3, '快速分配配置', 1, 1, 0, 25, 20, 15, 15, 15, 10, 'greedy', 5, 10, 15, 50, 1, '使用贪心算法，适合时间紧迫场景', '2026-02-02 22:49:37', NULL, '2026-02-02 22:49:37', NULL, 0);

-- ----------------------------
-- Table structure for sys_allocation_result
-- ----------------------------
DROP TABLE IF EXISTS `sys_allocation_result`;
CREATE TABLE `sys_allocation_result`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生姓名',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `dept_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '院系编码',
  `major_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业编码',
  `class_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级编码',
  `allocated_bed_id` bigint NULL DEFAULT NULL COMMENT '分配的床位ID',
  `allocated_room_id` bigint NULL DEFAULT NULL COMMENT '分配的房间ID',
  `allocated_room_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '房间编码',
  `allocated_floor_id` bigint NULL DEFAULT NULL COMMENT '楼层ID',
  `allocated_floor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼层编码',
  `match_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '匹配分数(0-100)',
  `match_details` json NULL COMMENT '匹配详情（各维度得分）',
  `conflict_reasons` json NULL COMMENT '不匹配/冲突原因列表',
  `advantages` json NULL COMMENT '匹配优势列表',
  `roommate_ids` json NULL COMMENT '室友ID列表',
  `roommate_names` json NULL COMMENT '室友姓名列表',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待确认 1-已确认 2-已拒绝 3-已调整',
  `adjusted_bed_id` bigint NULL DEFAULT NULL COMMENT '调整后的床位ID',
  `adjust_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '调整原因',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '确认时间',
  `confirm_by` bigint NULL DEFAULT NULL COMMENT '确认人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_match_score`(`match_score` ASC) USING BTREE,
  INDEX `idx_room_id`(`allocated_room_id` ASC) USING BTREE,
  INDEX `idx_task_status`(`task_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_bed_id`(`allocated_bed_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分配结果表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_allocation_result
-- ----------------------------

-- ----------------------------
-- Table structure for sys_allocation_task
-- ----------------------------
DROP TABLE IF EXISTS `sys_allocation_task`;
CREATE TABLE `sys_allocation_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `task_type` tinyint NOT NULL COMMENT '任务类型：1-批量分配 2-单个推荐 3-调宿优化',
  `config_id` bigint NOT NULL COMMENT '使用的配置ID',
  `target_enrollment_year` int NULL DEFAULT NULL COMMENT '目标入学年份',
  `target_gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标性别：male/female/不限则为空',
  `target_campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标校区编码',
  `target_dept_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标院系编码',
  `target_major_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标专业编码',
  `target_floor_ids` json NULL COMMENT '目标楼层ID列表',
  `target_building_codes` json NULL COMMENT '目标楼栋编码列表',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待执行 1-执行中 2-已完成 3-部分确认 4-全部确认 5-已取消',
  `total_students` int NULL DEFAULT 0 COMMENT '待分配学生总数',
  `total_beds` int NULL DEFAULT 0 COMMENT '可用床位总数',
  `allocated_count` int NULL DEFAULT 0 COMMENT '已分配数',
  `confirmed_count` int NULL DEFAULT 0 COMMENT '已确认数',
  `failed_count` int NULL DEFAULT 0 COMMENT '分配失败数（无法匹配）',
  `low_score_count` int NULL DEFAULT 0 COMMENT '低于阈值数',
  `avg_match_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '平均匹配分',
  `min_match_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '最低匹配分',
  `max_match_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '最高匹配分',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始执行时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '执行完成时间',
  `execute_duration` int NULL DEFAULT NULL COMMENT '执行耗时（秒）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_config_id`(`config_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_task_type`(`task_type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_enrollment_year`(`target_enrollment_year` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分配任务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_allocation_task
-- ----------------------------

-- ----------------------------
-- Table structure for sys_approval_flow
-- ----------------------------
DROP TABLE IF EXISTS `sys_approval_flow`;
CREATE TABLE `sys_approval_flow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `flow_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程名称',
  `flow_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程编码(唯一)',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型(check_in/transfer/check_out/stay)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流程描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flow_code`(`flow_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_business_type`(`business_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '审批流程定义表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_approval_flow
-- ----------------------------
INSERT INTO `sys_approval_flow` VALUES (1, '入住流程', '#001', 'check_in', '这是一个入住管理的配置流程。', 1, NULL, '2026-01-17 22:16:19', 1, '2026-01-25 15:09:07', 1, 0);
INSERT INTO `sys_approval_flow` VALUES (2, '调宿流程', '#002', 'transfer', '这是一条调宿申请流程', 1, NULL, '2026-01-25 15:06:46', 1, '2026-01-25 15:08:55', 1, 0);
INSERT INTO `sys_approval_flow` VALUES (3, '退宿流程', '#003', 'check_out', '这是一条退宿申请流程。', 1, NULL, '2026-01-25 15:32:53', 1, '2026-01-25 15:32:53', 1, 0);
INSERT INTO `sys_approval_flow` VALUES (4, '留宿流程', '#004', 'stay', '这是一条留宿申请流程。', 1, NULL, '2026-01-25 23:01:26', 1, '2026-01-25 23:01:26', 1, 0);

-- ----------------------------
-- Table structure for sys_approval_flow_binding
-- ----------------------------
DROP TABLE IF EXISTS `sys_approval_flow_binding`;
CREATE TABLE `sys_approval_flow_binding`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型(check_in/transfer/check_out/stay)',
  `flow_id` bigint NOT NULL COMMENT '绑定的流程ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_business_type`(`business_type` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_flow_id`(`flow_id` ASC) USING BTREE,
  INDEX `idx_business_type`(`business_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '审批流程绑定表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_approval_flow_binding
-- ----------------------------
INSERT INTO `sys_approval_flow_binding` VALUES (6, 'check_in', 1, 1, NULL, '2026-01-19 12:14:08', 1, '2026-01-19 12:14:08', 1, 0);
INSERT INTO `sys_approval_flow_binding` VALUES (7, 'transfer', 2, 1, NULL, '2026-01-25 15:06:55', 1, '2026-01-25 15:06:55', 1, 0);
INSERT INTO `sys_approval_flow_binding` VALUES (8, 'check_out', 3, 1, NULL, '2026-01-25 15:32:58', 1, '2026-01-25 22:09:28', 1, 0);
INSERT INTO `sys_approval_flow_binding` VALUES (9, 'stay', 4, 1, NULL, '2026-01-25 23:01:34', 1, '2026-01-25 23:01:34', 1, 0);

-- ----------------------------
-- Table structure for sys_approval_instance
-- ----------------------------
DROP TABLE IF EXISTS `sys_approval_instance`;
CREATE TABLE `sys_approval_instance`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `flow_id` bigint NOT NULL COMMENT '流程ID',
  `flow_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流程名称(冗余)',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型',
  `business_id` bigint NOT NULL COMMENT '业务数据ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `applicant_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请人姓名(冗余)',
  `current_node_id` bigint NULL DEFAULT NULL COMMENT '当前节点ID',
  `current_node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '当前节点名称(冗余)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-审批中 2-已通过 3-已拒绝 4-已撤销',
  `start_time` datetime NULL DEFAULT NULL COMMENT '流程开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '流程结束时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_business`(`business_type` ASC, `business_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_flow_id`(`flow_id` ASC) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE,
  INDEX `idx_applicant_id`(`applicant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_current_node`(`current_node_id` ASC) USING BTREE,
  INDEX `idx_applicant_status`(`applicant_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_flow`(`flow_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '审批实例表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_approval_instance
-- ----------------------------

-- ----------------------------
-- Table structure for sys_approval_node
-- ----------------------------
DROP TABLE IF EXISTS `sys_approval_node`;
CREATE TABLE `sys_approval_node`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `flow_id` bigint NOT NULL COMMENT '流程ID',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '节点名称',
  `node_order` int NOT NULL DEFAULT 1 COMMENT '节点顺序',
  `node_type` int NOT NULL COMMENT '节点类型：1-审批节点 2-抄送节点 3-条件节点',
  `reject_action` tinyint NOT NULL DEFAULT 1 COMMENT '拒绝处理：1直接结束 2退回申请人 3退回上一节点',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_flow_id`(`flow_id` ASC) USING BTREE,
  INDEX `idx_node_order`(`flow_id` ASC, `node_order` ASC) USING BTREE,
  INDEX `idx_flow`(`flow_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '审批流程节点表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_approval_node
-- ----------------------------
INSERT INTO `sys_approval_node` VALUES (1, 1, '宿舍阿姨', 1, 1, 1, NULL, '2026-01-25 15:09:07', 1, '2026-01-25 15:09:07', 1, 0);
INSERT INTO `sys_approval_node` VALUES (2, 1, '管理员', 2, 1, 1, NULL, '2026-01-25 15:09:07', 1, '2026-01-25 15:09:07', 1, 0);
INSERT INTO `sys_approval_node` VALUES (3, 2, '宿舍阿姨', 1, 1, 1, NULL, '2026-01-25 15:08:55', 1, '2026-01-25 15:08:55', 1, 0);
INSERT INTO `sys_approval_node` VALUES (4, 2, '管理员', 2, 1, 3, NULL, '2026-01-25 15:08:55', 1, '2026-01-25 15:08:55', 1, 0);
INSERT INTO `sys_approval_node` VALUES (5, 3, '宿舍阿姨', 1, 1, 1, NULL, '2026-01-25 15:32:53', 1, '2026-01-25 15:32:53', 1, 0);
INSERT INTO `sys_approval_node` VALUES (6, 3, '管理员', 2, 1, 2, NULL, '2026-01-25 15:32:53', 1, '2026-01-25 15:32:53', 1, 0);
INSERT INTO `sys_approval_node` VALUES (7, 4, '宿管阿姨', 1, 1, 1, NULL, '2026-01-25 23:01:26', 1, '2026-01-25 23:01:26', 1, 0);
INSERT INTO `sys_approval_node` VALUES (8, 4, '管理员', 2, 1, 3, NULL, '2026-01-25 23:01:26', 1, '2026-01-25 23:01:26', 1, 0);

-- ----------------------------
-- Table structure for sys_approval_node_assignee
-- ----------------------------
DROP TABLE IF EXISTS `sys_approval_node_assignee`;
CREATE TABLE `sys_approval_node_assignee`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `node_id` bigint NOT NULL COMMENT '节点ID',
  `assignee_type` tinyint NOT NULL COMMENT '指派类型：1角色 2用户',
  `assignee_id` bigint NOT NULL COMMENT '角色ID或用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_node_id`(`node_id` ASC) USING BTREE,
  INDEX `idx_assignee`(`assignee_type` ASC, `assignee_id` ASC) USING BTREE,
  INDEX `idx_node`(`node_id` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE,
  INDEX `idx_node_deleted`(`node_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '审批节点审批人表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_approval_node_assignee
-- ----------------------------
INSERT INTO `sys_approval_node_assignee` VALUES (13, 3, 2, 2, '2026-01-25 15:08:55', NULL, 1, NULL, 0);
INSERT INTO `sys_approval_node_assignee` VALUES (14, 4, 2, 1, '2026-01-25 15:08:55', NULL, 1, NULL, 0);
INSERT INTO `sys_approval_node_assignee` VALUES (15, 1, 2, 2, '2026-01-25 15:09:07', NULL, 1, NULL, 0);
INSERT INTO `sys_approval_node_assignee` VALUES (16, 2, 2, 1, '2026-01-25 15:09:07', NULL, 1, NULL, 0);
INSERT INTO `sys_approval_node_assignee` VALUES (17, 5, 2, 2, '2026-01-25 15:32:53', NULL, 1, NULL, 0);
INSERT INTO `sys_approval_node_assignee` VALUES (18, 6, 2, 1, '2026-01-25 15:32:53', NULL, 1, NULL, 0);
INSERT INTO `sys_approval_node_assignee` VALUES (19, 7, 2, 2, '2026-01-25 23:01:26', NULL, 1, NULL, 0);
INSERT INTO `sys_approval_node_assignee` VALUES (20, 8, 2, 1, '2026-01-25 23:01:26', NULL, 1, NULL, 0);

-- ----------------------------
-- Table structure for sys_approval_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_approval_record`;
CREATE TABLE `sys_approval_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `instance_id` bigint NOT NULL COMMENT '审批实例ID',
  `node_id` bigint NOT NULL COMMENT '节点ID',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点名称(冗余)',
  `approver_id` bigint NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审批人姓名(冗余)',
  `action` tinyint NOT NULL COMMENT '审批结果：1-通过 2-拒绝 3-转交',
  `opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审批意见',
  `approve_time` datetime NOT NULL COMMENT '审批时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_instance_id`(`instance_id` ASC) USING BTREE,
  INDEX `idx_node_id`(`node_id` ASC) USING BTREE,
  INDEX `idx_approver_id`(`approver_id` ASC) USING BTREE,
  INDEX `idx_approve_time`(`approve_time` ASC) USING BTREE,
  INDEX `idx_instance`(`instance_id` ASC) USING BTREE,
  INDEX `idx_approver_status`(`approver_id` ASC, `action` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '审批记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_approval_record
-- ----------------------------

-- ----------------------------
-- Table structure for sys_bed
-- ----------------------------
DROP TABLE IF EXISTS `sys_bed`;
CREATE TABLE `sys_bed`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bed_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '床位编码（如：101-1、101-2）',
  `bed_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '床位号（如：1、2、3、4）',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `room_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属房间编码（冗余字段）',
  `floor_id` bigint NULL DEFAULT NULL COMMENT '所属楼层ID（冗余字段）',
  `floor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属楼层编码（冗余字段）',
  `campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属校区编码（冗余字段）',
  `bed_position` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '床位位置（字典bed_position）：上铺、下铺、左、右等',
  `bed_status` tinyint NOT NULL DEFAULT 1 COMMENT '床位状态：1-空闲 2-已分配 3-维修中 4-停用',
  `student_id` bigint NULL DEFAULT NULL COMMENT '当前入住学生ID（关联学生表，可为空）',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前入住学生姓名（冗余字段）',
  `check_in_date` date NULL DEFAULT NULL COMMENT '入住日期',
  `check_out_date` date NULL DEFAULT NULL COMMENT '退宿日期',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_bed_room_code`(`room_id` ASC, `bed_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_room_code`(`room_code` ASC) USING BTREE,
  INDEX `idx_floor_id`(`floor_id` ASC) USING BTREE,
  INDEX `idx_campus_code`(`campus_code` ASC) USING BTREE,
  INDEX `idx_bed_code`(`bed_code` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_room_status`(`room_id` ASC, `bed_status` ASC) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '床位表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_bed
-- ----------------------------
INSERT INTO `sys_bed` VALUES (1, 'F1-0101-1', '1', 1, 'F1-0101', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 1, 1, NULL, 0, '2026-02-04 10:51:56', 1, '2026-02-04 10:51:56', 1);
INSERT INTO `sys_bed` VALUES (2, 'F1-0101-2', '2', 1, 'F1-0101', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 2, 1, NULL, 0, '2026-02-04 10:51:56', 1, '2026-02-04 10:51:56', 1);
INSERT INTO `sys_bed` VALUES (3, 'F1-0101-3', '3', 1, 'F1-0101', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 3, 1, NULL, 0, '2026-02-04 10:51:56', 1, '2026-02-04 10:51:56', 1);
INSERT INTO `sys_bed` VALUES (4, 'F1-0101-4', '4', 1, 'F1-0101', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 4, 1, NULL, 0, '2026-02-04 10:51:56', 1, '2026-02-04 10:51:56', 1);
INSERT INTO `sys_bed` VALUES (5, 'F1-0101-5', '5', 1, 'F1-0101', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 5, 1, NULL, 0, '2026-02-04 10:51:56', 1, '2026-02-04 10:51:56', 1);
INSERT INTO `sys_bed` VALUES (6, 'F1-0101-6', '6', 1, 'F1-0101', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 6, 1, NULL, 0, '2026-02-04 10:51:56', 1, '2026-02-04 10:51:56', 1);
INSERT INTO `sys_bed` VALUES (7, 'F2-0101-1', '1', 31, 'F2-0101', 2, 'F2', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 1, 1, NULL, 0, '2026-02-04 10:51:59', 1, '2026-02-04 10:51:59', 1);
INSERT INTO `sys_bed` VALUES (8, 'F2-0101-2', '2', 31, 'F2-0101', 2, 'F2', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 2, 1, NULL, 0, '2026-02-04 10:51:59', 1, '2026-02-04 10:51:59', 1);
INSERT INTO `sys_bed` VALUES (9, 'F2-0101-3', '3', 31, 'F2-0101', 2, 'F2', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 3, 1, NULL, 0, '2026-02-04 10:51:59', 1, '2026-02-04 10:51:59', 1);
INSERT INTO `sys_bed` VALUES (10, 'F2-0101-4', '4', 31, 'F2-0101', 2, 'F2', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 4, 1, NULL, 0, '2026-02-04 10:51:59', 1, '2026-02-04 10:51:59', 1);
INSERT INTO `sys_bed` VALUES (11, 'F3-0101-1', '1', 56, 'F3-0101', 3, 'F3', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 1, 1, NULL, 0, '2026-02-04 10:52:01', 1, '2026-02-04 10:52:01', 1);
INSERT INTO `sys_bed` VALUES (12, 'F3-0101-2', '2', 56, 'F3-0101', 3, 'F3', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 2, 1, NULL, 0, '2026-02-04 10:52:01', 1, '2026-02-04 10:52:01', 1);
INSERT INTO `sys_bed` VALUES (13, 'F3-0101-3', '3', 56, 'F3-0101', 3, 'F3', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 3, 1, NULL, 0, '2026-02-04 10:52:01', 1, '2026-02-04 10:52:01', 1);
INSERT INTO `sys_bed` VALUES (14, 'F3-0101-4', '4', 56, 'F3-0101', 3, 'F3', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 4, 1, NULL, 0, '2026-02-04 10:52:01', 1, '2026-02-04 10:52:01', 1);
INSERT INTO `sys_bed` VALUES (15, 'F1-0102-1', '1', 2, 'F1-0102', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 1, 1, NULL, 0, '2026-02-04 10:52:04', 1, '2026-02-04 10:52:04', 1);
INSERT INTO `sys_bed` VALUES (16, 'F1-0102-2', '2', 2, 'F1-0102', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 2, 1, NULL, 0, '2026-02-04 10:52:04', 1, '2026-02-04 10:52:04', 1);
INSERT INTO `sys_bed` VALUES (17, 'F1-0102-3', '3', 2, 'F1-0102', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 3, 1, NULL, 0, '2026-02-04 10:52:04', 1, '2026-02-04 10:52:04', 1);
INSERT INTO `sys_bed` VALUES (18, 'F1-0102-4', '4', 2, 'F1-0102', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 4, 1, NULL, 0, '2026-02-04 10:52:04', 1, '2026-02-04 10:52:04', 1);
INSERT INTO `sys_bed` VALUES (19, 'F1-0102-5', '5', 2, 'F1-0102', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 5, 1, NULL, 0, '2026-02-04 10:52:04', 1, '2026-02-04 10:52:04', 1);
INSERT INTO `sys_bed` VALUES (20, 'F1-0102-6', '6', 2, 'F1-0102', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 6, 1, NULL, 0, '2026-02-04 10:52:04', 1, '2026-02-04 10:52:04', 1);
INSERT INTO `sys_bed` VALUES (21, 'F3-0102-1', '1', 57, 'F3-0102', 3, 'F3', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 1, 1, NULL, 0, '2026-02-04 10:52:06', 1, '2026-02-04 10:52:06', 1);
INSERT INTO `sys_bed` VALUES (22, 'F3-0102-2', '2', 57, 'F3-0102', 3, 'F3', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 2, 1, NULL, 0, '2026-02-04 10:52:06', 1, '2026-02-04 10:52:06', 1);
INSERT INTO `sys_bed` VALUES (23, 'F3-0102-3', '3', 57, 'F3-0102', 3, 'F3', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 3, 1, NULL, 0, '2026-02-04 10:52:06', 1, '2026-02-04 10:52:06', 1);
INSERT INTO `sys_bed` VALUES (24, 'F3-0102-4', '4', 57, 'F3-0102', 3, 'F3', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 4, 1, NULL, 0, '2026-02-04 10:52:06', 1, '2026-02-04 10:52:06', 1);
INSERT INTO `sys_bed` VALUES (25, 'F2-0103-1', '1', 33, 'F2-0103', 2, 'F2', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 1, 1, NULL, 0, '2026-02-04 10:52:09', 1, '2026-02-04 10:52:09', 1);
INSERT INTO `sys_bed` VALUES (26, 'F2-0103-2', '2', 33, 'F2-0103', 2, 'F2', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 2, 1, NULL, 0, '2026-02-04 10:52:09', 1, '2026-02-04 10:52:09', 1);
INSERT INTO `sys_bed` VALUES (27, 'F2-0103-3', '3', 33, 'F2-0103', 2, 'F2', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 3, 1, NULL, 0, '2026-02-04 10:52:09', 1, '2026-02-04 10:52:09', 1);
INSERT INTO `sys_bed` VALUES (28, 'F2-0103-4', '4', 33, 'F2-0103', 2, 'F2', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 4, 1, NULL, 0, '2026-02-04 10:52:09', 1, '2026-02-04 10:52:09', 1);
INSERT INTO `sys_bed` VALUES (29, 'F1-0104-1', '1', 4, 'F1-0104', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 1, 1, NULL, 0, '2026-02-04 10:52:11', 1, '2026-02-04 10:52:11', 1);
INSERT INTO `sys_bed` VALUES (30, 'F1-0104-2', '2', 4, 'F1-0104', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 2, 1, NULL, 0, '2026-02-04 10:52:11', 1, '2026-02-04 10:52:11', 1);
INSERT INTO `sys_bed` VALUES (31, 'F1-0104-3', '3', 4, 'F1-0104', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 3, 1, NULL, 0, '2026-02-04 10:52:11', 1, '2026-02-04 10:52:11', 1);
INSERT INTO `sys_bed` VALUES (32, 'F1-0104-4', '4', 4, 'F1-0104', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 4, 1, NULL, 0, '2026-02-04 10:52:11', 1, '2026-02-04 10:52:11', 1);
INSERT INTO `sys_bed` VALUES (33, 'F1-0104-5', '5', 4, 'F1-0104', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 5, 1, NULL, 0, '2026-02-04 10:52:11', 1, '2026-02-04 10:52:11', 1);
INSERT INTO `sys_bed` VALUES (34, 'F1-0104-6', '6', 4, 'F1-0104', 1, 'F1', 'CAMPUS001', NULL, 1, NULL, NULL, NULL, NULL, 6, 1, NULL, 0, '2026-02-04 10:52:11', 1, '2026-02-04 10:52:11', 1);

-- ----------------------------
-- Table structure for sys_campus
-- ----------------------------
DROP TABLE IF EXISTS `sys_campus`;
CREATE TABLE `sys_campus`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '校区编码',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '校区名称',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '校区地址',
  `manager` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '负责人',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_campus_code`(`campus_code` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '校区表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_campus
-- ----------------------------
INSERT INTO `sys_campus` VALUES (1, 'CAMPUS001', '主校区', '北京市海淀区中关村大街1号', '张三', 1, 1, '2025-12-31 12:51:08', NULL, '2026-01-21 16:52:40', 1, 0);

-- ----------------------------
-- Table structure for sys_check_in
-- ----------------------------
DROP TABLE IF EXISTS `sys_check_in`;
CREATE TABLE `sys_check_in`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生姓名（冗余）',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号（冗余）',
  `check_in_type` int NOT NULL COMMENT '入住类型：1-新生入住 2-老生返校 3-调宿入住 4-其他',
  `campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '校区编码',
  `floor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼层编码',
  `room_id` bigint NULL DEFAULT NULL COMMENT '房间ID',
  `room_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '房间编码',
  `bed_id` bigint NULL DEFAULT NULL COMMENT '床位ID',
  `bed_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '床位编码',
  `apply_date` date NULL DEFAULT NULL COMMENT '申请日期',
  `check_in_date` date NULL DEFAULT NULL COMMENT '入住日期',
  `expected_check_out_date` date NULL DEFAULT NULL COMMENT '预计退宿日期（临时住宿）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-待审核 2-已通过 3-已拒绝 4-已完成',
  `approval_instance_id` bigint NULL DEFAULT NULL COMMENT '审批实例ID',
  `approver_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `approver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核人姓名',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `approve_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核意见',
  `apply_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '申请原因/备注',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_student_no`(`student_no` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_check_in_date`(`check_in_date` ASC) USING BTREE,
  INDEX `idx_bed_id`(`bed_id` ASC) USING BTREE,
  INDEX `idx_approval_instance`(`approval_instance_id` ASC) USING BTREE,
  INDEX `idx_student_status`(`student_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_apply_date`(`apply_date` ASC) USING BTREE,
  INDEX `idx_bed`(`bed_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '入住管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_check_in
-- ----------------------------

-- ----------------------------
-- Table structure for sys_check_out
-- ----------------------------
DROP TABLE IF EXISTS `sys_check_out`;
CREATE TABLE `sys_check_out`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生姓名（冗余）',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号（冗余）',
  `campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '校区编码',
  `floor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼层编码',
  `room_id` bigint NULL DEFAULT NULL COMMENT '房间ID',
  `room_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '房间编码',
  `bed_id` bigint NULL DEFAULT NULL COMMENT '床位ID',
  `bed_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '床位编码',
  `apply_date` date NULL DEFAULT NULL COMMENT '申请日期',
  `check_out_date` date NULL DEFAULT NULL COMMENT '退宿日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-待审核 2-已通过 3-已拒绝 4-已完成',
  `approval_instance_id` bigint NULL DEFAULT NULL COMMENT '审批实例ID',
  `approver_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `approver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核人姓名',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `approve_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核意见',
  `check_out_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退宿原因（文本说明）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_student_no`(`student_no` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_check_out_date`(`check_out_date` ASC) USING BTREE,
  INDEX `idx_bed_id`(`bed_id` ASC) USING BTREE,
  INDEX `idx_approval_instance`(`approval_instance_id` ASC) USING BTREE,
  INDEX `idx_student_status`(`student_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_apply_date`(`apply_date` ASC) USING BTREE,
  INDEX `idx_bed`(`bed_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '退宿管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_check_out
-- ----------------------------

-- ----------------------------
-- Table structure for sys_class
-- ----------------------------
DROP TABLE IF EXISTS `sys_class`;
CREATE TABLE `sys_class`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `class_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级编码',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级名称',
  `major_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属专业编码',
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '年级',
  `teacher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '负责人姓名（冗余字段）',
  `teacher_id` bigint NULL DEFAULT NULL COMMENT '负责人ID（关联sys_user）',
  `enrollment_year` int NOT NULL COMMENT '入学年份',
  `current_count` int NOT NULL DEFAULT 0 COMMENT '当前人数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_class_code`(`class_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_major_code`(`major_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_major`(`major_code` ASC) USING BTREE,
  INDEX `idx_grade`(`grade` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班级表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_class
-- ----------------------------
INSERT INTO `sys_class` VALUES (1, 'CS-SE202301', '2023级软件工程1班', 'CS-SE001', '2025', '测试用户', 2, 2025, 30, 1, '2025-12-31 12:51:17', NULL, '2026-01-06 16:00:34', 1, 0);
INSERT INTO `sys_class` VALUES (2, 'CS-SE202302', '2023级AI算法1班', 'CS-SE001', '2025', '测试用户', 2, 2025, 15, 1, '2026-01-02 22:51:09', 1, '2026-01-06 16:00:34', 1, 0);

-- ----------------------------
-- Table structure for sys_department
-- ----------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dept_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '院系编码',
  `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '院系名称',
  `campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属校区编码',
  `leader` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '院系领导',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dept_code`(`dept_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_campus_code`(`campus_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '院系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_department
-- ----------------------------
INSERT INTO `sys_department` VALUES (1, 'CS001', '计算机科学与技术学院', 'CAMPUS001', '李四', '010-12345678', 1, 1, '2025-12-31 12:51:11', NULL, '2026-01-21 16:54:33', 1, 0);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典标签',
  `value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典值',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'CSS类名',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认：1是 0否',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-停用 1-正常',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dict_code`(`dict_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort`(`sort` ASC) USING BTREE,
  INDEX `idx_type_sort`(`dict_code` ASC, `sort` ASC) USING BTREE,
  INDEX `idx_dict_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 323 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 'sys_user_sex', '男', '1', '', 'primary', 1, 0, 1, '男性', '2025-12-31 14:42:00', NULL, '2026-01-01 13:56:47', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (2, 'sys_user_sex', '女', '2', NULL, 'success', 2, 0, 1, '女性', '2025-12-31 14:42:00', NULL, '2025-12-31 14:42:00', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (3, 'sys_user_status', '正常', '1', NULL, 'success', 1, 1, 1, '正常状态', '2025-12-31 14:42:00', NULL, '2025-12-31 14:42:00', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (4, 'sys_user_status', '停用', '0', NULL, 'danger', 2, 0, 1, '停用状态', '2025-12-31 14:42:00', NULL, '2025-12-31 14:42:00', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (5, 'sys_switch', '开启', '1', NULL, 'success', 1, 0, 1, '开启状态', '2025-12-31 14:42:00', NULL, '2025-12-31 14:42:00', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (6, 'sys_switch', '关闭', '0', NULL, 'info', 2, 0, 1, '关闭状态', '2025-12-31 14:42:00', NULL, '2025-12-31 14:42:00', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (10, 'table_button_config', '新增', 'add', 'bg-theme/12 text-theme', NULL, 1, 0, 1, 'ri:add-fill', '2026-01-01 07:00:56', NULL, '2026-01-01 07:00:56', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (11, 'table_button_config', '编辑', 'edit', 'bg-secondary/12 text-secondary', '', 2, 0, 1, 'ri:pencil-line', '2026-01-01 07:00:56', NULL, '2026-01-01 15:12:49', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (12, 'table_button_config', '删除', 'delete', 'bg-error/12 text-error', NULL, 3, 0, 1, 'ri:delete-bin-5-line', '2026-01-01 07:00:56', NULL, '2026-01-01 07:00:56', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (13, 'table_button_config', '查看', 'view', 'bg-info/12 text-info', NULL, 4, 0, 1, 'ri:eye-line', '2026-01-01 07:00:56', NULL, '2026-01-01 07:00:56', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (14, 'table_button_config', '更多', 'more', '', NULL, 5, 0, 1, 'ri:more-2-fill', '2026-01-01 07:00:56', NULL, '2026-01-01 07:00:56', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (20, 'table_button_config', '分配', 'share', 'bg-info/12 text-info', '', 6, 0, 1, 'ri:share-line', '2026-01-01 15:10:14', NULL, '2026-01-01 15:11:15', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (21, 'table_button_config', '重置', 'reset', 'bg-secondary/12 text-secondary', '', 7, 0, 1, 'ri:shield-keyhole-line', '2026-01-01 15:12:38', NULL, '2026-01-01 15:12:52', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (22, 'degree_type', '专科', 'associate', '', NULL, 1, 0, 1, '专科', '2026-01-01 17:31:03', NULL, '2026-01-01 17:31:03', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (23, 'degree_type', '本科', 'bachelor', '', NULL, 2, 1, 1, '本科', '2026-01-01 17:31:03', NULL, '2026-01-01 17:31:03', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (24, 'degree_type', '研究生', 'graduate', '', NULL, 3, 0, 1, '研究生（包括硕士和博士）', '2026-01-01 17:31:03', NULL, '2026-01-01 17:31:03', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (25, 'sys_oper_business_type', '其它', '0', '', 'info', 1, 0, 1, '其它操作', '2026-01-01 20:40:03', NULL, '2026-01-01 20:40:03', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (26, 'sys_oper_business_type', '新增', '1', '', 'success', 2, 0, 1, '新增操作', '2026-01-01 20:40:03', NULL, '2026-01-01 20:40:03', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (27, 'sys_oper_business_type', '修改', '2', '', 'warning', 3, 0, 1, '修改操作', '2026-01-01 20:40:03', NULL, '2026-01-01 20:40:03', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (28, 'sys_oper_business_type', '删除', '3', '', 'danger', 4, 0, 1, '删除操作', '2026-01-01 20:40:03', NULL, '2026-01-01 20:40:03', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (29, 'sys_device_type', '桌面设备', '1', '', 'primary', 1, 1, 1, '桌面设备（PC、笔记本等）', '2026-01-01 20:40:14', NULL, '2026-01-01 20:40:14', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (30, 'sys_device_type', '移动设备', '2', '', 'success', 2, 0, 1, '移动设备（手机、平板等）', '2026-01-01 20:40:14', NULL, '2026-01-01 20:40:14', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (31, 'sys_device_type', '爬虫/Bot', '3', '', 'info', 3, 0, 1, '爬虫/Bot（搜索引擎、API调用等）', '2026-01-01 20:40:14', NULL, '2026-01-01 20:40:14', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (32, 'sys_user_online_status', '在线', '1', NULL, 'success', 1, 0, 1, NULL, '2026-01-02 07:38:42', NULL, '2026-01-02 07:38:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (33, 'sys_user_online_status', '离线', '0', NULL, 'info', 2, 0, 1, NULL, '2026-01-02 07:38:42', NULL, '2026-01-02 07:38:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (34, 'dormitory_room_type', '标准间', 'standard', NULL, 'primary', 1, 1, 1, '标准4人间', '2026-01-03 22:12:11', NULL, '2026-01-03 22:12:11', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (35, 'dormitory_room_type', '套间', 'suite', NULL, 'success', 2, 0, 1, '套间', '2026-01-03 22:12:12', NULL, '2026-01-03 22:12:12', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (36, 'dormitory_room_type', '单人间', 'single', NULL, 'info', 3, 0, 1, '单人间', '2026-01-03 22:12:12', NULL, '2026-01-03 22:12:12', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (37, 'dormitory_room_type', '双人间', 'double', NULL, 'warning', 4, 0, 1, '双人间', '2026-01-03 22:12:12', NULL, '2026-01-03 22:12:12', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (38, 'dormitory_bed_position', '上铺', 'upper', NULL, 'primary', 1, 1, 1, '上铺', '2026-01-03 22:12:12', NULL, '2026-01-03 22:12:12', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (39, 'dormitory_bed_position', '下铺', 'lower', NULL, 'success', 2, 0, 1, '下铺', '2026-01-03 22:12:12', NULL, '2026-01-03 22:12:12', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (40, 'dormitory_bed_position', '左侧', 'left', NULL, 'info', 3, 0, 1, '左侧床位', '2026-01-03 22:12:12', NULL, '2026-01-03 22:12:12', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (41, 'dormitory_bed_position', '右侧', 'right', NULL, 'warning', 4, 0, 1, '右侧床位', '2026-01-03 22:12:12', NULL, '2026-01-03 22:12:12', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (42, 'dormitory_room_status', '空闲', '1', NULL, 'success', 1, 1, 1, '房间空闲', '2026-01-03 22:12:13', NULL, '2026-01-03 22:12:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (43, 'dormitory_room_status', '已满', '2', NULL, 'danger', 2, 0, 1, '房间已满', '2026-01-03 22:12:13', NULL, '2026-01-03 22:12:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (44, 'dormitory_room_status', '维修中', '3', NULL, 'warning', 3, 0, 1, '房间维修中', '2026-01-03 22:12:13', NULL, '2026-01-03 22:12:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (45, 'dormitory_room_status', '已预订', '4', NULL, 'info', 4, 0, 1, '房间已预订', '2026-01-03 22:12:13', NULL, '2026-01-03 22:12:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (46, 'dormitory_bed_status', '空闲', '1', NULL, 'success', 1, 1, 1, '床位空闲', '2026-01-03 22:12:13', NULL, '2026-01-03 22:12:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (47, 'dormitory_bed_status', '已占用', '2', NULL, 'danger', 2, 0, 1, '床位已占用', '2026-01-03 22:12:13', NULL, '2026-01-03 22:12:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (48, 'dormitory_bed_status', '维修中', '3', NULL, 'warning', 3, 0, 1, '床位维修中', '2026-01-03 22:12:13', NULL, '2026-01-03 22:12:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (49, 'dormitory_bed_status', '已预订', '4', NULL, 'info', 4, 0, 1, '床位已预订', '2026-01-03 22:12:13', NULL, '2026-01-03 22:12:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (50, 'dormitory_room_facility', '空调', '1', NULL, NULL, 1, 0, 1, NULL, '2026-01-05 02:33:58', NULL, '2026-01-05 02:33:58', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (51, 'dormitory_room_facility', '独立卫生间', '2', NULL, NULL, 2, 0, 1, NULL, '2026-01-05 02:33:58', NULL, '2026-01-05 02:33:58', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (52, 'dormitory_room_facility', '阳台', '3', NULL, NULL, 3, 0, 1, NULL, '2026-01-05 02:33:58', NULL, '2026-01-05 02:33:58', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (53, 'dormitory_gender_type', '男生宿舍', '1', NULL, 'primary', 1, 0, 1, '男生宿舍', '2026-01-05 17:50:30', NULL, '2026-01-05 17:50:30', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (54, 'dormitory_gender_type', '女生宿舍', '2', NULL, 'success', 2, 0, 1, '女生宿舍', '2026-01-05 17:50:31', NULL, '2026-01-05 17:50:31', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (55, 'dormitory_gender_type', '混合宿舍', '3', NULL, 'info', 3, 1, 1, '混合宿舍', '2026-01-05 17:50:31', NULL, '2026-01-05 17:50:31', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (56, 'academic_status', '在读', '1', '', 'success', 1, 1, 1, '在读状态', '2026-01-06 09:51:20', NULL, '2026-01-06 09:51:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (57, 'academic_status', '休学', '2', '', 'warning', 2, 0, 1, '休学状态', '2026-01-06 09:51:21', NULL, '2026-01-06 09:51:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (58, 'academic_status', '毕业', '3', '', 'info', 3, 0, 1, '毕业状态', '2026-01-06 09:51:22', NULL, '2026-01-06 09:51:22', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (59, 'academic_status', '退学', '4', '', 'danger', 4, 0, 1, '退学状态', '2026-01-06 09:51:23', NULL, '2026-01-06 09:51:23', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (60, 'check_in_status', '待审核', '1', '', 'warning', 1, 0, 1, '待审核状态', '2026-01-06 09:51:24', NULL, '2026-01-06 09:51:24', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (61, 'check_in_status', '已通过', '2', '', 'success', 2, 0, 1, '已通过状态', '2026-01-06 09:51:25', NULL, '2026-01-06 09:51:25', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (62, 'check_in_status', '已拒绝', '3', '', 'danger', 3, 0, 1, '已拒绝状态', '2026-01-06 09:51:31', NULL, '2026-01-06 09:51:31', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (63, 'check_in_status', '已入住', '4', '', 'success', 4, 1, 1, '已入住状态', '2026-01-06 09:51:32', NULL, '2026-01-06 09:51:32', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (64, 'transfer_status', '待审核', '1', '', 'warning', 1, 0, 1, '待审核状态', '2026-01-06 09:51:33', NULL, '2026-01-06 09:51:33', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (65, 'transfer_status', '已通过', '2', '', 'success', 2, 0, 1, '已通过状态', '2026-01-06 09:51:34', NULL, '2026-01-06 09:51:34', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (66, 'transfer_status', '已拒绝', '3', '', 'danger', 3, 0, 1, '已拒绝状态', '2026-01-06 09:51:36', NULL, '2026-01-06 09:51:36', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (67, 'transfer_status', '已完成', '4', '', 'success', 4, 1, 1, '已完成状态', '2026-01-06 09:51:37', NULL, '2026-01-06 09:51:37', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (68, 'check_out_status', '待审核', '1', '', 'warning', 1, 0, 1, '待审核状态', '2026-01-06 09:51:38', NULL, '2026-01-06 09:51:38', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (69, 'check_out_status', '已通过', '2', '', 'success', 2, 0, 1, '已通过状态', '2026-01-06 09:51:39', NULL, '2026-01-06 09:51:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (70, 'check_out_status', '已拒绝', '3', '', 'danger', 3, 0, 1, '已拒绝状态', '2026-01-06 09:51:40', NULL, '2026-01-06 09:51:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (71, 'check_out_status', '已完成', '4', '', 'success', 4, 1, 1, '已完成状态', '2026-01-06 09:51:41', NULL, '2026-01-06 09:51:41', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (72, 'stay_status', '待审核', '1', '', 'warning', 1, 0, 1, '待审核状态', '2026-01-06 09:51:42', NULL, '2026-01-06 09:51:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (73, 'stay_status', '已通过', '2', '', 'success', 2, 0, 1, '已通过状态', '2026-01-06 09:51:43', NULL, '2026-01-06 09:51:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (74, 'stay_status', '已拒绝', '3', '', 'danger', 3, 0, 1, '已拒绝状态', '2026-01-06 09:51:44', NULL, '2026-01-06 09:51:44', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (75, 'stay_status', '已完成', '4', '', 'success', 4, 1, 1, '已完成状态', '2026-01-06 09:51:45', NULL, '2026-01-06 09:51:45', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (76, 'student_smoking_status', '不吸烟', '0', '', 'success', 1, 1, 1, '', '2026-01-10 19:50:17', NULL, '2026-01-10 19:50:17', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (77, 'student_smoking_status', '吸烟', '1', '', 'warning', 2, 0, 1, '', '2026-01-10 19:50:17', NULL, '2026-01-10 19:50:17', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (78, 'student_smoking_tolerance', '不接受', '0', '', 'info', 1, 1, 1, '', '2026-01-10 19:50:17', NULL, '2026-01-10 19:50:17', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (79, 'student_smoking_tolerance', '接受', '1', '', 'success', 2, 0, 1, '', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (80, 'student_sleep_schedule', '早睡早起(22:00-6:00)', '0', '', '', 1, 0, 1, '', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (81, 'student_sleep_schedule', '正常(23:00-7:00)', '1', '', '', 2, 1, 1, '', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (82, 'student_sleep_schedule', '晚睡晚起(24:00-8:00)', '2', '', '', 3, 0, 1, '', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (83, 'student_sleep_schedule', '夜猫子(01:00-9:00)', '3', '', '', 4, 0, 1, '', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (84, 'student_sleep_quality', '浅睡易醒', '0', '', 'warning', 1, 0, 1, '', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (85, 'student_sleep_quality', '正常', '1', '', 'success', 2, 1, 1, '', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (86, 'student_sleep_quality', '深睡', '2', '', 'success', 3, 0, 1, '', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (87, 'student_snores', '不打呼噜', '0', '', 'success', 1, 1, 1, '', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (88, 'student_snores', '打呼噜', '1', '', 'warning', 2, 0, 1, '', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (89, 'student_sensitive_to_light', '不敏感', '0', '', 'success', 1, 1, 1, '', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (90, 'student_sensitive_to_light', '敏感', '1', '', 'warning', 2, 0, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (91, 'student_sensitive_to_sound', '不敏感', '0', '', 'success', 1, 1, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (92, 'student_sensitive_to_sound', '敏感', '1', '', 'warning', 2, 0, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (93, 'student_cleanliness_level', '非常整洁', '1', '', 'success', 1, 0, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (94, 'student_cleanliness_level', '整洁', '2', '', 'success', 2, 1, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (95, 'student_cleanliness_level', '一般', '3', '', 'info', 3, 0, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (96, 'student_cleanliness_level', '随意', '4', '', 'warning', 4, 0, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (97, 'student_cleanliness_level', '不整洁', '5', '', 'danger', 5, 0, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (98, 'student_bedtime_cleanup', '不整理', '0', '', 'info', 1, 0, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (99, 'student_bedtime_cleanup', '偶尔整理', '1', '', 'info', 2, 1, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (100, 'student_bedtime_cleanup', '经常整理', '2', '', 'success', 3, 0, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (101, 'student_bedtime_cleanup', '总是整理', '3', '', 'success', 4, 0, 1, '', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (102, 'student_social_preference', '喜欢安静', '1', '', 'info', 1, 1, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (103, 'student_social_preference', '中等', '2', '', 'success', 2, 0, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (104, 'student_social_preference', '喜欢热闹', '3', '', 'warning', 3, 0, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (105, 'student_allow_visitors', '不允许', '0', '', 'info', 1, 0, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (106, 'student_allow_visitors', '偶尔可以', '1', '', 'warning', 2, 1, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (107, 'student_allow_visitors', '可以', '2', '', 'success', 3, 0, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (108, 'student_phone_call_time', '喜欢在宿舍打电话', '0', '', 'warning', 1, 0, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (109, 'student_phone_call_time', '偶尔在宿舍', '1', '', 'info', 2, 1, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (110, 'student_phone_call_time', '不在宿舍打电话', '2', '', 'success', 3, 0, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (111, 'student_study_in_room', '不在', '0', '', 'info', 1, 0, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (112, 'student_study_in_room', '偶尔', '1', '', 'info', 2, 1, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (113, 'student_study_in_room', '经常', '2', '', 'success', 3, 0, 1, '', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (114, 'student_study_in_room', '总是', '3', '', 'success', 4, 0, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (115, 'student_study_environment', '需要安静', '1', '', 'success', 1, 1, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (116, 'student_study_environment', '需要轻音乐', '2', '', 'info', 2, 0, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (117, 'student_study_environment', '可以接受声音', '3', '', 'warning', 3, 0, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (118, 'student_computer_usage_time', '不用', '0', '', 'info', 1, 0, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (119, 'student_computer_usage_time', '很少(1-2h/天)', '1', '', 'success', 2, 0, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (120, 'student_computer_usage_time', '正常(3-5h/天)', '2', '', 'success', 3, 1, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (121, 'student_computer_usage_time', '很多(6h+/天)', '3', '', 'warning', 4, 0, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (122, 'student_gaming_preference', '不玩游戏', '0', '', 'success', 1, 0, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (123, 'student_gaming_preference', '偶尔玩', '1', '', 'info', 2, 1, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (124, 'student_gaming_preference', '经常玩', '2', '', 'warning', 3, 0, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (125, 'student_music_preference', '不听', '0', '', 'info', 1, 0, 1, '', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (126, 'student_music_preference', '偶尔听', '1', '', 'info', 2, 1, 1, '', '2026-01-10 19:50:22', NULL, '2026-01-10 19:50:22', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (127, 'student_music_preference', '经常听', '2', '', 'warning', 3, 0, 1, '', '2026-01-10 19:50:22', NULL, '2026-01-10 19:50:22', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (128, 'student_music_volume', '喜欢小声', '1', '', 'success', 1, 1, 1, '', '2026-01-10 19:50:22', NULL, '2026-01-10 19:50:22', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (129, 'student_music_volume', '中等', '2', '', 'info', 2, 0, 1, '', '2026-01-10 19:50:22', NULL, '2026-01-10 19:50:22', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (130, 'student_music_volume', '喜欢大声', '3', '', 'warning', 3, 0, 1, '', '2026-01-10 19:50:22', NULL, '2026-01-10 19:50:22', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (131, 'student_eat_in_room', '不吃', '0', '', 'success', 1, 0, 1, '', '2026-01-10 19:50:22', NULL, '2026-01-10 19:50:22', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (132, 'student_eat_in_room', '偶尔', '1', '', 'info', 2, 1, 1, '', '2026-01-10 19:50:24', NULL, '2026-01-10 19:50:24', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (133, 'student_eat_in_room', '经常', '2', '', 'warning', 3, 0, 1, '', '2026-01-10 19:50:24', NULL, '2026-01-10 19:50:24', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (134, 'student_nation', '汉族', '汉族', '', 'info', 1, 1, 1, '', '2026-01-10 21:57:38', NULL, '2026-01-10 21:57:38', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (135, 'student_nation', '蒙古族', '蒙古族', '', 'info', 2, 0, 1, '', '2026-01-10 21:57:38', NULL, '2026-01-10 21:57:38', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (136, 'student_nation', '回族', '回族', '', 'info', 3, 0, 1, '', '2026-01-10 21:57:38', NULL, '2026-01-10 21:57:38', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (137, 'student_nation', '藏族', '藏族', '', 'info', 4, 0, 1, '', '2026-01-10 21:57:38', NULL, '2026-01-10 21:57:38', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (138, 'student_nation', '维吾尔族', '维吾尔族', '', 'info', 5, 0, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (139, 'student_nation', '苗族', '苗族', '', 'info', 6, 0, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (140, 'student_nation', '彝族', '彝族', '', 'info', 7, 0, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (141, 'student_nation', '壮族', '壮族', '', 'info', 8, 0, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (142, 'student_nation', '布依族', '布依族', '', 'info', 9, 0, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (143, 'student_nation', '朝鲜族', '朝鲜族', '', 'info', 10, 0, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (144, 'student_nation', '满族', '满族', '', 'info', 11, 0, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (145, 'student_nation', '侗族', '侗族', '', 'info', 12, 0, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (146, 'student_nation', '瑶族', '瑶族', '', 'info', 13, 0, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (147, 'student_nation', '白族', '白族', '', 'info', 14, 0, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (148, 'student_nation', '土家族', '土家族', '', 'info', 15, 0, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (149, 'student_political_status', '群众', '群众', '', 'info', 1, 1, 1, '', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (150, 'student_political_status', '共青团员', '共青团员', '', 'success', 2, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (151, 'student_political_status', '中共党员', '中共党员', '', 'warning', 3, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (152, 'student_political_status', '中共预备党员', '中共预备党员', '', 'warning', 4, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (153, 'student_political_status', '民革党员', '民革党员', '', 'info', 5, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (154, 'student_political_status', '民盟盟员', '民盟盟员', '', 'info', 6, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (155, 'student_political_status', '民建会员', '民建会员', '', 'info', 7, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (156, 'student_political_status', '民进会员', '民进会员', '', 'info', 8, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (157, 'student_political_status', '农工党党员', '农工党党员', '', 'info', 9, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (158, 'student_political_status', '致公党党员', '致公党党员', '', 'info', 10, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (159, 'student_political_status', '九三学社社员', '九三学社社员', '', 'info', 11, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (160, 'student_political_status', '台盟盟员', '台盟盟员', '', 'info', 12, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (161, 'student_political_status', '无党派人士', '无党派人士', '', 'info', 13, 0, 1, '', '2026-01-10 21:57:40', NULL, '2026-01-10 21:57:40', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (162, 'student_nation', '哈尼族', '哈尼族', '', 'info', 16, 0, 1, '', '2026-01-10 22:06:41', NULL, '2026-01-10 22:06:41', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (163, 'student_nation', '哈萨克族', '哈萨克族', '', 'info', 17, 0, 1, '', '2026-01-10 22:06:41', NULL, '2026-01-10 22:06:41', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (164, 'student_nation', '傣族', '傣族', '', 'info', 18, 0, 1, '', '2026-01-10 22:06:41', NULL, '2026-01-10 22:06:41', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (165, 'student_nation', '黎族', '黎族', '', 'info', 19, 0, 1, '', '2026-01-10 22:06:41', NULL, '2026-01-10 22:06:41', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (166, 'student_nation', '傈僳族', '傈僳族', '', 'info', 20, 0, 1, '', '2026-01-10 22:06:41', NULL, '2026-01-10 22:06:41', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (167, 'student_nation', '佤族', '佤族', '', 'info', 21, 0, 1, '', '2026-01-10 22:06:41', NULL, '2026-01-10 22:06:41', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (168, 'student_nation', '畲族', '畲族', '', 'info', 22, 0, 1, '', '2026-01-10 22:06:41', NULL, '2026-01-10 22:06:41', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (169, 'student_nation', '高山族', '高山族', '', 'info', 23, 0, 1, '', '2026-01-10 22:06:41', NULL, '2026-01-10 22:06:41', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (170, 'student_nation', '拉祜族', '拉祜族', '', 'info', 24, 0, 1, '', '2026-01-10 22:06:41', NULL, '2026-01-10 22:06:41', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (171, 'student_nation', '水族', '水族', '', 'info', 25, 0, 1, '', '2026-01-10 22:06:41', NULL, '2026-01-10 22:06:41', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (172, 'student_nation', '东乡族', '东乡族', '', 'info', 26, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (173, 'student_nation', '纳西族', '纳西族', '', 'info', 27, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (174, 'student_nation', '景颇族', '景颇族', '', 'info', 28, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (175, 'student_nation', '柯尔克孜族', '柯尔克孜族', '', 'info', 29, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (176, 'student_nation', '土族', '土族', '', 'info', 30, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (177, 'student_nation', '达斡尔族', '达斡尔族', '', 'info', 31, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (178, 'student_nation', '仫佬族', '仫佬族', '', 'info', 32, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (179, 'student_nation', '羌族', '羌族', '', 'info', 33, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (180, 'student_nation', '布朗族', '布朗族', '', 'info', 34, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (181, 'student_nation', '撒拉族', '撒拉族', '', 'info', 35, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (182, 'student_nation', '毛南族', '毛南族', '', 'info', 36, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (183, 'student_nation', '仡佬族', '仡佬族', '', 'info', 37, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (184, 'student_nation', '锡伯族', '锡伯族', '', 'info', 38, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (185, 'student_nation', '阿昌族', '阿昌族', '', 'info', 39, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (186, 'student_nation', '普米族', '普米族', '', 'info', 40, 0, 1, '', '2026-01-10 22:06:42', NULL, '2026-01-10 22:06:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (187, 'student_nation', '塔吉克族', '塔吉克族', '', 'info', 41, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (188, 'student_nation', '怒族', '怒族', '', 'info', 42, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (189, 'student_nation', '乌孜别克族', '乌孜别克族', '', 'info', 43, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (190, 'student_nation', '俄罗斯族', '俄罗斯族', '', 'info', 44, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (191, 'student_nation', '鄂温克族', '鄂温克族', '', 'info', 45, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (192, 'student_nation', '德昂族', '德昂族', '', 'info', 46, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (193, 'student_nation', '保安族', '保安族', '', 'info', 47, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (194, 'student_nation', '裕固族', '裕固族', '', 'info', 48, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (195, 'student_nation', '京族', '京族', '', 'info', 49, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (196, 'student_nation', '塔塔尔族', '塔塔尔族', '', 'info', 50, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (197, 'student_nation', '独龙族', '独龙族', '', 'info', 51, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (198, 'student_nation', '鄂伦春族', '鄂伦春族', '', 'info', 52, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (199, 'student_nation', '赫哲族', '赫哲族', '', 'info', 53, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (200, 'student_nation', '门巴族', '门巴族', '', 'info', 54, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (201, 'student_nation', '珞巴族', '珞巴族', '', 'info', 55, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (202, 'student_nation', '基诺族', '基诺族', '', 'info', 56, 0, 1, '', '2026-01-10 22:06:43', NULL, '2026-01-10 22:06:43', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (203, 'check_in_status', '已撤回', '5', NULL, NULL, 5, 0, 1, NULL, '2026-01-16 12:19:02', NULL, '2026-01-16 12:19:02', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (204, 'check_out_status', '已撤回', '5', NULL, NULL, 5, 0, 1, NULL, '2026-01-16 12:19:02', NULL, '2026-01-16 12:19:02', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (205, 'transfer_status', '已撤回', '5', NULL, NULL, 5, 0, 1, NULL, '2026-01-16 12:19:02', NULL, '2026-01-16 12:19:02', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (206, 'stay_status', '已撤回', '5', NULL, NULL, 5, 0, 1, NULL, '2026-01-16 12:19:02', NULL, '2026-01-16 12:19:02', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (207, 'business_type', '入住申请', 'check_in', '', '', 1, 0, 1, '', '2026-01-17 21:40:54', NULL, '2026-01-21 16:51:23', 1, 0);
INSERT INTO `sys_dict_data` VALUES (208, 'business_type', '调宿申请', 'transfer', NULL, NULL, 2, 0, 1, NULL, '2026-01-17 21:40:54', NULL, '2026-01-17 21:40:54', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (209, 'business_type', '退宿申请', 'check_out', NULL, NULL, 3, 0, 1, NULL, '2026-01-17 21:40:54', NULL, '2026-01-17 21:40:54', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (210, 'business_type', '留宿申请', 'stay', NULL, NULL, 4, 0, 1, NULL, '2026-01-17 21:40:54', NULL, '2026-01-17 21:40:54', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (211, 'table_button_config', '绑定', 'link', 'bg-info/12 text-info', '', 8, 0, 1, 'ri:link', '2026-01-21 19:25:35', 1, '2026-01-21 20:10:31', 1, 0);
INSERT INTO `sys_dict_data` VALUES (212, 'check_in_type', '长期住宿', '1', NULL, NULL, 1, 0, 1, NULL, '2026-01-28 19:39:05', NULL, '2026-01-28 19:39:05', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (213, 'check_in_type', '临时住宿', '2', NULL, NULL, 2, 0, 1, NULL, '2026-01-28 19:39:05', NULL, '2026-01-28 19:39:05', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (214, 'approval_business_type', '入住申请', 'check_in', NULL, NULL, 1, 0, 1, NULL, '2026-01-28 19:39:07', NULL, '2026-01-28 19:39:07', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (215, 'approval_business_type', '调宿申请', 'transfer', NULL, NULL, 2, 0, 1, NULL, '2026-01-28 19:39:07', NULL, '2026-01-28 19:39:07', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (216, 'approval_business_type', '退宿申请', 'check_out', NULL, NULL, 3, 0, 1, NULL, '2026-01-28 19:39:07', NULL, '2026-01-28 19:39:07', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (217, 'approval_business_type', '留宿申请', 'stay', NULL, NULL, 4, 0, 1, NULL, '2026-01-28 19:39:07', NULL, '2026-01-28 19:39:07', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (218, 'approval_action', '通过', '1', NULL, NULL, 1, 0, 1, NULL, '2026-01-28 19:39:09', NULL, '2026-01-28 19:39:09', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (219, 'approval_action', '拒绝', '2', NULL, NULL, 2, 0, 1, NULL, '2026-01-28 19:39:09', NULL, '2026-01-28 19:39:09', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (220, 'approval_node_type', '串行', '1', NULL, NULL, 1, 0, 1, NULL, '2026-01-28 19:39:10', NULL, '2026-01-28 19:39:10', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (221, 'approval_node_type', '会签', '2', NULL, NULL, 2, 0, 1, NULL, '2026-01-28 19:39:10', NULL, '2026-01-28 19:39:10', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (222, 'approval_node_type', '或签', '3', NULL, NULL, 3, 0, 1, NULL, '2026-01-28 19:39:10', NULL, '2026-01-28 19:39:10', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (223, 'approval_reject_action', '直接结束', '1', NULL, NULL, 1, 0, 1, NULL, '2026-01-28 19:39:11', NULL, '2026-01-28 19:39:11', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (224, 'approval_reject_action', '退回申请人', '2', NULL, NULL, 2, 0, 1, NULL, '2026-01-28 19:39:11', NULL, '2026-01-28 19:39:11', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (225, 'approval_reject_action', '退回上一节点', '3', NULL, NULL, 3, 0, 1, NULL, '2026-01-28 19:39:11', NULL, '2026-01-28 19:39:11', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (226, 'approval_instance_status', '进行中', '1', NULL, NULL, 1, 0, 1, NULL, '2026-01-28 19:39:13', NULL, '2026-01-28 19:39:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (227, 'approval_instance_status', '已通过', '2', NULL, NULL, 2, 0, 1, NULL, '2026-01-28 19:39:13', NULL, '2026-01-28 19:39:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (228, 'approval_instance_status', '已拒绝', '3', NULL, NULL, 3, 0, 1, NULL, '2026-01-28 19:39:13', NULL, '2026-01-28 19:39:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (229, 'approval_instance_status', '已撤回', '4', NULL, NULL, 4, 0, 1, NULL, '2026-01-28 19:39:13', NULL, '2026-01-28 19:39:13', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (230, 'parent_agree_status', '同意', 'agree', NULL, NULL, 1, 0, 1, NULL, '2026-01-28 19:39:14', NULL, '2026-01-28 19:39:14', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (231, 'parent_agree_status', '不同意', 'disagree', NULL, NULL, 2, 0, 1, NULL, '2026-01-28 19:39:14', NULL, '2026-01-28 19:39:14', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (232, 'parent_agree_status', '未填写', '', NULL, NULL, 3, 0, 1, NULL, '2026-01-28 19:39:14', NULL, '2026-01-28 19:39:14', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (258, 'notice_type', '系统通知', '1', NULL, NULL, 1, 0, 1, '系统通知类型', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (259, 'notice_type', '宿舍公告', '2', NULL, NULL, 2, 0, 1, '宿舍公告类型', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (260, 'notice_type', '安全提醒', '3', NULL, NULL, 3, 0, 1, '安全提醒类型', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (261, 'notice_type', '停水停电', '4', NULL, NULL, 4, 0, 1, '停水停电类型', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (262, 'notice_type', '其他', '99', NULL, NULL, 99, 0, 1, '其他类型', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (263, 'notice_status', '草稿', '0', NULL, NULL, 0, 0, 1, '草稿状态', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (264, 'notice_status', '已发布', '1', NULL, NULL, 1, 0, 1, '已发布状态', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (301, 'repair_type', '水电', '1', NULL, NULL, 1, 0, 1, '水管、电路等问题', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (302, 'repair_type', '门窗', '2', NULL, NULL, 2, 0, 1, '门锁、窗户等问题', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (303, 'repair_type', '家具', '3', NULL, NULL, 3, 0, 1, '床、桌椅等问题', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (304, 'repair_type', '网络', '4', NULL, NULL, 4, 0, 1, '网络连接问题', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (305, 'repair_type', '其他', '5', NULL, NULL, 5, 0, 1, '其他类型问题', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (306, 'repair_status', '待接单', '1', NULL, NULL, 1, 0, 1, '报修已提交，等待维修人员接单', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (307, 'repair_status', '已接单', '2', NULL, NULL, 2, 0, 1, '维修人员已接单', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (308, 'repair_status', '维修中', '3', NULL, NULL, 3, 0, 1, '维修人员正在维修', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (309, 'repair_status', '已完成', '4', NULL, NULL, 4, 0, 1, '维修已完成', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (310, 'repair_status', '已取消', '5', NULL, NULL, 5, 0, 1, '报修已取消', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (311, 'repair_urgent_level', '一般', '1', NULL, NULL, 1, 0, 1, '一般问题，可等待处理', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (312, 'repair_urgent_level', '紧急', '2', NULL, NULL, 2, 0, 1, '紧急问题，需要尽快处理', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (313, 'repair_urgent_level', '非常紧急', '3', NULL, NULL, 3, 0, 1, '非常紧急，需要立即处理', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (314, 'repair_rating', '1星', '1', NULL, NULL, 1, 0, 1, '非常不满意', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (315, 'repair_rating', '2星', '2', NULL, NULL, 2, 0, 1, '不满意', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (316, 'repair_rating', '3星', '3', NULL, NULL, 3, 0, 1, '一般', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (317, 'repair_rating', '4星', '4', NULL, NULL, 4, 0, 1, '满意', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (318, 'repair_rating', '5星', '5', NULL, NULL, 5, 0, 1, '非常满意', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (319, 'table_button_config', '复制', 'copy', 'bg-theme/12 text-theme', '', 9, 0, 1, 'ri:file-copy-2-line', '2026-02-03 11:08:29', 1, '2026-02-03 11:08:46', 1, 0);
INSERT INTO `sys_dict_data` VALUES (320, 'student_study_environment', '不需要安静', '0', NULL, NULL, 0, 0, 1, NULL, '2026-02-03 17:14:37', NULL, '2026-02-03 17:14:37', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (321, 'sys_common_status', '正常', '1', NULL, 'success', 1, 1, 1, '正常/启用状态', '2026-02-03 20:00:00', NULL, '2026-02-03 20:00:00', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (322, 'sys_common_status', '停用', '0', NULL, 'danger', 2, 0, 1, '停用/禁用状态', '2026-02-03 20:00:00', NULL, '2026-02-03 20:00:00', NULL, 0);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `dict_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-停用 1-正常',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_code`(`dict_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 73 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', 1, '用户性别字典', '2025-12-31 14:42:00', NULL, '2025-12-31 20:37:55', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (2, '用户状态', 'sys_user_status', 1, '用户状态字典', '2025-12-31 14:42:00', NULL, '2025-12-31 14:42:00', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_switch', 1, '系统开关字典', '2025-12-31 14:42:00', NULL, '2025-12-31 14:42:00', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (5, '表格按钮配置', 'table_button_config', 1, '表格操作按钮配置，包含图标、文字、样式等信息', '2026-01-01 07:00:53', NULL, '2026-01-01 07:00:53', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (7, '学位类型', 'degree_type', 1, '学位类型：专科、本科、研究生', '2026-01-01 17:31:02', NULL, '2026-01-01 17:31:02', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (8, '操作日志业务类型', 'sys_oper_business_type', 1, '操作日志业务类型：0其它 1新增 2修改 3删除', '2026-01-01 20:40:02', NULL, '2026-01-01 20:40:02', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (9, '设备类型', 'sys_device_type', 1, '设备类型：1桌面设备 2移动设备 3爬虫/Bot', '2026-01-01 20:40:14', NULL, '2026-01-01 20:40:14', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (10, '用户在线状态', 'sys_user_online_status', 1, '用户在线状态字典', '2026-01-02 07:38:41', NULL, '2026-01-02 07:38:41', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (11, '房间类型', 'dormitory_room_type', 1, '宿舍房间类型：标准间、套间、单人间等', '2026-01-03 22:12:11', NULL, '2026-01-03 22:12:11', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (12, '床位位置', 'dormitory_bed_position', 1, '床位位置：上铺、下铺、左、右等', '2026-01-03 22:12:12', NULL, '2026-01-03 22:12:12', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (13, '房间状态', 'dormitory_room_status', 1, '房间状态：空闲、已满、维修中、已预订', '2026-01-03 22:12:12', NULL, '2026-01-03 22:12:12', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (14, '床位状态', 'dormitory_bed_status', 1, '床位状态：空闲、已占用、维修中、已预订', '2026-01-03 22:12:13', NULL, '2026-01-03 22:12:13', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (15, '房间设施', 'dormitory_room_facility', 1, '房间设施：空调、独立卫生间、阳台等', '2026-01-05 02:33:46', NULL, '2026-01-05 20:14:34', 1, 0);
INSERT INTO `sys_dict_type` VALUES (16, '楼层适用性别', 'dormitory_gender_type', 1, '楼层适用性别：男生宿舍、女生宿舍、混合宿舍', '2026-01-05 17:50:30', NULL, '2026-01-05 17:50:30', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (17, '学籍状态', 'academic_status', 1, '学籍状态：1在读 2休学 3毕业 4退学', '2026-01-06 09:51:14', NULL, '2026-01-06 09:51:14', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (18, '入住状态', 'check_in_status', 1, '入住状态：1待审核 2已通过 3已拒绝 4已入住', '2026-01-06 09:51:15', NULL, '2026-01-06 09:51:15', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (19, '调宿状态', 'transfer_status', 1, '调宿状态：1待审核 2已通过 3已拒绝 4已完成', '2026-01-06 09:51:16', NULL, '2026-01-06 09:51:16', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (20, '退宿状态', 'check_out_status', 1, '退宿状态：1待审核 2已通过 3已拒绝 4已完成', '2026-01-06 09:51:18', NULL, '2026-01-06 09:51:18', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (21, '留宿状态', 'stay_status', 1, '留宿状态：1待审核 2已通过 3已拒绝 4已完成', '2026-01-06 09:51:19', NULL, '2026-01-06 09:51:19', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (22, '吸烟状态', 'student_smoking_status', 1, '学生是否吸烟：0不吸烟 1吸烟', '2026-01-10 19:50:17', NULL, '2026-01-10 19:50:17', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (23, '是否接受室友吸烟', 'student_smoking_tolerance', 1, '是否接受室友吸烟：0不接受 1接受', '2026-01-10 19:50:17', NULL, '2026-01-10 19:50:17', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (24, '作息时间', 'student_sleep_schedule', 1, '作息时间：0早睡早起 1正常 2晚睡晚起 3夜猫子', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (25, '睡眠质量', 'student_sleep_quality', 1, '睡眠质量：0浅睡易醒 1正常 2深睡', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (26, '是否打呼噜', 'student_snores', 1, '是否打呼噜：0不打 1打', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (27, '是否对光线敏感', 'student_sensitive_to_light', 1, '是否对光线敏感：0不敏感 1敏感', '2026-01-10 19:50:18', NULL, '2026-01-10 19:50:18', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (28, '是否对声音敏感', 'student_sensitive_to_sound', 1, '是否对声音敏感：0不敏感 1敏感', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (29, '整洁程度', 'student_cleanliness_level', 1, '整洁程度：1非常整洁 2整洁 3一般 4随意 5不整洁', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (30, '睡前是否整理', 'student_bedtime_cleanup', 1, '睡前是否整理：0不整理 1偶尔整理 2经常整理 3总是整理', '2026-01-10 19:50:19', NULL, '2026-01-10 19:50:19', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (31, '社交偏好', 'student_social_preference', 1, '社交偏好：1喜欢安静 2中等 3喜欢热闹', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (32, '是否允许室友带访客', 'student_allow_visitors', 1, '是否允许室友带访客：0不允许 1偶尔可以 2可以', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (33, '电话时间偏好', 'student_phone_call_time', 1, '电话时间偏好：0喜欢在宿舍打电话 1偶尔在宿舍 2不在宿舍打电话', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (34, '是否在宿舍学习', 'student_study_in_room', 1, '是否在宿舍学习：0不在 1偶尔 2经常 3总是', '2026-01-10 19:50:20', NULL, '2026-01-10 19:50:20', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (35, '学习环境偏好', 'student_study_environment', 1, '学习环境偏好：1需要安静 2需要轻音乐 3可以接受声音', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (36, '电脑使用时间', 'student_computer_usage_time', 1, '电脑使用时间：0不用 1很少 2正常 3很多', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (37, '游戏偏好', 'student_gaming_preference', 1, '游戏偏好：0不玩游戏 1偶尔玩 2经常玩', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (38, '听音乐偏好', 'student_music_preference', 1, '听音乐偏好：0不听 1偶尔听 2经常听', '2026-01-10 19:50:21', NULL, '2026-01-10 19:50:21', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (39, '音乐音量偏好', 'student_music_volume', 1, '音乐音量偏好：1喜欢小声 2中等 3喜欢大声', '2026-01-10 19:50:22', NULL, '2026-01-10 19:50:22', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (40, '是否在宿舍吃东西', 'student_eat_in_room', 1, '是否在宿舍吃东西：0不吃 1偶尔 2经常', '2026-01-10 19:50:22', NULL, '2026-01-10 19:50:22', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (41, '民族', 'student_nation', 1, '学生民族：包含中国56个民族', '2026-01-10 21:57:37', NULL, '2026-01-10 21:57:37', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (42, '政治面貌', 'student_political_status', 1, '学生政治面貌：群众、共青团员、中共党员等', '2026-01-10 21:57:39', NULL, '2026-01-10 21:57:39', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (43, '审批业务类型', 'business_type', 1, '审批流程关联的业务类型', '2026-01-17 21:40:54', NULL, '2026-01-21 16:51:20', 1, 0);
INSERT INTO `sys_dict_type` VALUES (44, '入住类型', 'check_in_type', 1, '入住申请的类型（长期/临时）', '2026-01-28 19:39:04', NULL, '2026-01-28 19:39:04', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (45, '审批业务类型', 'approval_business_type', 1, '审批流程关联的业务类型', '2026-01-28 19:39:06', NULL, '2026-01-28 19:39:06', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (46, '审批动作', 'approval_action', 1, '审批操作的动作类型（通过/拒绝）', '2026-01-28 19:39:08', NULL, '2026-01-28 19:39:08', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (47, '流程节点类型', 'approval_node_type', 1, '审批流程节点的类型（串行/会签/或签）', '2026-01-28 19:39:09', NULL, '2026-01-28 19:39:09', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (48, '拒绝处理方式', 'approval_reject_action', 1, '审批拒绝后的处理方式', '2026-01-28 19:39:11', NULL, '2026-01-28 19:39:11', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (49, '审批实例状态', 'approval_instance_status', 1, '审批流程实例的状态', '2026-01-28 19:39:12', NULL, '2026-01-28 19:39:12', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (50, '家长同意状态', 'parent_agree_status', 1, '留宿申请中家长是否同意', '2026-01-28 19:39:13', NULL, '2026-01-28 19:39:13', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (57, '通知类型', 'notice_type', 1, '通知公告的类型', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (58, '通知状态', 'notice_status', 1, '通知公告的状态', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (68, '维修类型', 'repair_type', 1, '报修的维修类型', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (69, '报修状态', 'repair_status', 1, '报修单的状态：1-待接单 2-已接单 3-维修中 4-已完成 5-已取消', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (70, '紧急程度', 'repair_urgent_level', 1, '报修的紧急程度：1-一般 2-紧急 3-非常紧急', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (71, '维修评分', 'repair_rating', 1, '报修完成后的评分：1-5星', '2026-01-29 19:33:18', NULL, '2026-01-29 19:33:18', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (72, '系统状态', 'sys_common_status', 1, '通用的启用/停用状态，用于系统各模块', '2026-02-03 20:00:00', NULL, '2026-02-03 20:00:00', NULL, 0);

-- ----------------------------
-- Table structure for sys_floor
-- ----------------------------
DROP TABLE IF EXISTS `sys_floor`;
CREATE TABLE `sys_floor`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `floor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '楼层编码（如：F1、F2）',
  `floor_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼层名称（如：1楼、2楼）',
  `floor_number` int NOT NULL COMMENT '楼层数（数字，如：1、2、3）',
  `campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属校区编码（关联sys_campus）',
  `gender_type` tinyint NOT NULL DEFAULT 3 COMMENT '性别限制：1-男生 2-女生 3-不限',
  `total_rooms` int NOT NULL DEFAULT 0 COMMENT '该楼层房间数（统计字段）',
  `total_beds` int NOT NULL DEFAULT 0 COMMENT '该楼层床位数（统计字段）',
  `current_occupancy` int NOT NULL DEFAULT 0 COMMENT '当前入住人数（统计字段）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_floor_code`(`floor_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_campus_code`(`campus_code` ASC) USING BTREE,
  INDEX `idx_floor_code`(`floor_code` ASC) USING BTREE,
  INDEX `idx_campus`(`campus_code` ASC) USING BTREE,
  INDEX `idx_gender`(`gender_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '楼层表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_floor
-- ----------------------------
INSERT INTO `sys_floor` VALUES (1, 'F1', '1号楼', 6, 'CAMPUS001', 3, 30, 180, 0, 1, 1, '学生宿舍楼，共6层', 0, '2026-02-03 21:36:50', 1, '2026-02-03 21:36:50', 1);
INSERT INTO `sys_floor` VALUES (2, 'F2', '2号楼', 5, 'CAMPUS001', 1, 25, 100, 0, 2, 1, '男生宿舍楼', 0, '2026-02-03 21:37:30', 1, '2026-02-03 21:37:30', 1);
INSERT INTO `sys_floor` VALUES (3, 'F3', '3号楼', 5, 'CAMPUS001', 2, 25, 100, 0, 3, 1, '女生宿舍', 0, '2026-02-03 21:37:54', 1, '2026-02-03 21:37:54', 1);

-- ----------------------------
-- Table structure for sys_lifestyle_survey
-- ----------------------------
DROP TABLE IF EXISTS `sys_lifestyle_survey`;
CREATE TABLE `sys_lifestyle_survey`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `survey_status` tinyint NULL DEFAULT 0 COMMENT '状态：0-未填写 1-已填写 2-已锁定（分配后）',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '首次提交时间',
  `last_update_time` datetime NULL DEFAULT NULL COMMENT '最后修改时间',
  `lock_time` datetime NULL DEFAULT NULL COMMENT '锁定时间（分配确认后锁定）',
  `survey_version` int NULL DEFAULT 1 COMMENT '问卷版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_survey_status`(`survey_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '生活习惯问卷状态表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_lifestyle_survey
-- ----------------------------

-- ----------------------------
-- Table structure for sys_major
-- ----------------------------
DROP TABLE IF EXISTS `sys_major`;
CREATE TABLE `sys_major`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `major_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '专业编码',
  `major_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '专业名称',
  `dept_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属院系编码',
  `director` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业负责人',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学位类型（字典degree_type）',
  `duration` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学制',
  `goal` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '培养目标',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_major_code`(`major_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_dept_code`(`dept_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_department`(`dept_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '专业表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_major
-- ----------------------------
INSERT INTO `sys_major` VALUES (1, 'CS-SE001', '软件工程', 'CS001', '王五', 'bachelor', '4年制', '培养具有扎实的软件工程理论基础和实践能力的高级专门人才', 1, '2025-12-31 12:51:14', NULL, '2026-01-06 15:32:01', 1, 0);

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单类型：M-目录 C-菜单 B-按钮',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `visible` tinyint NULL DEFAULT 1 COMMENT '是否可见：0-隐藏 1-显示',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-停用 1-显示',
  `keep_alive` tinyint NULL DEFAULT 1 COMMENT '是否缓存：0-否 1-是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 178 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '工作台', 'M', '/dashboard', '/index/index', '', 'ri:pie-chart-line', 1, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (2, 1, '控制台', 'C', 'console', '/dashboard/console', 'dashboard:console:view', '', 1, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2026-01-21 16:52:13', 1, 0);
INSERT INTO `sys_menu` VALUES (3, 0, '系统管理', 'M', '/system', '/index/index', '', 'ri:user-3-line', 11, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (4, 3, '用户管理', 'C', 'user', '/system/user', 'system:user:view', NULL, 1, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2025-12-30 11:16:38', NULL, 0);
INSERT INTO `sys_menu` VALUES (5, 3, '角色管理', 'C', 'role', '/system/role', 'system:role:view', NULL, 2, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2025-12-30 11:16:38', NULL, 0);
INSERT INTO `sys_menu` VALUES (6, 3, '菜单管理', 'C', 'menu', '/system/menu', 'system:menu:view', NULL, 3, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2025-12-30 11:16:38', NULL, 0);
INSERT INTO `sys_menu` VALUES (7, 3, '个人中心', 'C', 'user-center', '/system/user-center', 'system:user-center:view', '', 5, 0, 1, 1, '2025-12-30 11:16:38', NULL, '2025-12-31 14:44:02', NULL, 0);
INSERT INTO `sys_menu` VALUES (8, 0, '异常页面', 'M', '/exception', '/index/index', '', 'ri:error-warning-line', 12, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2026-02-06 15:23:04', 1, 0);
INSERT INTO `sys_menu` VALUES (9, 8, '403', 'C', '403', '/exception/403', 'exception:403:view', '', 1, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2026-02-06 15:23:07', 1, 0);
INSERT INTO `sys_menu` VALUES (10, 8, '404', 'C', '404', '/exception/404', 'exception:404:view', '', 2, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2026-02-06 15:23:10', 1, 0);
INSERT INTO `sys_menu` VALUES (11, 8, '500', 'C', '500', '/exception/500', 'exception:500:view', '', 3, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2026-02-06 15:23:13', 1, 0);
INSERT INTO `sys_menu` VALUES (12, 0, '结果页面', 'M', '/result', '/index/index', '', 'ri:checkbox-circle-line', 13, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (13, 12, '成功页', 'C', 'success', '/result/success', 'result:success:view', NULL, 1, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2025-12-30 11:16:38', NULL, 0);
INSERT INTO `sys_menu` VALUES (14, 12, '失败页', 'C', 'fail', '/result/fail', 'result:fail:view', NULL, 2, 1, 1, 1, '2025-12-30 11:16:38', NULL, '2025-12-30 11:16:38', NULL, 0);
INSERT INTO `sys_menu` VALUES (15, 3, '字典管理', 'C', 'dict', '/system/dict', 'system:dict:view', NULL, 4, 1, 1, 1, '2025-12-31 14:42:15', NULL, '2025-12-31 14:42:15', NULL, 0);
INSERT INTO `sys_menu` VALUES (16, 4, '新增用户', 'F', NULL, NULL, 'system:user:add', NULL, 1, 1, 1, 1, '2025-12-31 09:11:03', NULL, '2025-12-31 09:11:03', NULL, 0);
INSERT INTO `sys_menu` VALUES (17, 4, '编辑用户', 'F', NULL, NULL, 'system:user:edit', NULL, 2, 1, 1, 1, '2025-12-31 09:11:03', NULL, '2025-12-31 09:11:03', NULL, 0);
INSERT INTO `sys_menu` VALUES (18, 4, '删除用户', 'F', NULL, NULL, 'system:user:delete', NULL, 3, 1, 1, 1, '2025-12-31 09:11:03', NULL, '2025-12-31 09:11:03', NULL, 0);
INSERT INTO `sys_menu` VALUES (19, 4, '重置密码', 'F', NULL, NULL, 'system:user:reset-pwd', NULL, 4, 1, 1, 1, '2025-12-31 09:11:03', NULL, '2025-12-31 09:11:03', NULL, 0);
INSERT INTO `sys_menu` VALUES (37, 5, '新增角色', 'F', NULL, NULL, 'system:role:add', NULL, 1, 1, 1, 1, '2025-12-31 17:11:56', NULL, '2025-12-31 17:11:56', NULL, 0);
INSERT INTO `sys_menu` VALUES (38, 5, '编辑角色', 'F', NULL, NULL, 'system:role:edit', NULL, 2, 1, 1, 1, '2025-12-31 17:11:56', NULL, '2025-12-31 17:11:56', NULL, 0);
INSERT INTO `sys_menu` VALUES (39, 5, '删除角色', 'F', NULL, NULL, 'system:role:delete', NULL, 3, 1, 1, 1, '2025-12-31 17:11:56', NULL, '2025-12-31 17:11:56', NULL, 0);
INSERT INTO `sys_menu` VALUES (40, 5, '分配权限', 'F', NULL, NULL, 'system:role:assign', NULL, 4, 1, 1, 1, '2025-12-31 17:11:56', NULL, '2025-12-31 17:11:56', NULL, 0);
INSERT INTO `sys_menu` VALUES (41, 6, '新增菜单', 'F', NULL, NULL, 'system:menu:add', NULL, 1, 1, 1, 1, '2025-12-31 17:12:03', NULL, '2025-12-31 17:12:03', NULL, 0);
INSERT INTO `sys_menu` VALUES (42, 6, '编辑菜单', 'F', NULL, NULL, 'system:menu:edit', NULL, 2, 1, 1, 1, '2025-12-31 17:12:03', NULL, '2025-12-31 17:12:03', NULL, 0);
INSERT INTO `sys_menu` VALUES (43, 6, '删除菜单', 'F', NULL, NULL, 'system:menu:delete', NULL, 3, 1, 1, 1, '2025-12-31 17:12:03', NULL, '2025-12-31 17:12:03', NULL, 0);
INSERT INTO `sys_menu` VALUES (44, 15, '新增字典类型', 'F', NULL, NULL, 'system:dict:type:add', NULL, 1, 1, 1, 1, '2025-12-31 17:12:12', NULL, '2025-12-31 17:12:12', NULL, 0);
INSERT INTO `sys_menu` VALUES (45, 15, '编辑字典类型', 'F', NULL, NULL, 'system:dict:type:edit', NULL, 2, 1, 1, 1, '2025-12-31 17:12:12', NULL, '2025-12-31 17:12:12', NULL, 0);
INSERT INTO `sys_menu` VALUES (46, 15, '删除字典类型', 'F', NULL, NULL, 'system:dict:type:delete', NULL, 3, 1, 1, 1, '2025-12-31 17:12:12', NULL, '2025-12-31 17:12:12', NULL, 0);
INSERT INTO `sys_menu` VALUES (47, 15, '新增字典数据', 'F', NULL, NULL, 'system:dict:data:add', NULL, 4, 1, 1, 1, '2025-12-31 17:12:12', NULL, '2025-12-31 17:12:12', NULL, 0);
INSERT INTO `sys_menu` VALUES (48, 15, '编辑字典数据', 'F', NULL, NULL, 'system:dict:data:edit', NULL, 5, 1, 1, 1, '2025-12-31 17:12:12', NULL, '2025-12-31 17:12:12', NULL, 0);
INSERT INTO `sys_menu` VALUES (49, 15, '删除字典数据', 'F', NULL, NULL, 'system:dict:data:delete', NULL, 6, 1, 1, 1, '2025-12-31 17:12:12', NULL, '2025-12-31 17:12:12', NULL, 0);
INSERT INTO `sys_menu` VALUES (50, 0, '学校管理', 'M', '/school', '/index/index', '', 'ri:school-line', 9, 1, 1, 1, '2025-12-31 20:01:27', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (55, 67, '新增校区', 'F', NULL, NULL, 'system:campus:add', NULL, 1, 1, 1, 1, '2025-12-31 20:01:27', NULL, '2026-01-01 16:08:18', NULL, 0);
INSERT INTO `sys_menu` VALUES (56, 67, '编辑校区', 'F', NULL, NULL, 'system:campus:edit', NULL, 2, 1, 1, 1, '2025-12-31 20:01:27', NULL, '2026-01-01 16:08:20', NULL, 0);
INSERT INTO `sys_menu` VALUES (57, 67, '删除校区', 'F', NULL, NULL, 'system:campus:delete', NULL, 3, 1, 1, 1, '2025-12-31 20:01:27', NULL, '2026-01-01 16:08:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (58, 68, '新增院系', 'F', NULL, NULL, 'system:department:add', NULL, 1, 1, 1, 1, '2025-12-31 20:01:27', NULL, '2026-01-01 16:08:35', NULL, 0);
INSERT INTO `sys_menu` VALUES (59, 68, '编辑院系', 'F', NULL, NULL, 'system:department:edit', NULL, 2, 1, 1, 1, '2025-12-31 20:01:28', NULL, '2026-01-01 16:08:37', NULL, 0);
INSERT INTO `sys_menu` VALUES (60, 68, '删除院系', 'F', NULL, NULL, 'system:department:delete', NULL, 3, 1, 1, 1, '2025-12-31 20:01:28', NULL, '2026-01-01 16:08:40', NULL, 0);
INSERT INTO `sys_menu` VALUES (61, 69, '新增专业', 'F', NULL, NULL, 'system:major:add', NULL, 1, 1, 1, 1, '2025-12-31 20:01:28', NULL, '2026-01-01 16:08:45', NULL, 0);
INSERT INTO `sys_menu` VALUES (62, 69, '编辑专业', 'F', NULL, NULL, 'system:major:edit', NULL, 2, 1, 1, 1, '2025-12-31 20:01:28', NULL, '2026-01-01 16:08:47', NULL, 0);
INSERT INTO `sys_menu` VALUES (63, 69, '删除专业', 'F', NULL, NULL, 'system:major:delete', NULL, 3, 1, 1, 1, '2025-12-31 20:01:28', NULL, '2026-01-01 16:08:49', NULL, 0);
INSERT INTO `sys_menu` VALUES (64, 70, '新增班级', 'F', NULL, NULL, 'system:class:add', NULL, 1, 1, 1, 1, '2025-12-31 20:01:28', NULL, '2026-01-01 16:08:56', NULL, 0);
INSERT INTO `sys_menu` VALUES (65, 70, '编辑班级', 'F', NULL, NULL, 'system:class:edit', NULL, 2, 1, 1, 1, '2025-12-31 20:01:28', NULL, '2026-01-01 16:08:57', NULL, 0);
INSERT INTO `sys_menu` VALUES (66, 70, '删除班级', 'F', NULL, NULL, 'system:class:delete', NULL, 3, 1, 1, 1, '2025-12-31 20:01:28', NULL, '2026-01-01 16:09:07', NULL, 0);
INSERT INTO `sys_menu` VALUES (67, 50, '校区管理', 'C', 'campus', '/school/campus', NULL, '', 1, 1, 1, 1, '2025-12-31 20:25:36', NULL, '2025-12-31 20:29:28', NULL, 0);
INSERT INTO `sys_menu` VALUES (68, 88, '院系管理', 'C', 'department', '/organization/department', NULL, '', 1, 1, 1, 1, '2025-12-31 20:25:36', NULL, '2026-01-03 12:46:08', NULL, 0);
INSERT INTO `sys_menu` VALUES (69, 88, '专业管理', 'C', 'major', '/organization/major', NULL, '', 2, 1, 1, 1, '2025-12-31 20:25:36', NULL, '2026-01-03 12:46:19', NULL, 0);
INSERT INTO `sys_menu` VALUES (70, 88, '班级管理', 'C', 'class', '/organization/class', NULL, '', 3, 1, 1, 1, '2025-12-31 20:25:36', NULL, '2026-01-03 12:46:22', NULL, 0);
INSERT INTO `sys_menu` VALUES (83, 3, '操作日志', 'C', 'oper-log', '/system/oper-log', 'system:operlog:view', '', 6, 1, 1, 1, '2026-01-01 19:53:03', NULL, '2026-01-01 19:54:30', 1, 0);
INSERT INTO `sys_menu` VALUES (84, 83, '查看详情', 'F', NULL, NULL, 'system:operlog:detail', NULL, 1, 1, 1, 1, '2026-01-01 19:53:03', NULL, '2026-01-01 19:53:03', NULL, 0);
INSERT INTO `sys_menu` VALUES (85, 83, '删除日志', 'F', NULL, NULL, 'system:operlog:delete', NULL, 2, 1, 1, 1, '2026-01-01 19:53:03', NULL, '2026-01-01 19:53:03', NULL, 0);
INSERT INTO `sys_menu` VALUES (86, 83, '清空日志', 'F', NULL, NULL, 'system:operlog:clean', NULL, 3, 1, 1, 1, '2026-01-01 19:53:03', NULL, '2026-01-01 19:53:03', NULL, 0);
INSERT INTO `sys_menu` VALUES (87, 4, '分配管理', 'F', '', '', 'system:user:assign-scope', '', 5, 1, 1, 1, '2026-01-02 17:57:21', 1, '2026-01-02 17:57:21', 1, 0);
INSERT INTO `sys_menu` VALUES (88, 0, '组织管理', 'M', '/organization', '/index/index', NULL, 'ri:organization-chart', 10, 1, 1, 1, '2026-01-03 12:45:25', NULL, '2026-02-03 23:16:47', NULL, 0);
INSERT INTO `sys_menu` VALUES (89, 50, '学年管理', 'C', 'academic-year', '/school/academic-year', NULL, '', 2, 1, 1, 1, '2026-01-03 12:45:29', NULL, '2026-01-03 20:56:04', NULL, 0);
INSERT INTO `sys_menu` VALUES (90, 0, '宿舍管理', 'M', '/dormitory', '/index/index', '', 'ri:building-line', 4, 1, 1, 1, '2026-01-03 12:45:30', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (94, 89, '新增学年', 'F', NULL, NULL, 'system:academic-year:add', NULL, 1, 1, 1, 1, '2026-01-03 20:51:45', NULL, '2026-01-03 20:51:45', NULL, 0);
INSERT INTO `sys_menu` VALUES (95, 89, '编辑学年', 'F', NULL, NULL, 'system:academic-year:edit', NULL, 2, 1, 1, 1, '2026-01-03 20:51:45', NULL, '2026-01-03 20:51:45', NULL, 0);
INSERT INTO `sys_menu` VALUES (96, 89, '删除学年', 'F', NULL, NULL, 'system:academic-year:delete', NULL, 3, 1, 1, 1, '2026-01-03 20:51:45', NULL, '2026-01-03 20:51:45', NULL, 0);
INSERT INTO `sys_menu` VALUES (97, 90, '楼层管理', 'C', 'floor', '/dormitory/floor', NULL, '', 1, 1, 1, 1, '2026-01-03 20:51:45', NULL, '2026-01-03 20:55:44', NULL, 0);
INSERT INTO `sys_menu` VALUES (98, 90, '房间管理', 'C', 'room', '/dormitory/room', NULL, '', 2, 1, 1, 1, '2026-01-03 20:51:45', NULL, '2026-01-03 20:55:41', NULL, 0);
INSERT INTO `sys_menu` VALUES (99, 90, '床位管理', 'C', 'bed', '/dormitory/bed', NULL, '', 3, 1, 1, 1, '2026-01-03 20:51:45', NULL, '2026-01-03 20:55:30', NULL, 0);
INSERT INTO `sys_menu` VALUES (100, 97, '新增楼层', 'F', NULL, NULL, 'system:floor:add', NULL, 1, 1, 1, 1, '2026-01-03 22:12:25', NULL, '2026-01-03 22:12:25', NULL, 0);
INSERT INTO `sys_menu` VALUES (101, 97, '编辑楼层', 'F', NULL, NULL, 'system:floor:edit', NULL, 2, 1, 1, 1, '2026-01-03 22:12:25', NULL, '2026-01-03 22:12:25', NULL, 0);
INSERT INTO `sys_menu` VALUES (102, 97, '删除楼层', 'F', NULL, NULL, 'system:floor:delete', NULL, 3, 1, 1, 1, '2026-01-03 22:12:25', NULL, '2026-01-03 22:12:25', NULL, 0);
INSERT INTO `sys_menu` VALUES (103, 97, '批量删除', 'F', NULL, NULL, 'system:floor:batchDelete', NULL, 4, 1, 1, 1, '2026-01-03 22:12:25', NULL, '2026-01-03 22:12:25', NULL, 0);
INSERT INTO `sys_menu` VALUES (104, 97, '状态切换', 'F', NULL, NULL, 'system:floor:status', NULL, 5, 1, 1, 1, '2026-01-03 22:12:25', NULL, '2026-01-03 22:12:25', NULL, 0);
INSERT INTO `sys_menu` VALUES (105, 97, '导出', 'F', NULL, NULL, 'system:floor:export', NULL, 6, 1, 1, 1, '2026-01-03 22:12:25', NULL, '2026-01-03 22:12:25', NULL, 0);
INSERT INTO `sys_menu` VALUES (106, 98, '新增房间', 'F', NULL, NULL, 'system:room:add', NULL, 1, 1, 1, 1, '2026-01-03 22:12:25', NULL, '2026-01-03 22:12:25', NULL, 0);
INSERT INTO `sys_menu` VALUES (107, 98, '编辑房间', 'F', NULL, NULL, 'system:room:edit', NULL, 2, 1, 1, 1, '2026-01-03 22:12:25', NULL, '2026-01-03 22:12:25', NULL, 0);
INSERT INTO `sys_menu` VALUES (108, 98, '删除房间', 'F', NULL, NULL, 'system:room:delete', NULL, 3, 1, 1, 1, '2026-01-03 22:12:25', NULL, '2026-01-03 22:12:25', NULL, 0);
INSERT INTO `sys_menu` VALUES (109, 98, '批量删除', 'F', NULL, NULL, 'system:room:batchDelete', NULL, 4, 1, 1, 1, '2026-01-03 22:12:25', NULL, '2026-01-03 22:12:25', NULL, 0);
INSERT INTO `sys_menu` VALUES (110, 98, '状态切换', 'F', NULL, NULL, 'system:room:status', NULL, 5, 1, 1, 1, '2026-01-03 22:12:26', NULL, '2026-01-03 22:12:26', NULL, 0);
INSERT INTO `sys_menu` VALUES (111, 98, '导出', 'F', NULL, NULL, 'system:room:export', NULL, 6, 1, 1, 1, '2026-01-03 22:12:26', NULL, '2026-01-03 22:12:26', NULL, 0);
INSERT INTO `sys_menu` VALUES (112, 99, '新增床位', 'F', NULL, NULL, 'system:bed:add', NULL, 1, 1, 1, 1, '2026-01-03 22:12:26', NULL, '2026-01-03 22:12:26', NULL, 0);
INSERT INTO `sys_menu` VALUES (113, 99, '编辑床位', 'F', NULL, NULL, 'system:bed:edit', NULL, 2, 1, 1, 1, '2026-01-03 22:12:26', NULL, '2026-01-03 22:12:26', NULL, 0);
INSERT INTO `sys_menu` VALUES (114, 99, '删除床位', 'F', NULL, NULL, 'system:bed:delete', NULL, 3, 1, 1, 1, '2026-01-03 22:12:26', NULL, '2026-01-03 22:12:26', NULL, 0);
INSERT INTO `sys_menu` VALUES (115, 99, '批量删除', 'F', NULL, NULL, 'system:bed:batchDelete', NULL, 4, 1, 1, 1, '2026-01-03 22:12:26', NULL, '2026-01-03 22:12:26', NULL, 0);
INSERT INTO `sys_menu` VALUES (116, 99, '状态切换', 'F', NULL, NULL, 'system:bed:status', NULL, 5, 1, 1, 1, '2026-01-03 22:12:26', NULL, '2026-01-03 22:12:26', NULL, 0);
INSERT INTO `sys_menu` VALUES (117, 99, '导出', 'F', NULL, NULL, 'system:bed:export', NULL, 6, 1, 1, 1, '2026-01-03 22:12:26', NULL, '2026-01-03 22:12:26', NULL, 0);
INSERT INTO `sys_menu` VALUES (118, 97, '批量增加房间', 'F', NULL, NULL, 'system:floor:batchAdd', NULL, 7, 1, 1, 1, '2026-01-06 04:01:38', NULL, '2026-01-06 04:01:38', NULL, 0);
INSERT INTO `sys_menu` VALUES (119, 98, '批量增加床位', 'F', NULL, NULL, 'system:room:batchAdd', NULL, 7, 1, 1, 1, '2026-01-06 04:01:39', NULL, '2026-01-06 04:01:39', NULL, 0);
INSERT INTO `sys_menu` VALUES (124, 4, '分配权限', 'F', '', '', 'system:user:assign-permission', '', 6, 1, 1, 1, '2026-01-06 14:29:02', 1, '2026-01-06 14:29:08', 1, 0);
INSERT INTO `sys_menu` VALUES (125, 0, '住宿管理', 'M', '/accommodation', '/index/index', '', 'ri:community-line', 2, 1, 1, 1, '2026-01-06 17:55:18', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (126, 0, '学生管理', 'M', '/student', '/index/index', '', 'ri:user-follow-line', 3, 1, 1, 1, '2026-01-06 17:55:18', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (127, 177, '新增学生', 'F', '', '', 'student:add', '', 1, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-02-03 23:16:47', NULL, 0);
INSERT INTO `sys_menu` VALUES (128, 177, '编辑学生', 'F', '', '', 'student:edit', '', 2, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-02-03 23:16:47', NULL, 0);
INSERT INTO `sys_menu` VALUES (129, 177, '删除学生', 'F', '', '', 'student:delete', '', 3, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-02-03 23:16:47', NULL, 0);
INSERT INTO `sys_menu` VALUES (130, 125, '入住管理', 'C', 'check-in', '/accommodation/check-in', '', '', 1, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (131, 130, '新增入住', 'F', '', '', 'system:checkIn:add', '', 1, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-01-06 17:55:19', NULL, 0);
INSERT INTO `sys_menu` VALUES (132, 130, '删除入住', 'F', '', '', 'system:checkIn:delete', '', 2, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-01-06 17:55:19', NULL, 0);
INSERT INTO `sys_menu` VALUES (133, 125, '调宿管理', 'C', 'transfer', '/accommodation/transfer', '', '', 2, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (134, 133, '新增调宿', 'F', '', '', 'system:transfer:add', '', 1, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-01-06 17:55:19', NULL, 0);
INSERT INTO `sys_menu` VALUES (135, 133, '删除调宿', 'F', '', '', 'system:transfer:delete', '', 2, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-01-06 17:55:19', NULL, 0);
INSERT INTO `sys_menu` VALUES (136, 125, '退宿管理', 'C', 'check-out', '/accommodation/check-out', '', '', 3, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (137, 136, '新增退宿', 'F', '', '', 'system:checkOut:add', '', 1, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-01-06 17:55:19', NULL, 0);
INSERT INTO `sys_menu` VALUES (138, 136, '删除退宿', 'F', '', '', 'system:checkOut:delete', '', 2, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-01-06 17:55:19', NULL, 0);
INSERT INTO `sys_menu` VALUES (139, 125, '留宿管理', 'C', 'stay', '/accommodation/stay', '', '', 4, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (140, 139, '新增留宿', 'F', '', '', 'system:stay:add', '', 1, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-01-06 17:55:19', NULL, 0);
INSERT INTO `sys_menu` VALUES (141, 139, '删除留宿', 'F', '', '', 'system:stay:delete', '', 2, 1, 1, 1, '2026-01-06 17:55:19', NULL, '2026-01-06 17:55:19', NULL, 0);
INSERT INTO `sys_menu` VALUES (142, 0, '审批管理', 'M', '/approval', '/index/index', '', 'ri:flow-chart', 8, 1, 1, 1, '2026-01-17 16:30:53', NULL, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (143, 142, '流程配置', 'C', 'flow-config', '/approval/flow-config', NULL, '', 1, 1, 1, 1, '2026-01-17 16:30:53', NULL, '2026-01-17 16:35:12', NULL, 0);
INSERT INTO `sys_menu` VALUES (144, 142, '待办审批', 'C', 'pending', '/approval/pending', NULL, '', 2, 1, 1, 1, '2026-01-17 16:30:53', NULL, '2026-01-17 16:35:15', NULL, 0);
INSERT INTO `sys_menu` VALUES (145, 142, '审批记录', 'C', 'history', '/approval/history', NULL, '', 3, 1, 1, 1, '2026-01-17 16:30:53', NULL, '2026-01-17 16:35:18', NULL, 0);
INSERT INTO `sys_menu` VALUES (146, 0, '通知管理', 'M', '/notice', '/index/index', '', 'ri:notification-3-line', 7, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-02-03 23:16:47', NULL, 0);
INSERT INTO `sys_menu` VALUES (147, 146, '通知管理', 'C', 'manage', '/notice/manage', 'notice:manage:view', '', 1, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 15:49:55', NULL, 0);
INSERT INTO `sys_menu` VALUES (148, 147, '新增通知', 'F', NULL, NULL, 'system:notice:add', NULL, 1, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 18:12:31', NULL, 0);
INSERT INTO `sys_menu` VALUES (149, 147, '编辑通知', 'F', NULL, NULL, 'system:notice:edit', NULL, 2, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 18:12:33', NULL, 0);
INSERT INTO `sys_menu` VALUES (150, 147, '删除通知', 'F', NULL, NULL, 'system:notice:delete', NULL, 3, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 18:12:35', NULL, 0);
INSERT INTO `sys_menu` VALUES (151, 147, '发布/下架', 'F', NULL, NULL, 'system:notice:publish', NULL, 4, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 18:12:37', NULL, 0);
INSERT INTO `sys_menu` VALUES (152, 0, '报修管理', 'M', '/repair', '/index/index', '', 'ri:tools-line', 5, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-02-03 23:16:47', NULL, 0);
INSERT INTO `sys_menu` VALUES (153, 152, '报修工单', 'C', 'manage', '/repair/manage', 'repair:manage:view', '', 1, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 15:49:55', NULL, 0);
INSERT INTO `sys_menu` VALUES (154, 153, '接单', 'F', NULL, NULL, 'system:repair:accept', NULL, 1, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 18:12:41', NULL, 0);
INSERT INTO `sys_menu` VALUES (155, 153, '完成维修', 'F', NULL, NULL, 'system:repair:complete', NULL, 2, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 18:12:42', NULL, 0);
INSERT INTO `sys_menu` VALUES (156, 153, '删除工单', 'F', NULL, NULL, 'system:repair:delete', NULL, 3, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 18:12:45', NULL, 0);
INSERT INTO `sys_menu` VALUES (157, 0, '智能分配', 'M', '/allocation', '/index/index', '', 'ri:robot-line', 6, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-03 23:16:47', 1, 0);
INSERT INTO `sys_menu` VALUES (158, 157, '分配配置', 'C', 'config', '/allocation/config', 'allocation:config:view', '', 1, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-03 15:05:59', 1, 0);
INSERT INTO `sys_menu` VALUES (159, 158, '新增配置', 'F', NULL, NULL, 'allocation:config:add', NULL, 1, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (160, 158, '编辑配置', 'F', NULL, NULL, 'allocation:config:edit', NULL, 2, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (161, 158, '删除配置', 'F', NULL, NULL, 'allocation:config:delete', NULL, 3, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (162, 157, '分配任务', 'C', 'task', '/allocation/task', 'allocation:task:view', '', 2, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-03 15:06:04', 1, 0);
INSERT INTO `sys_menu` VALUES (163, 162, '创建任务', 'F', NULL, NULL, 'allocation:task:add', NULL, 1, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (164, 162, '执行分配', 'F', NULL, NULL, 'allocation:task:execute', NULL, 2, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (165, 162, '取消任务', 'F', NULL, NULL, 'allocation:task:cancel', NULL, 3, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (166, 162, '删除任务', 'F', NULL, NULL, 'allocation:task:delete', NULL, 4, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (167, 157, '分配结果', 'C', 'result', '/allocation/result', 'allocation:result:view', '', 3, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-03 15:06:08', 1, 0);
INSERT INTO `sys_menu` VALUES (168, 167, '查看详情', 'F', NULL, NULL, 'allocation:result:detail', NULL, 1, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (169, 167, '导出结果', 'F', NULL, NULL, 'allocation:result:export', NULL, 2, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (170, 167, '调整分配', 'F', NULL, NULL, 'allocation:result:adjust', NULL, 3, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (171, 167, '确认分配', 'F', NULL, NULL, 'allocation:result:confirm', NULL, 4, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (172, 157, '问卷管理', 'C', 'survey', '/allocation/survey', 'allocation:survey:view', '', 4, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-03 15:06:12', 1, 0);
INSERT INTO `sys_menu` VALUES (173, 172, '查看问卷', 'F', NULL, NULL, 'allocation:survey:detail', NULL, 1, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (174, 172, '统计分析', 'F', NULL, NULL, 'allocation:survey:statistics', NULL, 2, 1, 1, 1, '2026-02-02 23:03:29', 1, '2026-02-02 23:03:29', NULL, 0);
INSERT INTO `sys_menu` VALUES (177, 126, '学生管理', 'C', 'manage', '/student/manage', 'student:manage:view', '', 1, 1, 1, 1, '2026-02-03 23:16:47', NULL, '2026-02-03 23:16:47', NULL, 0);

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容',
  `notice_type` int NOT NULL COMMENT '通知类型：1-系统通知 2-宿舍公告 3-安全提醒 4-停水停电 99-其他',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
  `attachments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '附件（JSON数组）',
  `publisher_id` bigint NULL DEFAULT NULL COMMENT '发布人ID',
  `publisher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布人姓名',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `is_top` tinyint NULL DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
  `target_floors` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '目标楼层（JSON数组，为空表示全部）',
  `read_count` int NULL DEFAULT 0 COMMENT '阅读次数',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notice_type`(`notice_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE,
  INDEX `idx_is_top`(`is_top` ASC) USING BTREE,
  INDEX `idx_type_status_top`(`notice_type` ASC, `status` ASC, `is_top` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------

-- ----------------------------
-- Table structure for sys_notice_read
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_read`;
CREATE TABLE `sys_notice_read`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notice_id` bigint NOT NULL COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（学生ID）',
  `read_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notice_user`(`notice_id` ASC, `user_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_notice_id`(`notice_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_notice`(`user_id` ASC, `notice_id` ASC) USING BTREE,
  INDEX `idx_notice`(`notice_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知阅读记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice_read
-- ----------------------------

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作模块',
  `business_type` int NULL DEFAULT 0 COMMENT '业务类型：1-新增 2-修改 3-删除 4-查询 5-导出 6-导入 7-其他',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int NULL DEFAULT 0 COMMENT '操作类别：1-后台用户 2-小程序用户',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作人员',
  `device_type` int NULL DEFAULT NULL COMMENT '设备类型：1-PC 2-手机 3-平板',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int NULL DEFAULT 0 COMMENT '操作状态：0-正常 1-异常',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint NULL DEFAULT NULL COMMENT '消耗时间（毫秒）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_oper_time`(`oper_time` ASC) USING BTREE,
  INDEX `idx_business_type`(`business_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_business_time`(`business_type` ASC, `oper_time` ASC) USING BTREE,
  INDEX `idx_oper_name`(`oper_name` ASC) USING BTREE,
  INDEX `idx_status_time`(`status` ASC, `oper_time` ASC) USING BTREE,
  INDEX `idx_title`(`title` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (1, '分配角色权限', 0, 'com.project.backend.system.controller.RoleController.assignPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/role/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,90,97,100,101,102,103,104,105,118,98,106,107,108,109,110,111,119,99,112,113,114,115,116,117,152,153,154,155,156,146,147,148,149,150,151,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,142,143,144,145,50,67,55,56,57,89,94,95,96,88,68,58,59,60,69,61,62,63,70,64,65,66,3,4,16,17,18,19,87,124,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1770044895971}', 0, '', '2026-02-02 23:08:16', 150);
INSERT INTO `sys_oper_log` VALUES (2, '分配用户权限', 0, 'com.project.backend.system.controller.UserController.assignUserPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/user/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,90,97,100,101,102,103,104,105,118,98,106,107,108,109,110,111,119,99,112,113,114,115,116,117,152,153,154,155,156,146,147,148,149,150,151,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,142,143,144,145,50,67,55,56,57,89,94,95,96,88,68,58,59,60,69,61,62,63,70,64,65,66,3,4,16,17,18,19,87,124,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1770044903681}', 0, '', '2026-02-02 23:08:24', 112);
INSERT INTO `sys_oper_log` VALUES (3, '修改状态', 2, 'com.project.backend.allocation.controller.AllocationConfigController.updateStatus()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/allocation/config/1/status/0', '0:0:0:0:0:0:0:1', '', '1 0', '{\"code\":200,\"message\":\"分配配置已停用\",\"data\":null,\"timestamp\":1770047378747}', 0, '', '2026-02-02 23:49:39', 10);
INSERT INTO `sys_oper_log` VALUES (4, '修改状态', 2, 'com.project.backend.allocation.controller.AllocationConfigController.updateStatus()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/allocation/config/1/status/1', '0:0:0:0:0:0:0:1', '', '1 1', '{\"code\":200,\"message\":\"分配配置已启用\",\"data\":null,\"timestamp\":1770047379313}', 0, '', '2026-02-02 23:49:39', 6);
INSERT INTO `sys_oper_log` VALUES (5, '新增字典数据', 1, 'com.project.backend.system.controller.DictDataController.add()', 'POST', 1, 'superAdmin', 1, '/api/v1/system/dict/data', '0:0:0:0:0:0:0:1', '', '{\"id\":null,\"dictCode\":\"table_button_config\",\"label\":\"复制\",\"value\":\"copy\",\"cssClass\":\"\",\"listClass\":\"\",\"sort\":9,\"isDefault\":0,\"status\":1,\"remark\":\"ri:file-copy-2-line\"}', '{\"code\":200,\"message\":\"字典数据新增成功\",\"data\":null,\"timestamp\":1770088109152}', 0, '', '2026-02-03 11:08:29', 25);
INSERT INTO `sys_oper_log` VALUES (6, '编辑字典数据', 2, 'com.project.backend.system.controller.DictDataController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/dict/data/319', '0:0:0:0:0:0:0:1', '', '319 {\"id\":319,\"dictCode\":\"table_button_config\",\"label\":\"复制\",\"value\":\"copy\",\"cssClass\":\"bg-theme/12 text-theme\",\"listClass\":\"\",\"sort\":9,\"isDefault\":0,\"status\":1,\"remark\":\"ri:file-copy-2-line\"}', '{\"code\":200,\"message\":\"字典数据编辑成功\",\"data\":null,\"timestamp\":1770088126485}', 0, '', '2026-02-03 11:08:46', 11);
INSERT INTO `sys_oper_log` VALUES (7, '分配角色权限', 0, 'com.project.backend.system.controller.RoleController.assignPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/role/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,90,97,100,101,102,103,104,105,118,98,106,107,108,109,110,111,119,99,112,113,114,115,116,117,152,153,154,155,156,146,147,148,149,150,151,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,142,143,144,145,50,67,55,56,57,89,94,95,96,88,68,58,59,60,69,61,62,63,70,64,65,66,3,4,16,17,18,19,87,124,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1770100999590}', 0, '', '2026-02-03 14:43:20', 142);
INSERT INTO `sys_oper_log` VALUES (8, '分配用户权限', 0, 'com.project.backend.system.controller.UserController.assignUserPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/user/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,90,97,100,101,102,103,104,105,118,98,106,107,108,109,110,111,119,99,112,113,114,115,116,117,152,153,154,155,156,146,147,148,149,150,151,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,142,143,144,145,50,67,55,56,57,89,94,95,96,88,68,58,59,60,69,61,62,63,70,64,65,66,3,4,16,17,18,19,87,124,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1770101009117}', 0, '', '2026-02-03 14:43:29', 101);
INSERT INTO `sys_oper_log` VALUES (9, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/157', '0:0:0:0:0:0:0:1', '', '157 {\"id\":157,\"parentId\":0,\"menuName\":\"智能分配\",\"menuType\":\"M\",\"path\":\"/allocation\",\"component\":\"/index/index\",\"permission\":\"\",\"icon\":\"ri:robot-line\",\"sort\":5,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770102322837}', 0, '', '2026-02-03 15:05:23', 11);
INSERT INTO `sys_oper_log` VALUES (10, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/158', '0:0:0:0:0:0:0:1', '', '158 {\"id\":158,\"parentId\":157,\"menuName\":\"分配配置\",\"menuType\":\"C\",\"path\":\"config\",\"component\":\"/allocation/config\",\"permission\":\"allocation:config:view\",\"icon\":\"\",\"sort\":1,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770102359154}', 0, '', '2026-02-03 15:05:59', 3);
INSERT INTO `sys_oper_log` VALUES (11, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/162', '0:0:0:0:0:0:0:1', '', '162 {\"id\":162,\"parentId\":157,\"menuName\":\"分配任务\",\"menuType\":\"C\",\"path\":\"task\",\"component\":\"/allocation/task\",\"permission\":\"allocation:task:view\",\"icon\":\"\",\"sort\":2,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770102363543}', 0, '', '2026-02-03 15:06:04', 4);
INSERT INTO `sys_oper_log` VALUES (12, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/167', '0:0:0:0:0:0:0:1', '', '167 {\"id\":167,\"parentId\":157,\"menuName\":\"分配结果\",\"menuType\":\"C\",\"path\":\"result\",\"component\":\"/allocation/result\",\"permission\":\"allocation:result:view\",\"icon\":\"\",\"sort\":3,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770102367920}', 0, '', '2026-02-03 15:06:08', 4);
INSERT INTO `sys_oper_log` VALUES (13, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/172', '0:0:0:0:0:0:0:1', '', '172 {\"id\":172,\"parentId\":157,\"menuName\":\"问卷管理\",\"menuType\":\"C\",\"path\":\"survey\",\"component\":\"/allocation/survey\",\"permission\":\"allocation:survey:view\",\"icon\":\"\",\"sort\":4,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770102372495}', 0, '', '2026-02-03 15:06:12', 4);
INSERT INTO `sys_oper_log` VALUES (14, '分配角色权限', 0, 'com.project.backend.system.controller.RoleController.assignPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/role/1/permissions', '0:0:0:0:0:0:0:1', '', '1 []', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1770102399054}', 0, '', '2026-02-03 15:06:39', 14);
INSERT INTO `sys_oper_log` VALUES (15, '分配角色权限', 0, 'com.project.backend.system.controller.RoleController.assignPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/role/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,90,97,100,101,102,103,104,105,118,98,106,107,108,109,110,111,119,99,112,113,114,115,116,117,152,153,154,155,156,146,147,148,149,150,151,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,142,143,144,145,50,67,55,56,57,89,94,95,96,88,68,58,59,60,69,61,62,63,70,64,65,66,3,4,16,17,18,19,87,124,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1770102401740}', 0, '', '2026-02-03 15:06:42', 84);
INSERT INTO `sys_oper_log` VALUES (16, '分配用户权限', 0, 'com.project.backend.system.controller.UserController.assignUserPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/user/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,90,97,100,101,102,103,104,105,118,98,106,107,108,109,110,111,119,99,112,113,114,115,116,117,152,153,154,155,156,146,147,148,149,150,151,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,142,143,144,145,50,67,55,56,57,89,94,95,96,88,68,58,59,60,69,61,62,63,70,64,65,66,3,4,16,17,18,19,87,124,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1770102408869}', 0, '', '2026-02-03 15:06:49', 124);
INSERT INTO `sys_oper_log` VALUES (17, '分配配置管理-编辑分配配置(ID:1)', 2, 'com.project.backend.allocation.controller.AllocationConfigController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/allocation/config/1', '0:0:0:0:0:0:0:1', '', '1 {\"id\":1,\"configName\":\"默认配置模板\",\"smokingConstraint\":1,\"genderConstraint\":1,\"sleepHardConstraint\":0,\"sleepWeight\":30,\"smokingWeight\":20,\"cleanlinessWeight\":15,\"socialWeight\":15,\"studyWeight\":10,\"entertainmentWeight\":10,\"algorithmType\":\"kmeans\",\"sameDeptBonus\":5,\"sameMajorBonus\":10,\"sameClassBonus\":15,\"minMatchScore\":60,\"remark\":\"系统默认配置，可作为创建新配置的模板\"}', '{\"code\":200,\"message\":\"分配配置编辑成功\",\"data\":null,\"timestamp\":1770118258379}', 0, '', '2026-02-03 19:30:58', 20);
INSERT INTO `sys_oper_log` VALUES (18, '修改状态', 2, 'com.project.backend.school.controller.AcademicYearController.updateStatus()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/academic-year/14/status/1', '0:0:0:0:0:0:0:1', '', '14 1', '{\"code\":200,\"message\":\"学年已启用\",\"data\":null,\"timestamp\":1770123679525}', 0, '', '2026-02-03 21:01:20', 13);
INSERT INTO `sys_oper_log` VALUES (19, '修改状态', 2, 'com.project.backend.school.controller.AcademicYearController.updateStatus()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/academic-year/14/status/0', '0:0:0:0:0:0:0:1', '', '14 0', '{\"code\":200,\"message\":\"学年已停用\",\"data\":null,\"timestamp\":1770123680298}', 0, '', '2026-02-03 21:01:20', 20);
INSERT INTO `sys_oper_log` VALUES (20, '楼层管理-新增楼层', 1, 'com.project.backend.room.controller.FloorController.add()', 'POST', 1, 'superAdmin', 1, '/api/v1/system/floor', '0:0:0:0:0:0:0:1', '', '{\"id\":null,\"floorCode\":\"F1\",\"floorName\":\"1号楼\",\"floorNumber\":6,\"campusCode\":\"CAMPUS001\",\"genderType\":3,\"sort\":1,\"status\":1,\"remark\":\"学生宿舍楼，共6层\"}', '{\"code\":200,\"message\":\"楼层新增成功\",\"data\":null,\"timestamp\":1770125809861}', 0, '', '2026-02-03 21:36:50', 3);
INSERT INTO `sys_oper_log` VALUES (21, '楼层管理-新增楼层', 1, 'com.project.backend.room.controller.FloorController.add()', 'POST', 1, 'superAdmin', 1, '/api/v1/system/floor', '0:0:0:0:0:0:0:1', '', '{\"id\":null,\"floorCode\":\"F2\",\"floorName\":\"2号楼\",\"floorNumber\":5,\"campusCode\":\"CAMPUS001\",\"genderType\":1,\"sort\":2,\"status\":1,\"remark\":\"男生宿舍楼\"}', '{\"code\":200,\"message\":\"楼层新增成功\",\"data\":null,\"timestamp\":1770125849896}', 0, '', '2026-02-03 21:37:30', 18);
INSERT INTO `sys_oper_log` VALUES (22, '楼层管理-新增楼层', 1, 'com.project.backend.room.controller.FloorController.add()', 'POST', 1, 'superAdmin', 1, '/api/v1/system/floor', '0:0:0:0:0:0:0:1', '', '{\"id\":null,\"floorCode\":\"F3\",\"floorName\":\"3号楼\",\"floorNumber\":5,\"campusCode\":\"CAMPUS001\",\"genderType\":2,\"sort\":3,\"status\":1,\"remark\":\"女生宿舍\"}', '{\"code\":200,\"message\":\"楼层新增成功\",\"data\":null,\"timestamp\":1770125873761}', 0, '', '2026-02-03 21:37:54', 0);
INSERT INTO `sys_oper_log` VALUES (23, '分配用户权限', 0, 'com.project.backend.system.controller.UserController.assignUserPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/user/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,125,130,131,132,133,134,135,136,137,138,139,140,141,126,90,97,100,101,102,103,104,105,118,98,106,107,108,109,110,111,119,99,112,113,114,115,116,117,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,146,147,148,149,150,151,142,143,144,145,50,67,55,56,57,89,94,95,96,88,68,58,59,60,69,61,62,63,70,64,65,66,3,4,16,17,18,19,87,124,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1770132206903}', 0, '', '2026-02-03 23:23:27', 132);
INSERT INTO `sys_oper_log` VALUES (24, '分配角色权限', 0, 'com.project.backend.system.controller.RoleController.assignPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/role/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,125,130,131,132,133,134,135,136,137,138,139,140,141,126,177,127,128,129,90,97,100,101,102,103,104,105,118,98,106,107,108,109,110,111,119,99,112,113,114,115,116,117,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,146,147,148,149,150,151,142,143,144,145,50,67,55,56,57,89,94,95,96,88,68,58,59,60,69,61,62,63,70,64,65,66,3,4,16,17,18,19,87,124,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1770132225756}', 0, '', '2026-02-03 23:23:46', 96);
INSERT INTO `sys_oper_log` VALUES (25, '分配用户权限', 0, 'com.project.backend.system.controller.UserController.assignUserPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/user/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,125,130,131,132,133,134,135,136,137,138,139,140,141,126,177,127,128,129,90,97,100,101,102,103,104,105,118,98,106,107,108,109,110,111,119,99,112,113,114,115,116,117,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,146,147,148,149,150,151,142,143,144,145,50,67,55,56,57,89,94,95,96,88,68,58,59,60,69,61,62,63,70,64,65,66,3,4,16,17,18,19,87,124,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1770132233864}', 0, '', '2026-02-03 23:23:54', 90);
INSERT INTO `sys_oper_log` VALUES (26, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/8', '0:0:0:0:0:0:0:1', '', '8 {\"id\":8,\"parentId\":0,\"menuName\":\"异常页面\",\"menuType\":\"M\",\"path\":\"/exception\",\"component\":\"/index/index\",\"permission\":\"\",\"icon\":\"ri:error-warning-line\",\"sort\":12,\"visible\":0,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770174481611}', 0, '', '2026-02-04 11:08:02', 25);
INSERT INTO `sys_oper_log` VALUES (27, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/8', '0:0:0:0:0:0:0:1', '', '8 {\"id\":8,\"parentId\":0,\"menuName\":\"异常页面\",\"menuType\":\"M\",\"path\":\"/exception\",\"component\":\"/index/index\",\"permission\":\"\",\"icon\":\"ri:error-warning-line\",\"sort\":12,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770174486227}', 0, '', '2026-02-04 11:08:06', 5);
INSERT INTO `sys_oper_log` VALUES (28, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/8', '0:0:0:0:0:0:0:1', '', '8 {\"id\":8,\"parentId\":0,\"menuName\":\"异常页面\",\"menuType\":\"M\",\"path\":\"/exception\",\"component\":\"/index/index\",\"permission\":\"\",\"icon\":\"ri:error-warning-line\",\"sort\":12,\"visible\":0,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770359292150}', 0, '', '2026-02-06 14:28:12', 202);
INSERT INTO `sys_oper_log` VALUES (29, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/8', '0:0:0:0:0:0:0:1', '', '8 {\"id\":8,\"parentId\":0,\"menuName\":\"异常页面\",\"menuType\":\"M\",\"path\":\"/exception\",\"component\":\"/index/index\",\"permission\":\"\",\"icon\":\"ri:error-warning-line\",\"sort\":12,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770359305275}', 0, '', '2026-02-06 14:28:25', 8);
INSERT INTO `sys_oper_log` VALUES (30, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/8', '0:0:0:0:0:0:0:1', '', '8 {\"id\":8,\"parentId\":0,\"menuName\":\"异常页面\",\"menuType\":\"M\",\"path\":\"/exception\",\"component\":\"/index/index\",\"permission\":\"\",\"icon\":\"ri:error-warning-line\",\"sort\":12,\"visible\":0,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770359319370}', 0, '', '2026-02-06 14:28:39', 5);
INSERT INTO `sys_oper_log` VALUES (31, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/9', '0:0:0:0:0:0:0:1', '', '9 {\"id\":9,\"parentId\":8,\"menuName\":\"403\",\"menuType\":\"C\",\"path\":\"403\",\"component\":\"/exception/403\",\"permission\":\"exception:403:view\",\"icon\":\"\",\"sort\":1,\"visible\":0,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770359351684}', 0, '', '2026-02-06 14:29:12', 6);
INSERT INTO `sys_oper_log` VALUES (32, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/10', '0:0:0:0:0:0:0:1', '', '10 {\"id\":10,\"parentId\":8,\"menuName\":\"404\",\"menuType\":\"C\",\"path\":\"404\",\"component\":\"/exception/404\",\"permission\":\"exception:404:view\",\"icon\":\"\",\"sort\":2,\"visible\":0,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770359354923}', 0, '', '2026-02-06 14:29:15', 6);
INSERT INTO `sys_oper_log` VALUES (33, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/11', '0:0:0:0:0:0:0:1', '', '11 {\"id\":11,\"parentId\":8,\"menuName\":\"500\",\"menuType\":\"C\",\"path\":\"500\",\"component\":\"/exception/500\",\"permission\":\"exception:500:view\",\"icon\":\"\",\"sort\":3,\"visible\":0,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770359358153}', 0, '', '2026-02-06 14:29:18', 4);
INSERT INTO `sys_oper_log` VALUES (34, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/8', '0:0:0:0:0:0:0:1', '', '8 {\"id\":8,\"parentId\":0,\"menuName\":\"异常页面\",\"menuType\":\"M\",\"path\":\"/exception\",\"component\":\"/index/index\",\"permission\":\"\",\"icon\":\"ri:error-warning-line\",\"sort\":12,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770359374481}', 0, '', '2026-02-06 14:29:34', 4);
INSERT INTO `sys_oper_log` VALUES (35, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/9', '0:0:0:0:0:0:0:1', '', '9 {\"id\":9,\"parentId\":8,\"menuName\":\"403\",\"menuType\":\"C\",\"path\":\"403\",\"component\":\"/exception/403\",\"permission\":\"exception:403:view\",\"icon\":\"\",\"sort\":1,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770359386145}', 0, '', '2026-02-06 14:29:46', 4);
INSERT INTO `sys_oper_log` VALUES (36, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/10', '0:0:0:0:0:0:0:1', '', '10 {\"id\":10,\"parentId\":8,\"menuName\":\"404\",\"menuType\":\"C\",\"path\":\"404\",\"component\":\"/exception/404\",\"permission\":\"exception:404:view\",\"icon\":\"\",\"sort\":2,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770359400860}', 0, '', '2026-02-06 14:30:01', 0);
INSERT INTO `sys_oper_log` VALUES (37, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/11', '0:0:0:0:0:0:0:1', '', '11 {\"id\":11,\"parentId\":8,\"menuName\":\"500\",\"menuType\":\"C\",\"path\":\"500\",\"component\":\"/exception/500\",\"permission\":\"exception:500:view\",\"icon\":\"\",\"sort\":3,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770359403610}', 0, '', '2026-02-06 14:30:04', 4);
INSERT INTO `sys_oper_log` VALUES (38, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/9', '0:0:0:0:0:0:0:1', '', '9 {\"id\":9,\"parentId\":8,\"menuName\":\"403\",\"menuType\":\"C\",\"path\":\"403\",\"component\":\"/exception/403\",\"permission\":\"exception:403:view\",\"icon\":\"\",\"sort\":1,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770359747154}', 0, '', '2026-02-06 14:35:47', 8);
INSERT INTO `sys_oper_log` VALUES (39, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/8', '0:0:0:0:0:0:0:1', '', '8 {\"id\":8,\"parentId\":0,\"menuName\":\"异常页面\",\"menuType\":\"M\",\"path\":\"/exception\",\"component\":\"/index/index\",\"permission\":\"\",\"icon\":\"ri:error-warning-line\",\"sort\":12,\"visible\":0,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770362580589}', 0, '', '2026-02-06 15:23:01', 86);
INSERT INTO `sys_oper_log` VALUES (40, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/8', '0:0:0:0:0:0:0:1', '', '8 {\"id\":8,\"parentId\":0,\"menuName\":\"异常页面\",\"menuType\":\"M\",\"path\":\"/exception\",\"component\":\"/index/index\",\"permission\":\"\",\"icon\":\"ri:error-warning-line\",\"sort\":12,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770362583995}', 0, '', '2026-02-06 15:23:04', 6);
INSERT INTO `sys_oper_log` VALUES (41, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/9', '0:0:0:0:0:0:0:1', '', '9 {\"id\":9,\"parentId\":8,\"menuName\":\"403\",\"menuType\":\"C\",\"path\":\"403\",\"component\":\"/exception/403\",\"permission\":\"exception:403:view\",\"icon\":\"\",\"sort\":1,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770362587074}', 0, '', '2026-02-06 15:23:07', 7);
INSERT INTO `sys_oper_log` VALUES (42, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/10', '0:0:0:0:0:0:0:1', '', '10 {\"id\":10,\"parentId\":8,\"menuName\":\"404\",\"menuType\":\"C\",\"path\":\"404\",\"component\":\"/exception/404\",\"permission\":\"exception:404:view\",\"icon\":\"\",\"sort\":2,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770362589998}', 0, '', '2026-02-06 15:23:10', 5);
INSERT INTO `sys_oper_log` VALUES (43, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/11', '0:0:0:0:0:0:0:1', '', '11 {\"id\":11,\"parentId\":8,\"menuName\":\"500\",\"menuType\":\"C\",\"path\":\"500\",\"component\":\"/exception/500\",\"permission\":\"exception:500:view\",\"icon\":\"\",\"sort\":3,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1770362592619}', 0, '', '2026-02-06 15:23:13', 6);

-- ----------------------------
-- Table structure for sys_repair
-- ----------------------------
DROP TABLE IF EXISTS `sys_repair`;
CREATE TABLE `sys_repair`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID（关联 sys_student）',
  `room_id` bigint NULL DEFAULT NULL COMMENT '房间ID（关联 sys_room）',
  `bed_id` bigint NULL DEFAULT NULL COMMENT '床位ID（关联 sys_bed）',
  `repair_type` int NOT NULL COMMENT '维修类型：1-水电 2-门窗 3-家具 4-网络 5-其他',
  `fault_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '故障描述',
  `fault_images` json NULL COMMENT '故障图片（JSON数组）',
  `urgent_level` int NOT NULL DEFAULT 1 COMMENT '紧急程度：1-一般 2-紧急 3-非常紧急',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-待接单 2-已接单 3-维修中 4-已完成 5-已取消',
  `repair_person_id` bigint NULL DEFAULT NULL COMMENT '维修人员ID',
  `repair_person_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '维修人员姓名',
  `appointment_time` datetime NULL DEFAULT NULL COMMENT '预约时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `repair_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '维修结果描述',
  `repair_images` json NULL COMMENT '维修后图片（JSON数组）',
  `rating` int NULL DEFAULT NULL COMMENT '评分：1-5星',
  `rating_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评价内容',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_repair_type`(`repair_type` ASC) USING BTREE,
  INDEX `idx_repair_person_id`(`repair_person_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_student_status`(`student_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_repairer_status`(`repair_person_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_room`(`room_id` ASC) USING BTREE,
  INDEX `idx_type_urgent`(`repair_type` ASC, `urgent_level` ASC) USING BTREE,
  INDEX `idx_repair_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '报修管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_repair
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-停用 1-正常',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'SUPER_ADMIN', '超级管理员', 1, 1, '系统最高权限', 0, '2025-12-30 17:19:06', NULL, '2026-01-21 16:50:42', 1);
INSERT INTO `sys_role` VALUES (2, 'DORMITORY_MANAGER', '宿管员', 2, 1, '管理宿舍楼和房间信息', 0, '2025-12-30 17:19:06', NULL, '2026-01-21 16:50:44', 1);
INSERT INTO `sys_role` VALUES (3, 'COUNSELOR', '辅导员', 3, 1, '审核本学院学生申请', 0, '2025-12-30 17:19:06', NULL, '2026-01-21 16:50:46', 1);
INSERT INTO `sys_role` VALUES (4, 'COLLEGE_ADMIN', '院系管理员', 4, 1, '管理本学院住宿信息', 0, '2025-12-30 17:19:06', NULL, '2026-01-21 16:50:48', 1);
INSERT INTO `sys_role` VALUES (5, 'SCHOOL_AUDITOR', '学校审核员', 5, 1, '最终审核权限', 0, '2025-12-30 17:19:06', NULL, '2026-01-21 16:50:38', 1);
INSERT INTO `sys_role` VALUES (6, 'TEST_ROLE', '测试角色', 100, 1, '这是一个测试角色', 0, '2025-12-31 11:41:23', NULL, '2026-01-21 16:50:40', 1);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id` ASC, `menu_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_menu_id`(`menu_id` ASC) USING BTREE,
  INDEX `idx_role`(`role_id` ASC) USING BTREE,
  INDEX `idx_menu`(`menu_id` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE,
  INDEX `idx_role_deleted`(`role_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_menu_deleted`(`menu_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2952 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1954, 6, 1, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1955, 6, 2, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1956, 6, 125, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1957, 6, 126, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1958, 6, 127, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1959, 6, 128, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1960, 6, 129, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1961, 6, 130, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1962, 6, 131, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1963, 6, 132, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1964, 6, 133, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1965, 6, 134, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1966, 6, 135, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1967, 6, 136, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1968, 6, 137, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1969, 6, 138, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1970, 6, 139, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1971, 6, 140, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1972, 6, 141, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1973, 6, 90, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1974, 6, 97, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1975, 6, 100, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1976, 6, 101, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1977, 6, 102, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1978, 6, 103, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1979, 6, 104, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1980, 6, 105, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1981, 6, 118, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1982, 6, 98, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1983, 6, 106, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1984, 6, 107, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1985, 6, 108, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1986, 6, 109, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1987, 6, 110, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1988, 6, 111, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1989, 6, 119, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1990, 6, 99, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1991, 6, 112, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1992, 6, 113, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1993, 6, 114, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1994, 6, 115, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1995, 6, 116, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1996, 6, 117, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1997, 6, 142, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1998, 6, 143, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1999, 6, 144, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2000, 6, 145, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2001, 6, 88, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2002, 6, 68, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2003, 6, 58, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2004, 6, 59, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2005, 6, 60, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2006, 6, 69, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2007, 6, 61, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2008, 6, 62, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2009, 6, 63, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2010, 6, 70, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2011, 6, 64, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2012, 6, 65, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2013, 6, 66, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2014, 6, 50, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2015, 6, 67, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2016, 6, 55, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2017, 6, 56, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2018, 6, 57, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2019, 6, 89, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2020, 6, 94, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2021, 6, 95, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2022, 6, 96, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2023, 6, 3, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2024, 6, 4, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2025, 6, 16, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2026, 6, 17, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2027, 6, 18, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2028, 6, 19, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2029, 6, 87, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2030, 6, 124, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2031, 6, 5, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2032, 6, 37, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2033, 6, 38, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2034, 6, 39, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2035, 6, 40, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2036, 6, 6, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2037, 6, 41, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2038, 6, 42, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2039, 6, 43, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2040, 6, 15, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2041, 6, 44, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2042, 6, 45, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2043, 6, 46, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2044, 6, 47, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2045, 6, 48, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2046, 6, 49, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2047, 6, 7, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2048, 6, 83, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2049, 6, 84, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2050, 6, 85, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2051, 6, 86, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2052, 6, 8, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2053, 6, 9, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2054, 6, 10, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2055, 6, 11, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2056, 6, 12, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2057, 6, 13, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2058, 6, 14, '2026-01-17 22:54:57', NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2683, 1, 1, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2684, 1, 2, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2685, 1, 125, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2686, 1, 126, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2687, 1, 127, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2688, 1, 128, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2689, 1, 129, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2690, 1, 130, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2691, 1, 131, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2692, 1, 132, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2693, 1, 133, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2694, 1, 134, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2695, 1, 135, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2696, 1, 136, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2697, 1, 137, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2698, 1, 138, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2699, 1, 139, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2700, 1, 140, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2701, 1, 141, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2702, 1, 90, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2703, 1, 97, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2704, 1, 100, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2705, 1, 101, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2706, 1, 102, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2707, 1, 103, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2708, 1, 104, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2709, 1, 105, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2710, 1, 118, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2711, 1, 98, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2712, 1, 106, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2713, 1, 107, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2714, 1, 108, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2715, 1, 109, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2716, 1, 110, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2717, 1, 111, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2718, 1, 119, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2719, 1, 99, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2720, 1, 112, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2721, 1, 113, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2722, 1, 114, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2723, 1, 115, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2724, 1, 116, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2725, 1, 117, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2726, 1, 152, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2727, 1, 153, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2728, 1, 154, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2729, 1, 155, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2730, 1, 156, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2731, 1, 146, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2732, 1, 147, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2733, 1, 148, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2734, 1, 149, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2735, 1, 150, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2736, 1, 151, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2737, 1, 157, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2738, 1, 158, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2739, 1, 159, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2740, 1, 160, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2741, 1, 161, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2742, 1, 162, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2743, 1, 163, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2744, 1, 164, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2745, 1, 165, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2746, 1, 166, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2747, 1, 167, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2748, 1, 168, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2749, 1, 169, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2750, 1, 170, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2751, 1, 171, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2752, 1, 172, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2753, 1, 173, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2754, 1, 174, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2755, 1, 142, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2756, 1, 143, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2757, 1, 144, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2758, 1, 145, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2759, 1, 50, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2760, 1, 67, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2761, 1, 55, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2762, 1, 56, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2763, 1, 57, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2764, 1, 89, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2765, 1, 94, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2766, 1, 95, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2767, 1, 96, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2768, 1, 88, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2769, 1, 68, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2770, 1, 58, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2771, 1, 59, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2772, 1, 60, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2773, 1, 69, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2774, 1, 61, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2775, 1, 62, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2776, 1, 63, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2777, 1, 70, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2778, 1, 64, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2779, 1, 65, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2780, 1, 66, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2781, 1, 3, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2782, 1, 4, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2783, 1, 16, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2784, 1, 17, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2785, 1, 18, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2786, 1, 19, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2787, 1, 87, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2788, 1, 124, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2789, 1, 5, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2790, 1, 37, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2791, 1, 38, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2792, 1, 39, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2793, 1, 40, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2794, 1, 6, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2795, 1, 41, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2796, 1, 42, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2797, 1, 43, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2798, 1, 15, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2799, 1, 44, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2800, 1, 45, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2801, 1, 46, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2802, 1, 47, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2803, 1, 48, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2804, 1, 49, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2805, 1, 7, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2806, 1, 83, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2807, 1, 84, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2808, 1, 85, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2809, 1, 86, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2810, 1, 8, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2811, 1, 9, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2812, 1, 10, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2813, 1, 11, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2814, 1, 12, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2815, 1, 13, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2816, 1, 14, '2026-02-03 15:06:42', '2026-02-03 15:06:42', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2817, 1, 1, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2818, 1, 2, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2819, 1, 125, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2820, 1, 130, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2821, 1, 131, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2822, 1, 132, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2823, 1, 133, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2824, 1, 134, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2825, 1, 135, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2826, 1, 136, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2827, 1, 137, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2828, 1, 138, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2829, 1, 139, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2830, 1, 140, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2831, 1, 141, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2832, 1, 126, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2833, 1, 177, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2834, 1, 127, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2835, 1, 128, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2836, 1, 129, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2837, 1, 90, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2838, 1, 97, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2839, 1, 100, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2840, 1, 101, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2841, 1, 102, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2842, 1, 103, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2843, 1, 104, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2844, 1, 105, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2845, 1, 118, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2846, 1, 98, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2847, 1, 106, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2848, 1, 107, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2849, 1, 108, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2850, 1, 109, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2851, 1, 110, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2852, 1, 111, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2853, 1, 119, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2854, 1, 99, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2855, 1, 112, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2856, 1, 113, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2857, 1, 114, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2858, 1, 115, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2859, 1, 116, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2860, 1, 117, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2861, 1, 152, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2862, 1, 153, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2863, 1, 154, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2864, 1, 155, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2865, 1, 156, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2866, 1, 157, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2867, 1, 158, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2868, 1, 159, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2869, 1, 160, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2870, 1, 161, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2871, 1, 162, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2872, 1, 163, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2873, 1, 164, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2874, 1, 165, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2875, 1, 166, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2876, 1, 167, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2877, 1, 168, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2878, 1, 169, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2879, 1, 170, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2880, 1, 171, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2881, 1, 172, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2882, 1, 173, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2883, 1, 174, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2884, 1, 146, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2885, 1, 147, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2886, 1, 148, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2887, 1, 149, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2888, 1, 150, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2889, 1, 151, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2890, 1, 142, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2891, 1, 143, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2892, 1, 144, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2893, 1, 145, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2894, 1, 50, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2895, 1, 67, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2896, 1, 55, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2897, 1, 56, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2898, 1, 57, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2899, 1, 89, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2900, 1, 94, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2901, 1, 95, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2902, 1, 96, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2903, 1, 88, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2904, 1, 68, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2905, 1, 58, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2906, 1, 59, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2907, 1, 60, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2908, 1, 69, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2909, 1, 61, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2910, 1, 62, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2911, 1, 63, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2912, 1, 70, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2913, 1, 64, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2914, 1, 65, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2915, 1, 66, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2916, 1, 3, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2917, 1, 4, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2918, 1, 16, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2919, 1, 17, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2920, 1, 18, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2921, 1, 19, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2922, 1, 87, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2923, 1, 124, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2924, 1, 5, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2925, 1, 37, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2926, 1, 38, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2927, 1, 39, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2928, 1, 40, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2929, 1, 6, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2930, 1, 41, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2931, 1, 42, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2932, 1, 43, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2933, 1, 15, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2934, 1, 44, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2935, 1, 45, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2936, 1, 46, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2937, 1, 47, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2938, 1, 48, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2939, 1, 49, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2940, 1, 7, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2941, 1, 83, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2942, 1, 84, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2943, 1, 85, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2944, 1, 86, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2945, 1, 8, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2946, 1, 9, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2947, 1, 10, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2948, 1, 11, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2949, 1, 12, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2950, 1, 13, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (2951, 1, 14, '2026-02-03 23:23:46', '2026-02-03 23:23:46', 1, 1, 0);

-- ----------------------------
-- Table structure for sys_room
-- ----------------------------
DROP TABLE IF EXISTS `sys_room`;
CREATE TABLE `sys_room`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房间编码（如：101、102）',
  `room_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房间号（如：101、102）',
  `floor_id` bigint NOT NULL COMMENT '所属楼层ID',
  `floor_number` int NULL DEFAULT NULL COMMENT '所属楼层数（冗余字段）',
  `floor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属楼层编码（冗余字段）',
  `campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属校区编码（冗余字段）',
  `room_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '房间类型：standard-标准间 suite-套间 apartment-公寓',
  `bed_count` int NOT NULL DEFAULT 4 COMMENT '床位数（标准配置）',
  `current_occupancy` int NOT NULL DEFAULT 0 COMMENT '当前入住人数',
  `max_occupancy` int NULL DEFAULT NULL COMMENT '最大入住人数',
  `area` decimal(10, 2) NULL DEFAULT NULL COMMENT '房间面积（平方米）',
  `has_air_conditioner` tinyint NOT NULL DEFAULT 0 COMMENT '是否有空调：1是 0否',
  `has_bathroom` tinyint NOT NULL DEFAULT 0 COMMENT '是否有独立卫生间：1是 0否',
  `has_balcony` tinyint NOT NULL DEFAULT 0 COMMENT '是否有阳台：1是 0否',
  `room_status` tinyint NOT NULL DEFAULT 1 COMMENT '房间状态：1-正常 2-维修中 3-停用',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_room_code`(`room_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_floor_id`(`floor_id` ASC) USING BTREE,
  INDEX `idx_floor_code`(`floor_code` ASC) USING BTREE,
  INDEX `idx_campus_code`(`campus_code` ASC) USING BTREE,
  INDEX `idx_room_code`(`room_code` ASC) USING BTREE,
  INDEX `idx_floor_id_number`(`floor_id` ASC, `floor_number` ASC) USING BTREE,
  INDEX `idx_floor_status`(`floor_id` ASC, `room_status` ASC) USING BTREE,
  INDEX `idx_campus_floor`(`campus_code` ASC, `floor_code` ASC) USING BTREE,
  INDEX `idx_room_type`(`room_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房间表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_room
-- ----------------------------
INSERT INTO `sys_room` VALUES (1, 'F1-0101', '0101', 1, 1, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 1, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (2, 'F1-0102', '0102', 1, 1, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 2, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (3, 'F1-0103', '0103', 1, 1, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 3, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (4, 'F1-0104', '0104', 1, 1, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 4, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (5, 'F1-0105', '0105', 1, 1, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 5, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (6, 'F1-0201', '0201', 1, 2, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 6, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (7, 'F1-0202', '0202', 1, 2, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 7, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (8, 'F1-0203', '0203', 1, 2, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 8, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (9, 'F1-0204', '0204', 1, 2, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 9, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (10, 'F1-0205', '0205', 1, 2, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 10, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (11, 'F1-0301', '0301', 1, 3, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 11, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (12, 'F1-0302', '0302', 1, 3, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 12, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (13, 'F1-0303', '0303', 1, 3, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 13, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (14, 'F1-0304', '0304', 1, 3, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 14, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (15, 'F1-0305', '0305', 1, 3, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 15, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (16, 'F1-0401', '0401', 1, 4, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 16, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (17, 'F1-0402', '0402', 1, 4, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 17, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (18, 'F1-0403', '0403', 1, 4, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 18, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (19, 'F1-0404', '0404', 1, 4, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 19, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (20, 'F1-0405', '0405', 1, 4, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 20, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (21, 'F1-0501', '0501', 1, 5, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 21, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (22, 'F1-0502', '0502', 1, 5, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 22, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (23, 'F1-0503', '0503', 1, 5, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 23, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (24, 'F1-0504', '0504', 1, 5, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 24, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (25, 'F1-0505', '0505', 1, 5, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 25, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (26, 'F1-0601', '0601', 1, 6, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 26, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (27, 'F1-0602', '0602', 1, 6, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 27, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (28, 'F1-0603', '0603', 1, 6, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 28, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (29, 'F1-0604', '0604', 1, 6, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 29, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (30, 'F1-0605', '0605', 1, 6, 'F1', 'CAMPUS001', 'standard', 6, 0, 6, 24.50, 1, 1, 1, 1, 30, 1, NULL, 0, '2026-02-03 21:38:39', 1, '2026-02-03 21:38:39', 1);
INSERT INTO `sys_room` VALUES (31, 'F2-0101', '0101', 2, 1, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 1, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (32, 'F2-0102', '0102', 2, 1, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 2, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (33, 'F2-0103', '0103', 2, 1, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 3, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (34, 'F2-0104', '0104', 2, 1, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 4, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (35, 'F2-0105', '0105', 2, 1, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 5, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (36, 'F2-0201', '0201', 2, 2, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 6, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (37, 'F2-0202', '0202', 2, 2, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 7, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (38, 'F2-0203', '0203', 2, 2, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 8, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (39, 'F2-0204', '0204', 2, 2, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 9, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (40, 'F2-0205', '0205', 2, 2, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 10, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (41, 'F2-0301', '0301', 2, 3, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 11, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (42, 'F2-0302', '0302', 2, 3, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 12, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (43, 'F2-0303', '0303', 2, 3, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 13, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (44, 'F2-0304', '0304', 2, 3, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 14, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (45, 'F2-0305', '0305', 2, 3, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 15, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (46, 'F2-0401', '0401', 2, 4, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 16, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (47, 'F2-0402', '0402', 2, 4, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 17, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (48, 'F2-0403', '0403', 2, 4, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 18, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (49, 'F2-0404', '0404', 2, 4, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 19, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (50, 'F2-0405', '0405', 2, 4, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 20, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (51, 'F2-0501', '0501', 2, 5, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 21, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (52, 'F2-0502', '0502', 2, 5, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 22, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (53, 'F2-0503', '0503', 2, 5, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 23, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (54, 'F2-0504', '0504', 2, 5, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 24, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (55, 'F2-0505', '0505', 2, 5, 'F2', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 25, 1, NULL, 0, '2026-02-03 21:38:56', 1, '2026-02-03 21:38:56', 1);
INSERT INTO `sys_room` VALUES (56, 'F3-0101', '0101', 3, 1, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 1, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (57, 'F3-0102', '0102', 3, 1, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 2, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (58, 'F3-0103', '0103', 3, 1, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 3, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (59, 'F3-0104', '0104', 3, 1, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 4, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (60, 'F3-0105', '0105', 3, 1, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 5, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (61, 'F3-0201', '0201', 3, 2, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 6, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (62, 'F3-0202', '0202', 3, 2, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 7, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (63, 'F3-0203', '0203', 3, 2, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 8, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (64, 'F3-0204', '0204', 3, 2, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 9, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (65, 'F3-0205', '0205', 3, 2, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 10, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (66, 'F3-0301', '0301', 3, 3, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 11, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (67, 'F3-0302', '0302', 3, 3, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 12, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (68, 'F3-0303', '0303', 3, 3, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 13, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (69, 'F3-0304', '0304', 3, 3, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 14, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (70, 'F3-0305', '0305', 3, 3, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 15, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (71, 'F3-0401', '0401', 3, 4, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 16, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (72, 'F3-0402', '0402', 3, 4, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 17, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (73, 'F3-0403', '0403', 3, 4, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 18, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (74, 'F3-0404', '0404', 3, 4, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 19, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (75, 'F3-0405', '0405', 3, 4, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 20, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (76, 'F3-0501', '0501', 3, 5, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 21, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (77, 'F3-0502', '0502', 3, 5, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 22, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (78, 'F3-0503', '0503', 3, 5, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 23, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (79, 'F3-0504', '0504', 3, 5, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 24, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);
INSERT INTO `sys_room` VALUES (80, 'F3-0505', '0505', 3, 5, 'F3', 'CAMPUS001', 'standard', 4, 0, 4, 24.50, 1, 1, 1, 1, 25, 1, NULL, 0, '2026-02-03 21:39:13', 1, '2026-02-03 21:39:13', 1);

-- ----------------------------
-- Table structure for sys_semester
-- ----------------------------
DROP TABLE IF EXISTS `sys_semester`;
CREATE TABLE `sys_semester`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `academic_year_id` bigint NOT NULL COMMENT '所属学年ID',
  `semester_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学期编码',
  `semester_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学期名称',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `semester_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学期类型（如：第一学期、第二学期）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_semester_code`(`semester_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_academic_year_id`(`academic_year_id` ASC) USING BTREE,
  CONSTRAINT `fk_semester_academic_year` FOREIGN KEY (`academic_year_id`) REFERENCES `sys_academic_year` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学期表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_semester
-- ----------------------------
INSERT INTO `sys_semester` VALUES (1, 11, '2021-2022-1', '2021-2022学年第一学期', '2021-09-01', '2022-01-31', '第一学期', '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_semester` VALUES (2, 11, '2021-2022-2', '2021-2022学年第二学期', '2022-02-01', '2022-08-31', '第二学期', '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_semester` VALUES (3, 12, '2022-2023-1', '2022-2023学年第一学期', '2022-09-01', '2023-01-31', '第一学期', '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_semester` VALUES (4, 12, '2022-2023-2', '2022-2023学年第二学期', '2023-02-01', '2023-08-31', '第二学期', '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_semester` VALUES (5, 13, '2023-2024-1', '2023-2024学年第一学期', '2023-09-01', '2024-01-31', '第一学期', '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_semester` VALUES (6, 13, '2023-2024-2', '2023-2024学年第二学期', '2024-02-01', '2024-08-31', '第二学期', '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_semester` VALUES (7, 14, '2024-2025-1', '2024-2025学年第一学期', '2024-09-01', '2025-01-31', '第一学期', '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_semester` VALUES (8, 14, '2024-2025-2', '2024-2025学年第二学期', '2025-02-01', '2025-08-31', '第二学期', '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_semester` VALUES (9, 15, '2025-2026-1', '2025-2026学年第一学期', '2025-09-01', '2026-01-31', '第一学期', '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);
INSERT INTO `sys_semester` VALUES (10, 15, '2025-2026-2', '2025-2026学年第二学期', '2026-02-01', '2026-08-31', '第二学期', '2026-02-03 20:51:44', NULL, '2026-02-03 20:51:44', NULL, 0);

-- ----------------------------
-- Table structure for sys_stay
-- ----------------------------
DROP TABLE IF EXISTS `sys_stay`;
CREATE TABLE `sys_stay`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生姓名（冗余）',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号（冗余）',
  `campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '校区编码',
  `floor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼层编码',
  `room_id` bigint NULL DEFAULT NULL COMMENT '房间ID',
  `room_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '房间编码',
  `bed_id` bigint NULL DEFAULT NULL COMMENT '床位ID',
  `bed_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '床位编码',
  `apply_date` date NULL DEFAULT NULL COMMENT '申请日期',
  `stay_start_date` date NULL DEFAULT NULL COMMENT '留宿开始日期',
  `stay_end_date` date NULL DEFAULT NULL COMMENT '留宿结束日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-待审核 2-已通过 3-已拒绝 4-已完成',
  `approval_instance_id` bigint NULL DEFAULT NULL COMMENT '审批实例ID',
  `approver_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `approver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核人姓名',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `approve_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核意见',
  `stay_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '留宿原因（文本说明）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `parent_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '家长姓名',
  `parent_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '家长电话',
  `parent_agree` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '家长是否同意：agree-同意 disagree-不同意',
  `signature` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '本人签名图片URL',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '附件图片列表（JSON字符串数组）',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_student_no`(`student_no` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_stay_start_date`(`stay_start_date` ASC) USING BTREE,
  INDEX `idx_stay_end_date`(`stay_end_date` ASC) USING BTREE,
  INDEX `idx_bed_id`(`bed_id` ASC) USING BTREE,
  INDEX `idx_approval_instance`(`approval_instance_id` ASC) USING BTREE,
  INDEX `idx_student_status`(`student_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_stay_dates`(`stay_start_date` ASC, `stay_end_date` ASC) USING BTREE,
  INDEX `idx_bed`(`bed_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '留宿管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_stay
-- ----------------------------

-- ----------------------------
-- Table structure for sys_student
-- ----------------------------
DROP TABLE IF EXISTS `sys_student`;
CREATE TABLE `sys_student`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学号',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别：male-男 female-女',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `nation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '民族',
  `political_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '政治面貌',
  `enrollment_year` int NULL DEFAULT NULL COMMENT '入学年份',
  `schooling_length` int NULL DEFAULT NULL COMMENT '学制（年）',
  `current_grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前年级',
  `academic_status` int NULL DEFAULT NULL COMMENT '学籍状态：1-在读 2-休学 3-退学 4-毕业',
  `home_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '家庭地址',
  `emergency_contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '紧急联系人电话',
  `parent_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '家长姓名',
  `parent_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '家长电话',
  `campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '校区编码',
  `dept_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '院系编码',
  `major_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业编码',
  `class_id` bigint NULL DEFAULT NULL COMMENT '班级ID',
  `class_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级编码',
  `floor_id` bigint NULL DEFAULT NULL COMMENT '楼层ID',
  `floor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼层编码',
  `room_id` bigint NULL DEFAULT NULL COMMENT '房间ID',
  `room_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '房间编码',
  `bed_id` bigint NULL DEFAULT NULL COMMENT '床位ID',
  `bed_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '床位编码',
  `smoking_status` tinyint NULL DEFAULT 0 COMMENT '吸烟状态：0不吸烟 1吸烟',
  `smoking_tolerance` tinyint NULL DEFAULT 0 COMMENT '是否接受室友吸烟：0不接受 1接受',
  `sleep_schedule` tinyint NULL DEFAULT 1 COMMENT '作息时间：0早睡早起(22:00-6:00) 1正常(23:00-7:00) 2晚睡晚起(24:00-8:00) 3夜猫子(01:00-9:00)',
  `sleep_quality` tinyint NULL DEFAULT 1 COMMENT '睡眠质量：0浅睡易醒 1正常 2深睡',
  `snores` tinyint NULL DEFAULT 0 COMMENT '是否打呼噜：0不打 1打',
  `sensitive_to_light` tinyint NULL DEFAULT 0 COMMENT '是否对光线敏感：0不敏感 1敏感',
  `sensitive_to_sound` tinyint NULL DEFAULT 0 COMMENT '是否对声音敏感：0不敏感 1敏感',
  `cleanliness_level` tinyint NULL DEFAULT 2 COMMENT '整洁程度：1非常整洁 2整洁 3一般 4随意 5不整洁',
  `bedtime_cleanup` tinyint NULL DEFAULT 1 COMMENT '睡前是否整理：0不整理 1偶尔整理 2经常整理 3总是整理',
  `social_preference` tinyint NULL DEFAULT 1 COMMENT '社交偏好：1喜欢安静 2中等 3喜欢热闹',
  `allow_visitors` tinyint NULL DEFAULT 1 COMMENT '是否允许室友带访客：0不允许 1偶尔可以 2可以',
  `phone_call_time` tinyint NULL DEFAULT 1 COMMENT '电话时间偏好：0喜欢在宿舍打电话 1偶尔在宿舍 2不在宿舍打电话',
  `study_in_room` tinyint NULL DEFAULT 1 COMMENT '是否在宿舍学习：0不在 1偶尔 2经常 3总是',
  `study_environment` tinyint NULL DEFAULT 1 COMMENT '学习环境偏好：1需要安静 2需要轻音乐 3可以接受声音',
  `computer_usage_time` tinyint NULL DEFAULT 2 COMMENT '电脑使用时间：0不用 1很少(1-2h/天) 2正常(3-5h/天) 3很多(6h+/天)',
  `gaming_preference` tinyint NULL DEFAULT 1 COMMENT '游戏偏好：0不玩游戏 1偶尔玩 2经常玩',
  `music_preference` tinyint NULL DEFAULT 1 COMMENT '听音乐偏好：0不听 1偶尔听 2经常听',
  `music_volume` tinyint NULL DEFAULT 1 COMMENT '音乐音量偏好：1喜欢小声 2中等 3喜欢大声',
  `eat_in_room` tinyint NULL DEFAULT 1 COMMENT '是否在宿舍吃东西：0不吃 1偶尔 2经常',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-正常 2-休学 3-退学 4-毕业',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码（加密）',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信openid',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_no`(`student_no` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_student_name`(`student_name` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_id_card`(`id_card` ASC) USING BTREE,
  INDEX `idx_campus_code`(`campus_code` ASC) USING BTREE,
  INDEX `idx_dept_code`(`dept_code` ASC) USING BTREE,
  INDEX `idx_major_code`(`major_code` ASC) USING BTREE,
  INDEX `idx_class_id`(`class_id` ASC) USING BTREE,
  INDEX `idx_bed_id`(`bed_id` ASC) USING BTREE,
  INDEX `idx_status_year`(`status` ASC, `enrollment_year` ASC) USING BTREE,
  INDEX `idx_class`(`class_id` ASC) USING BTREE,
  INDEX `idx_major`(`major_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32001 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_student
-- ----------------------------

-- ----------------------------
-- Table structure for sys_transfer
-- ----------------------------
DROP TABLE IF EXISTS `sys_transfer`;
CREATE TABLE `sys_transfer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生姓名（冗余）',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号（冗余）',
  `original_campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原校区编码',
  `original_floor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原楼层编码',
  `original_room_id` bigint NULL DEFAULT NULL COMMENT '原房间ID',
  `original_room_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原房间编码',
  `original_bed_id` bigint NULL DEFAULT NULL COMMENT '原床位ID',
  `original_bed_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原床位编码',
  `target_campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标校区编码',
  `target_floor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标楼层编码',
  `target_room_id` bigint NULL DEFAULT NULL COMMENT '目标房间ID',
  `target_room_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标房间编码',
  `target_bed_id` bigint NULL DEFAULT NULL COMMENT '目标床位ID',
  `target_bed_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标床位编码',
  `apply_date` date NULL DEFAULT NULL COMMENT '申请日期',
  `transfer_date` date NULL DEFAULT NULL COMMENT '调宿日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-待审核 2-已通过 3-已拒绝 4-已完成',
  `approval_instance_id` bigint NULL DEFAULT NULL COMMENT '审批实例ID',
  `approver_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `approver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核人姓名',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `approve_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核意见',
  `transfer_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '调宿原因（文本说明）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_student_no`(`student_no` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_transfer_date`(`transfer_date` ASC) USING BTREE,
  INDEX `idx_original_bed_id`(`original_bed_id` ASC) USING BTREE,
  INDEX `idx_target_bed_id`(`target_bed_id` ASC) USING BTREE,
  INDEX `idx_approval_instance`(`approval_instance_id` ASC) USING BTREE,
  INDEX `idx_student_status`(`student_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_apply_date`(`apply_date` ASC) USING BTREE,
  INDEX `idx_from_bed`(`original_bed_id` ASC) USING BTREE,
  INDEX `idx_to_bed`(`target_bed_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '调宿管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_transfer
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `manage_scope` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理范围',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-停用 1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `gender` int NULL DEFAULT NULL COMMENT '性别：1-男 2-女 0-未知',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `introduction` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人介绍',
  `cp_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业微信ID',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信openid',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_college`(`manage_scope` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'superAdmin', '$2a$10$LGpvVk9hFrfIIVRnWHoRVe.FkSVbqJ0CtyrY/WPLUva9e6xU7b/Ta', '超级管理员', NULL, 'superAdmin@example.com', '17876648229', '{\"campusIds\":[1],\"departmentIds\":[1],\"majorIds\":[1],\"classIds\":[2,1]}', 1, '2025-12-30 17:19:05', NULL, '2026-01-21 19:48:52', 2, '2026-02-05 22:52:10', 1, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_user` VALUES (2, 'testuser', '$2a$10$LGpvVk9hFrfIIVRnWHoRVe.FkSVbqJ0CtyrY/WPLUva9e6xU7b/Ta', '测试用户', NULL, 'test@example.com', '13800138000', '', 1, '2025-12-31 11:41:22', NULL, '2026-01-03 16:43:14', 1, '2026-01-26 19:14:33', NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_user` VALUES (3, 'testminuser', '$2a$10$UbN1rmnnbg/b5tybLDza0.8i0PGS4xNcVKEBHk7Fjp3USv88RdcFK', '测试小用户', NULL, '', '17877778888', NULL, 1, '2026-01-30 17:58:06', 1, '2026-01-30 17:58:15', 1, NULL, NULL, NULL, NULL, NULL, NULL, 1);

-- ----------------------------
-- Table structure for sys_user_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_menu`;
CREATE TABLE `sys_user_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_menu`(`user_id` ASC, `menu_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_menu_id`(`menu_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_menu`(`menu_id` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE,
  INDEX `idx_user_deleted`(`user_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_menu_deleted`(`menu_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2105 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_menu
-- ----------------------------
INSERT INTO `sys_user_menu` VALUES (878, 2, 1, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (879, 2, 2, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (880, 2, 125, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (881, 2, 126, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (882, 2, 127, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (883, 2, 128, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (884, 2, 129, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (885, 2, 130, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (886, 2, 131, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (887, 2, 132, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (888, 2, 133, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (889, 2, 134, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (890, 2, 135, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (891, 2, 136, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (892, 2, 137, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (893, 2, 138, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (894, 2, 139, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (895, 2, 140, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (896, 2, 141, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (897, 2, 90, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (898, 2, 97, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (899, 2, 100, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (900, 2, 101, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (901, 2, 102, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (902, 2, 103, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (903, 2, 104, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (904, 2, 105, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (905, 2, 118, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (906, 2, 98, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (907, 2, 106, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (908, 2, 107, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (909, 2, 108, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (910, 2, 109, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (911, 2, 110, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (912, 2, 111, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (913, 2, 119, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (914, 2, 99, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (915, 2, 112, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (916, 2, 113, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (917, 2, 114, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (918, 2, 115, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (919, 2, 116, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (920, 2, 117, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (921, 2, 142, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (922, 2, 143, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (923, 2, 144, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (924, 2, 145, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (925, 2, 88, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (926, 2, 68, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (927, 2, 58, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (928, 2, 59, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (929, 2, 60, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (930, 2, 69, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (931, 2, 61, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (932, 2, 62, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (933, 2, 63, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (934, 2, 70, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (935, 2, 64, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (936, 2, 65, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (937, 2, 66, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (938, 2, 50, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (939, 2, 67, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (940, 2, 55, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (941, 2, 56, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (942, 2, 57, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (943, 2, 89, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (944, 2, 94, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (945, 2, 95, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (946, 2, 96, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (947, 2, 3, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (948, 2, 4, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (949, 2, 16, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (950, 2, 17, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (951, 2, 18, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (952, 2, 19, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (953, 2, 87, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (954, 2, 124, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (955, 2, 5, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (956, 2, 37, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (957, 2, 38, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (958, 2, 39, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (959, 2, 40, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (960, 2, 6, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (961, 2, 41, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (962, 2, 42, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (963, 2, 43, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (964, 2, 15, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (965, 2, 44, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (966, 2, 45, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (967, 2, 46, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (968, 2, 47, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (969, 2, 48, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (970, 2, 49, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (971, 2, 7, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (972, 2, 83, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (973, 2, 84, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (974, 2, 85, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (975, 2, 86, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (976, 2, 8, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (977, 2, 9, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (978, 2, 10, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (979, 2, 11, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (980, 2, 12, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (981, 2, 13, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (982, 2, 14, '2026-01-21 20:12:27', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (1099, 3, 1, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1100, 3, 2, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1101, 3, 3, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1102, 3, 4, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1103, 3, 5, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1104, 3, 6, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1105, 3, 7, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1106, 3, 8, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1107, 3, 9, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1108, 3, 10, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1109, 3, 11, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1110, 3, 12, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1111, 3, 13, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1112, 3, 14, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1113, 3, 15, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1114, 3, 16, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1115, 3, 17, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1116, 3, 18, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1117, 3, 19, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1118, 3, 37, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1119, 3, 38, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1120, 3, 39, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1121, 3, 40, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1122, 3, 41, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1123, 3, 42, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1124, 3, 43, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1125, 3, 44, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1126, 3, 45, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1127, 3, 46, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1128, 3, 47, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1129, 3, 48, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1130, 3, 49, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1131, 3, 50, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1132, 3, 55, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1133, 3, 56, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1134, 3, 57, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1135, 3, 58, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1136, 3, 59, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1137, 3, 60, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1138, 3, 61, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1139, 3, 62, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1140, 3, 63, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1141, 3, 64, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1142, 3, 65, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1143, 3, 66, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1144, 3, 67, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1145, 3, 68, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1146, 3, 69, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1147, 3, 70, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1148, 3, 83, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1149, 3, 84, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1150, 3, 85, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1151, 3, 86, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1152, 3, 87, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1153, 3, 88, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1154, 3, 89, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1155, 3, 90, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1156, 3, 94, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1157, 3, 95, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1158, 3, 96, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1159, 3, 97, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1160, 3, 98, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1161, 3, 99, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1162, 3, 100, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1163, 3, 101, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1164, 3, 102, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1165, 3, 103, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1166, 3, 104, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1167, 3, 105, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1168, 3, 106, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1169, 3, 107, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1170, 3, 108, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1171, 3, 109, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1172, 3, 110, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1173, 3, 111, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1174, 3, 112, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1175, 3, 113, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1176, 3, 114, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1177, 3, 115, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1178, 3, 116, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1179, 3, 117, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1180, 3, 118, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1181, 3, 119, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1182, 3, 124, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1183, 3, 125, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1184, 3, 126, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1185, 3, 127, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1186, 3, 128, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1187, 3, 129, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1188, 3, 130, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1189, 3, 131, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1190, 3, 132, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1191, 3, 133, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1192, 3, 134, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1193, 3, 135, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1194, 3, 136, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1195, 3, 137, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1196, 3, 138, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1197, 3, 139, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1198, 3, 140, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1199, 3, 141, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1200, 3, 142, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1201, 3, 143, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1202, 3, 144, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1203, 3, 145, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1839, 1, 1, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1840, 1, 2, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1841, 1, 125, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1842, 1, 130, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1843, 1, 131, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1844, 1, 132, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1845, 1, 133, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1846, 1, 134, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1847, 1, 135, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1848, 1, 136, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1849, 1, 137, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1850, 1, 138, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1851, 1, 139, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1852, 1, 140, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1853, 1, 141, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1854, 1, 126, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1855, 1, 90, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1856, 1, 97, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1857, 1, 100, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1858, 1, 101, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1859, 1, 102, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1860, 1, 103, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1861, 1, 104, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1862, 1, 105, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1863, 1, 118, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1864, 1, 98, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1865, 1, 106, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1866, 1, 107, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1867, 1, 108, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1868, 1, 109, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1869, 1, 110, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1870, 1, 111, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1871, 1, 119, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1872, 1, 99, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1873, 1, 112, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1874, 1, 113, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1875, 1, 114, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1876, 1, 115, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1877, 1, 116, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1878, 1, 117, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1879, 1, 152, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1880, 1, 153, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1881, 1, 154, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1882, 1, 155, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1883, 1, 156, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1884, 1, 157, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1885, 1, 158, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1886, 1, 159, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1887, 1, 160, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1888, 1, 161, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1889, 1, 162, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1890, 1, 163, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1891, 1, 164, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1892, 1, 165, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1893, 1, 166, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1894, 1, 167, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1895, 1, 168, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1896, 1, 169, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1897, 1, 170, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1898, 1, 171, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1899, 1, 172, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1900, 1, 173, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1901, 1, 174, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1902, 1, 146, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1903, 1, 147, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1904, 1, 148, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1905, 1, 149, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1906, 1, 150, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1907, 1, 151, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1908, 1, 142, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1909, 1, 143, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1910, 1, 144, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1911, 1, 145, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1912, 1, 50, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1913, 1, 67, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1914, 1, 55, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1915, 1, 56, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1916, 1, 57, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1917, 1, 89, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1918, 1, 94, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1919, 1, 95, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1920, 1, 96, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1921, 1, 88, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1922, 1, 68, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1923, 1, 58, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1924, 1, 59, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1925, 1, 60, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1926, 1, 69, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1927, 1, 61, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1928, 1, 62, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1929, 1, 63, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1930, 1, 70, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1931, 1, 64, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1932, 1, 65, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1933, 1, 66, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1934, 1, 3, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1935, 1, 4, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1936, 1, 16, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1937, 1, 17, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1938, 1, 18, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1939, 1, 19, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1940, 1, 87, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1941, 1, 124, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1942, 1, 5, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1943, 1, 37, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1944, 1, 38, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1945, 1, 39, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1946, 1, 40, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1947, 1, 6, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1948, 1, 41, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1949, 1, 42, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1950, 1, 43, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1951, 1, 15, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1952, 1, 44, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1953, 1, 45, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1954, 1, 46, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1955, 1, 47, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1956, 1, 48, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1957, 1, 49, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1958, 1, 7, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1959, 1, 83, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1960, 1, 84, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1961, 1, 85, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1962, 1, 86, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1963, 1, 8, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1964, 1, 9, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1965, 1, 10, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1966, 1, 11, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1967, 1, 12, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1968, 1, 13, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1969, 1, 14, '2026-02-03 23:23:27', '2026-02-03 23:23:27', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (1970, 1, 1, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1971, 1, 2, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1972, 1, 125, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1973, 1, 130, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1974, 1, 131, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1975, 1, 132, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1976, 1, 133, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1977, 1, 134, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1978, 1, 135, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1979, 1, 136, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1980, 1, 137, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1981, 1, 138, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1982, 1, 139, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1983, 1, 140, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1984, 1, 141, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1985, 1, 126, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1986, 1, 177, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1987, 1, 127, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1988, 1, 128, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1989, 1, 129, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1990, 1, 90, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1991, 1, 97, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1992, 1, 100, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1993, 1, 101, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1994, 1, 102, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1995, 1, 103, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1996, 1, 104, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1997, 1, 105, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1998, 1, 118, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (1999, 1, 98, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2000, 1, 106, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2001, 1, 107, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2002, 1, 108, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2003, 1, 109, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2004, 1, 110, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2005, 1, 111, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2006, 1, 119, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2007, 1, 99, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2008, 1, 112, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2009, 1, 113, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2010, 1, 114, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2011, 1, 115, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2012, 1, 116, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2013, 1, 117, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2014, 1, 152, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2015, 1, 153, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2016, 1, 154, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2017, 1, 155, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2018, 1, 156, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2019, 1, 157, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2020, 1, 158, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2021, 1, 159, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2022, 1, 160, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2023, 1, 161, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2024, 1, 162, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2025, 1, 163, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2026, 1, 164, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2027, 1, 165, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2028, 1, 166, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2029, 1, 167, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2030, 1, 168, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2031, 1, 169, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2032, 1, 170, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2033, 1, 171, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2034, 1, 172, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2035, 1, 173, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2036, 1, 174, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2037, 1, 146, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2038, 1, 147, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2039, 1, 148, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2040, 1, 149, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2041, 1, 150, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2042, 1, 151, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2043, 1, 142, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2044, 1, 143, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2045, 1, 144, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2046, 1, 145, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2047, 1, 50, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2048, 1, 67, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2049, 1, 55, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2050, 1, 56, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2051, 1, 57, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2052, 1, 89, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2053, 1, 94, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2054, 1, 95, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2055, 1, 96, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2056, 1, 88, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2057, 1, 68, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2058, 1, 58, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2059, 1, 59, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2060, 1, 60, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2061, 1, 69, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2062, 1, 61, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2063, 1, 62, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2064, 1, 63, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2065, 1, 70, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2066, 1, 64, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2067, 1, 65, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2068, 1, 66, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2069, 1, 3, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2070, 1, 4, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2071, 1, 16, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2072, 1, 17, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2073, 1, 18, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2074, 1, 19, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2075, 1, 87, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2076, 1, 124, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2077, 1, 5, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2078, 1, 37, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2079, 1, 38, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2080, 1, 39, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2081, 1, 40, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2082, 1, 6, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2083, 1, 41, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2084, 1, 42, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2085, 1, 43, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2086, 1, 15, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2087, 1, 44, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2088, 1, 45, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2089, 1, 46, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2090, 1, 47, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2091, 1, 48, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2092, 1, 49, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2093, 1, 7, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2094, 1, 83, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2095, 1, 84, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2096, 1, 85, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2097, 1, 86, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2098, 1, 8, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2099, 1, 9, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2100, 1, 10, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2101, 1, 11, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2102, 1, 12, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2103, 1, 13, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2104, 1, 14, '2026-02-03 23:23:54', '2026-02-03 23:23:54', 1, 1, 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_role`(`role_id` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE,
  INDEX `idx_user_deleted`(`user_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_role_deleted`(`role_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 82 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (76, 2, 3, '2026-01-03 16:43:14', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_role` VALUES (77, 2, 6, '2026-01-03 16:43:14', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_role` VALUES (80, 1, 1, '2026-01-21 19:48:52', NULL, NULL, NULL, 0);
INSERT INTO `sys_user_role` VALUES (81, 3, 6, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
