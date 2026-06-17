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

 Date: 15/06/2026 23:23:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典编码',
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典标签',
  `value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典值',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'CSS类名',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认：1是 0否',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-停用 1-正常',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dict_code`(`dict_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort`(`sort` ASC) USING BTREE,
  INDEX `idx_type_sort`(`dict_code` ASC, `sort` ASC) USING BTREE,
  INDEX `idx_dict_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 328 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `sys_dict_data` VALUES (25, 'sys_oper_business_type', '其它', '0', '', 'info', 1, 0, 1, '其它操作', '2026-01-01 20:40:03', NULL, '2026-01-01 20:40:03', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (26, 'sys_oper_business_type', '新增', '1', '', 'success', 2, 0, 1, '新增操作', '2026-01-01 20:40:03', NULL, '2026-01-01 20:40:03', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (27, 'sys_oper_business_type', '修改', '2', '', 'warning', 3, 0, 1, '修改操作', '2026-01-01 20:40:03', NULL, '2026-01-01 20:40:03', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (28, 'sys_oper_business_type', '删除', '3', '', 'danger', 4, 0, 1, '删除操作', '2026-01-01 20:40:03', NULL, '2026-01-01 20:40:03', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (29, 'sys_device_type', '桌面设备', '1', '', 'primary', 1, 1, 1, '桌面设备（PC、笔记本等）', '2026-01-01 20:40:14', NULL, '2026-01-01 20:40:14', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (30, 'sys_device_type', '移动设备', '2', '', 'success', 2, 0, 1, '移动设备（手机、平板等）', '2026-01-01 20:40:14', NULL, '2026-01-01 20:40:14', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (31, 'sys_device_type', '爬虫/Bot', '3', '', 'info', 3, 0, 1, '爬虫/Bot（搜索引擎、API调用等）', '2026-01-01 20:40:14', NULL, '2026-01-01 20:40:14', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (32, 'sys_user_online_status', '在线', '1', NULL, 'success', 1, 0, 1, NULL, '2026-01-02 07:38:42', NULL, '2026-01-02 07:38:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (33, 'sys_user_online_status', '离线', '0', NULL, 'info', 2, 0, 1, NULL, '2026-01-02 07:38:42', NULL, '2026-01-02 07:38:42', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (211, 'table_button_config', '绑定', 'link', 'bg-info/12 text-info', '', 8, 0, 1, 'ri:link', '2026-01-21 19:25:35', 1, '2026-01-21 20:10:31', 1, 0);
INSERT INTO `sys_dict_data` VALUES (263, 'notice_status', '草稿', '0', NULL, NULL, 0, 0, 1, '草稿状态', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (264, 'notice_status', '已发布', '1', NULL, NULL, 1, 0, 1, '已发布状态', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (319, 'table_button_config', '复制', 'copy', 'bg-theme/12 text-theme', '', 9, 0, 1, 'ri:file-copy-2-line', '2026-02-03 11:08:29', 1, '2026-02-03 11:08:46', 1, 0);
INSERT INTO `sys_dict_data` VALUES (321, 'sys_common_status', '正常', '1', NULL, 'success', 1, 1, 1, '正常/启用状态', '2026-02-03 20:00:00', NULL, '2026-02-03 20:00:00', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (322, 'sys_common_status', '停用', '0', NULL, 'danger', 2, 0, 1, '停用/禁用状态', '2026-02-03 20:00:00', NULL, '2026-02-03 20:00:00', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (323, 'table_button_config', '提醒', 'notify', NULL, NULL, 10, 0, 1, NULL, '2026-02-06 22:36:16', NULL, '2026-02-06 22:36:16', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (326, 'table_button_config', '执行', 'play', 'bg-theme/12 text-theme', '', 11, 0, 1, 'ri:play-circle-line', '2026-02-08 03:27:06', NULL, '2026-02-08 03:27:06', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (327, 'table_button_config', '取消', 'cancel', 'bg-error/12 text-error', '', 12, 0, 1, 'ri:close-circle-line', '2026-02-08 03:27:10', NULL, '2026-02-08 03:27:10', NULL, 0);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典名称',
  `dict_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典编码',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-停用 1-正常',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_code`(`dict_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 74 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', 1, '用户性别字典', '2025-12-31 14:42:00', NULL, '2025-12-31 20:37:55', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (2, '用户状态', 'sys_user_status', 1, '用户状态字典', '2025-12-31 14:42:00', NULL, '2025-12-31 14:42:00', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_switch', 1, '系统开关字典', '2025-12-31 14:42:00', NULL, '2025-12-31 14:42:00', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (5, '表格按钮配置', 'table_button_config', 1, '表格操作按钮配置，包含图标、文字、样式等信息', '2026-01-01 07:00:53', NULL, '2026-01-01 07:00:53', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (8, '操作日志业务类型', 'sys_oper_business_type', 1, '操作日志业务类型：0其它 1新增 2修改 3删除', '2026-01-01 20:40:02', NULL, '2026-01-01 20:40:02', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (9, '设备类型', 'sys_device_type', 1, '设备类型：1桌面设备 2移动设备 3爬虫/Bot', '2026-01-01 20:40:14', NULL, '2026-01-01 20:40:14', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (10, '用户在线状态', 'sys_user_online_status', 1, '用户在线状态字典', '2026-01-02 07:38:41', NULL, '2026-01-02 07:38:41', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (58, '通知状态', 'notice_status', 1, '通知公告的状态', '2026-01-29 16:46:29', NULL, '2026-01-29 16:46:29', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (72, '系统状态', 'sys_common_status', 1, '通用的启用/停用状态，用于系统各模块', '2026-02-03 20:00:00', NULL, '2026-02-03 20:00:00', NULL, 0);

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `login_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录类型：password-密码登录',
  `login_status` int NOT NULL COMMENT '登录状态：0-失败 1-成功 2-登出',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作系统',
  `message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提示消息',
  `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_login_type`(`login_type` ASC) USING BTREE,
  INDEX `idx_login_status`(`login_status` ASC) USING BTREE,
  INDEX `idx_login_time`(`login_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '登录日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------
INSERT INTO `sys_login_log` VALUES (12, 'superAdmin', 'password', 1, '0:0:0:0:0:0:0:1', '本地', 'MSEdge 145.0.0.0', 'Windows 10 or Windows Server 2016', '登录成功', '2026-03-08 12:31:48');
INSERT INTO `sys_login_log` VALUES (13, 'superAdmin', 'password', 1, '0:0:0:0:0:0:0:1', '本地', 'Chrome 142.0.7444.265', 'Windows 10 or Windows Server 2016', '登录成功', '2026-03-08 15:26:42');
INSERT INTO `sys_login_log` VALUES (14, 'superAdmin', 'password', 2, '0:0:0:0:0:0:0:1', '本地', 'Chrome 142.0.7444.265', 'Windows 10 or Windows Server 2016', '登出成功', '2026-03-08 15:40:43');
INSERT INTO `sys_login_log` VALUES (15, 'superAdmin', 'password', 1, '0:0:0:0:0:0:0:1', '本地', 'Chrome 142.0.7444.265', 'Windows 10 or Windows Server 2016', '登录成功', '2026-03-08 15:40:45');
INSERT INTO `sys_login_log` VALUES (16, 'superAdmin', 'password', 2, '0:0:0:0:0:0:0:1', '本地', 'Chrome 142.0.7444.265', 'Windows 10 or Windows Server 2016', '登出成功', '2026-03-08 15:48:41');
INSERT INTO `sys_login_log` VALUES (17, 'superAdmin', 'password', 1, '0:0:0:0:0:0:0:1', '本地', 'Chrome 142.0.7444.265', 'Windows 10 or Windows Server 2016', '登录成功', '2026-03-08 15:48:43');
INSERT INTO `sys_login_log` VALUES (18, 'superAdmin', 'password', 2, '0:0:0:0:0:0:0:1', '本地', 'Chrome 142.0.7444.265', 'Windows 10 or Windows Server 2016', '登出成功', '2026-03-09 04:25:15');
INSERT INTO `sys_login_log` VALUES (19, 'superAdmin', 'password', 1, '0:0:0:0:0:0:0:1', '本地', 'Chrome 142.0.7444.265', 'Windows 10 or Windows Server 2016', '登录成功', '2026-03-09 04:25:18');
INSERT INTO `sys_login_log` VALUES (20, 'superAdmin', 'password', 1, '0:0:0:0:0:0:0:1', '本地', 'MSEdge 147.0.0.0', 'Windows 10 or Windows Server 2016', '登录成功', '2026-04-27 00:38:37');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单类型：M-目录 C-菜单 B-按钮',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `visible` tinyint NULL DEFAULT 1 COMMENT '是否可见：0-隐藏 1-显示',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-停用 1-显示',
  `keep_alive` tinyint NULL DEFAULT 1 COMMENT '是否缓存：0-否 1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 184 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统菜单表' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `sys_menu` VALUES (87, 4, '分配管理', 'F', '', '', 'system:user:assign-permission', '', 5, 1, 1, 1, '2026-01-02 17:57:21', 1, '2026-03-06 03:51:40', 1, 0);
INSERT INTO `sys_menu` VALUES (89, 50, '学年管理', 'C', 'academic-year', '/school/academic-year', NULL, '', 2, 1, 1, 1, '2026-01-03 12:45:29', NULL, '2026-01-03 20:56:04', NULL, 0);
INSERT INTO `sys_menu` VALUES (148, 147, '新增通知', 'F', NULL, NULL, 'system:notice:add', NULL, 1, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 18:12:31', NULL, 0);
INSERT INTO `sys_menu` VALUES (149, 147, '编辑通知', 'F', NULL, NULL, 'system:notice:edit', NULL, 2, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 18:12:33', NULL, 0);
INSERT INTO `sys_menu` VALUES (150, 147, '删除通知', 'F', NULL, NULL, 'system:notice:delete', NULL, 3, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 18:12:35', NULL, 0);
INSERT INTO `sys_menu` VALUES (151, 147, '发布/下架', 'F', NULL, NULL, 'system:notice:publish', NULL, 4, 1, 1, 1, '2026-01-29 18:38:02', 1, '2026-01-31 18:12:37', NULL, 0);
INSERT INTO `sys_menu` VALUES (179, 4, '导入用户', 'F', '', '', 'system:user:import', '', 6, 1, 1, 1, '2026-03-06 05:43:08', 1, '2026-03-06 05:43:08', 1, 0);
INSERT INTO `sys_menu` VALUES (180, 3, '登入日志', 'C', 'login-log', '/system/login-log', 'system:loginlog:view', '', 7, 1, 1, 1, '2026-03-08 00:00:00', 1, '2026-03-08 00:00:00', 1, 0);
INSERT INTO `sys_menu` VALUES (181, 180, '查看详情', 'F', NULL, NULL, 'system:loginlog:detail', NULL, 1, 1, 1, 1, '2026-03-08 00:00:00', 1, '2026-03-08 00:00:00', 1, 0);
INSERT INTO `sys_menu` VALUES (182, 180, '删除日志', 'F', NULL, NULL, 'system:loginlog:delete', NULL, 2, 1, 1, 1, '2026-03-08 00:00:00', 1, '2026-03-08 00:00:00', 1, 0);
INSERT INTO `sys_menu` VALUES (183, 180, '清空日志', 'F', NULL, NULL, 'system:loginlog:clean', NULL, 3, 1, 1, 1, '2026-03-08 00:00:00', 1, '2026-03-08 00:00:00', 1, 0);

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
  `operator_type` int NULL DEFAULT 0 COMMENT '操作类别：1-后台用户',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作人员',
  `device_type` int NULL DEFAULT NULL COMMENT '设备类型：1-PC 2-手机 3-平板',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` tinyint NULL DEFAULT NULL COMMENT '操作状态：0-正常 1-异常',
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
) ENGINE = InnoDB AUTO_INCREMENT = 97 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (77, '清空操作日志', 3, 'com.project.backend.system.controller.OperLogController.clean()', 'DELETE', 1, 'superAdmin', 1, '/api/v1/system/oper-log/clean', '0:0:0:0:0:0:0:1', '', '', '{\"code\":200,\"message\":\"操作日志清空成功\",\"data\":null,\"timestamp\":1772719848076}', 0, '', '2026-03-05 22:10:48', 14);
INSERT INTO `sys_oper_log` VALUES (78, '分配角色权限', 0, 'com.project.backend.system.controller.RoleController.assignPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/role/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,146,147,148,149,150,151,3,4,16,17,18,19,87,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1772735986494}', 0, '', '2026-03-06 02:39:47', 88);
INSERT INTO `sys_oper_log` VALUES (79, '分配角色权限', 0, 'com.project.backend.system.controller.RoleController.assignPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/role/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,146,147,148,149,150,151,3,4,16,17,18,19,87,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1772736023346}', 0, '', '2026-03-06 02:40:23', 48);
INSERT INTO `sys_oper_log` VALUES (80, '分配用户权限', 0, 'com.project.backend.system.controller.UserController.assignUserPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/user/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,146,147,148,149,150,151,3,4,16,17,18,19,87,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1772736608055}', 0, '', '2026-03-06 02:50:08', 56);
INSERT INTO `sys_oper_log` VALUES (81, '编辑菜单', 2, 'com.project.backend.system.controller.MenuController.update()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/menu/87', '0:0:0:0:0:0:0:1', '', '87 {\"id\":87,\"parentId\":4,\"menuName\":\"分配管理\",\"menuType\":\"F\",\"path\":\"\",\"component\":\"\",\"permission\":\"system:user:assign-permission\",\"icon\":\"\",\"sort\":5,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单编辑成功\",\"data\":null,\"timestamp\":1772740299911}', 0, '', '2026-03-06 03:51:40', 17);
INSERT INTO `sys_oper_log` VALUES (82, '新增菜单', 1, 'com.project.backend.system.controller.MenuController.add()', 'POST', 1, 'superAdmin', 1, '/api/v1/system/menu', '0:0:0:0:0:0:0:1', '', '{\"id\":null,\"parentId\":4,\"menuName\":\"导入用户\",\"menuType\":\"F\",\"path\":\"\",\"component\":\"\",\"permission\":\"system:user:import\",\"icon\":\"\",\"sort\":6,\"visible\":1,\"status\":1,\"keepAlive\":1}', '{\"code\":200,\"message\":\"菜单新增成功\",\"data\":null,\"timestamp\":1772746987935}', 0, '', '2026-03-06 05:43:08', 10);
INSERT INTO `sys_oper_log` VALUES (83, '分配角色权限', 0, 'com.project.backend.system.controller.RoleController.assignPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/role/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,146,147,148,149,150,151,3,4,16,17,18,19,87,179,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1772746997895}', 0, '', '2026-03-06 05:43:18', 50);
INSERT INTO `sys_oper_log` VALUES (84, '分配用户权限', 0, 'com.project.backend.system.controller.UserController.assignUserPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/user/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,146,147,148,149,150,151,3,4,16,17,18,19,87,179,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1772747005515}', 0, '', '2026-03-06 05:43:26', 49);
INSERT INTO `sys_oper_log` VALUES (85, '修改用户状态', 2, 'com.project.backend.system.controller.UserController.updateStatus()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/user/2/status/0', '0:0:0:0:0:0:0:1', '', '2 0', '{\"code\":200,\"message\":\"用户已停用\",\"data\":null,\"timestamp\":1772751085062}', 0, '', '2026-03-06 06:51:25', 14);
INSERT INTO `sys_oper_log` VALUES (86, '修改用户状态', 2, 'com.project.backend.system.controller.UserController.updateStatus()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/user/2/status/1', '0:0:0:0:0:0:0:1', '', '2 1', '{\"code\":200,\"message\":\"用户已启用\",\"data\":null,\"timestamp\":1772751085980}', 0, '', '2026-03-06 06:51:26', 8);
INSERT INTO `sys_oper_log` VALUES (87, '删除菜单', 3, 'com.project.backend.system.controller.MenuController.delete()', 'DELETE', 1, 'superAdmin', 1, '/api/v1/system/menu/146', '0:0:0:0:0:0:0:1', '', '146', '', 1, '\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'1-146-1\' for key \'sys_role_menu.uk_role_menu\'\r\n### The error may exist in com/project/backend/system/mapper/RoleMenuMapper.java (best guess)\r\n### The error may involve com.project.backend.system.mapper.RoleMenuMapper.delete-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE sys_role_menu SET deleted=1  WHERE deleted=0     AND (menu_id = ?)\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'1-146-1\' for key \'sys_role_menu.uk_role_menu\'\n; Duplicate entry \'1-146-1\' for key \'sys_role_menu.uk_role_menu\'', '2026-03-08 12:12:20', 147);
INSERT INTO `sys_oper_log` VALUES (88, '分配角色权限', 0, 'com.project.backend.system.controller.RoleController.assignPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/role/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,3,4,16,17,18,19,87,179,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,180,181,182,183,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1772943256843}', 0, '', '2026-03-08 12:14:17', 82);
INSERT INTO `sys_oper_log` VALUES (89, '分配用户权限', 0, 'com.project.backend.system.controller.UserController.assignUserPermissions()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/user/1/permissions', '0:0:0:0:0:0:0:1', '', '1 [1,2,3,4,16,17,18,19,87,179,5,37,38,39,40,6,41,42,43,15,44,45,46,47,48,49,7,83,84,85,86,180,181,182,183,8,9,10,11,12,13,14]', '{\"code\":200,\"message\":\"权限分配成功\",\"data\":null,\"timestamp\":1772943264328}', 0, '', '2026-03-08 12:14:24', 59);
INSERT INTO `sys_oper_log` VALUES (90, '清空登入日志', 3, 'com.project.backend.system.controller.LoginLogController.clean()', 'DELETE', 1, 'superAdmin', 1, '/api/v1/system/login-log/clean', '0:0:0:0:0:0:0:1', '', '', '{\"code\":200,\"message\":\"登入日志清空成功\",\"data\":null,\"timestamp\":1772943746840}', 0, '', '2026-03-08 12:22:27', 6);
INSERT INTO `sys_oper_log` VALUES (91, '清空登入日志', 3, 'com.project.backend.system.controller.LoginLogController.clean()', 'DELETE', 1, 'superAdmin', 1, '/api/v1/system/login-log/clean', '0:0:0:0:0:0:0:1', '', '', '{\"code\":200,\"message\":\"登入日志清空成功\",\"data\":null,\"timestamp\":1772944100255}', 0, '', '2026-03-08 12:28:20', 5);
INSERT INTO `sys_oper_log` VALUES (92, '批量删除登入日志', 3, 'com.project.backend.system.controller.LoginLogController.batchDelete()', 'DELETE', 1, 'superAdmin', 1, '/api/v1/system/login-log/batch', '0:0:0:0:0:0:0:1', '', '[7]', '{\"code\":200,\"message\":\"登入日志批量删除成功\",\"data\":null,\"timestamp\":1772944271507}', 0, '', '2026-03-08 12:31:12', 17);
INSERT INTO `sys_oper_log` VALUES (93, '清空登入日志', 3, 'com.project.backend.system.controller.LoginLogController.clean()', 'DELETE', 1, 'superAdmin', 1, '/api/v1/system/login-log/clean', '0:0:0:0:0:0:0:1', '', '', '{\"code\":200,\"message\":\"登入日志清空成功\",\"data\":null,\"timestamp\":1772944282134}', 0, '', '2026-03-08 12:31:22', 6);
INSERT INTO `sys_oper_log` VALUES (94, '清空登入日志', 3, 'com.project.backend.system.controller.LoginLogController.clean()', 'DELETE', 1, 'superAdmin', 1, '/api/v1/system/login-log/clean', '0:0:0:0:0:0:0:1', '', '', '{\"code\":200,\"message\":\"登入日志清空成功\",\"data\":null,\"timestamp\":1772944303036}', 0, '', '2026-03-08 12:31:43', 95);
INSERT INTO `sys_oper_log` VALUES (95, '修改角色状态', 2, 'com.project.backend.system.controller.RoleController.updateStatus()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/role/2/status/0', '0:0:0:0:0:0:0:1', '', '2 0', '{\"code\":200,\"message\":\"角色已停用\",\"data\":null,\"timestamp\":1772956553435}', 0, '', '2026-03-08 15:55:53', 28);
INSERT INTO `sys_oper_log` VALUES (96, '修改角色状态', 2, 'com.project.backend.system.controller.RoleController.updateStatus()', 'PUT', 1, 'superAdmin', 1, '/api/v1/system/role/2/status/1', '0:0:0:0:0:0:0:1', '', '2 1', '{\"code\":200,\"message\":\"角色已启用\",\"data\":null,\"timestamp\":1772956554289}', 0, '', '2026-03-08 15:55:54', 6);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-停用 1-正常',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统角色表' ROW_FORMAT = DYNAMIC;

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
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
) ENGINE = InnoDB AUTO_INCREMENT = 3260 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1954, 6, 1, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (1955, 6, 2, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2002, 6, 68, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2006, 6, 69, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2009, 6, 63, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2010, 6, 70, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2011, 6, 64, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2012, 6, 65, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2013, 6, 66, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2015, 6, 67, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2019, 6, 89, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2023, 6, 3, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2024, 6, 4, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2025, 6, 16, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2026, 6, 17, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2027, 6, 18, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2028, 6, 19, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2029, 6, 87, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2031, 6, 5, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2032, 6, 37, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2033, 6, 38, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2034, 6, 39, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2035, 6, 40, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2036, 6, 6, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2037, 6, 41, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2038, 6, 42, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2039, 6, 43, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2040, 6, 15, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2041, 6, 44, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2042, 6, 45, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2043, 6, 46, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2044, 6, 47, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2045, 6, 48, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2046, 6, 49, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2047, 6, 7, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2048, 6, 83, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2049, 6, 84, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2050, 6, 85, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2051, 6, 86, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2052, 6, 8, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2053, 6, 9, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2054, 6, 10, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2055, 6, 11, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2056, 6, 12, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2057, 6, 13, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2058, 6, 14, '2026-01-17 22:54:57', '2026-01-17 22:54:57', NULL, NULL, 0);
INSERT INTO `sys_role_menu` VALUES (3174, 1, 1, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3175, 1, 2, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3176, 1, 146, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3178, 1, 148, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3179, 1, 149, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3180, 1, 150, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3181, 1, 151, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3182, 1, 3, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3183, 1, 4, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3184, 1, 16, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3185, 1, 17, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3186, 1, 18, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3187, 1, 19, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3188, 1, 87, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3189, 1, 179, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3190, 1, 5, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3191, 1, 37, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3192, 1, 38, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3193, 1, 39, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3194, 1, 40, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3195, 1, 6, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3196, 1, 41, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3197, 1, 42, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3198, 1, 43, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3199, 1, 15, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3200, 1, 44, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3201, 1, 45, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3202, 1, 46, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3203, 1, 47, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3204, 1, 48, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3205, 1, 49, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3206, 1, 7, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3207, 1, 83, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3208, 1, 84, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3209, 1, 85, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3210, 1, 86, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3211, 1, 8, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3212, 1, 9, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3213, 1, 10, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3214, 1, 11, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3215, 1, 12, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3216, 1, 13, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3217, 1, 14, '2026-03-06 05:43:18', '2026-03-08 12:14:16', 1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (3218, 1, 1, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3219, 1, 2, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3220, 1, 3, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3221, 1, 4, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3222, 1, 16, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3223, 1, 17, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3224, 1, 18, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3225, 1, 19, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3226, 1, 87, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3227, 1, 179, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3228, 1, 5, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3229, 1, 37, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3230, 1, 38, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3231, 1, 39, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3232, 1, 40, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3233, 1, 6, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3234, 1, 41, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3235, 1, 42, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3236, 1, 43, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3237, 1, 15, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3238, 1, 44, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3239, 1, 45, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3240, 1, 46, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3241, 1, 47, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3242, 1, 48, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3243, 1, 49, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3244, 1, 7, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3245, 1, 83, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3246, 1, 84, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3247, 1, 85, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3248, 1, 86, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3249, 1, 180, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3250, 1, 181, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3251, 1, 182, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3252, 1, 183, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3253, 1, 8, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3254, 1, 9, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3255, 1, 10, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3256, 1, 11, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3257, 1, 12, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3258, 1, 13, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);
INSERT INTO `sys_role_menu` VALUES (3259, 1, 14, '2026-03-08 12:14:17', '2026-03-08 12:14:17', 1, 1, 0);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-停用 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `gender` int NULL DEFAULT NULL COMMENT '性别：1-男 2-女 0-未知',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `introduction` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人介绍',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'superAdmin', '$2a$10$LGpvVk9hFrfIIVRnWHoRVe.FkSVbqJ0CtyrY/WPLUva9e6xU7b/Ta', '超级管理员', NULL, 'superAdmin@example.com', '17876648229', 1, '2025-12-30 17:19:05', NULL, '2026-02-24 00:22:08', 1, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_user` VALUES (2, 'testuser', '$2a$10$LGpvVk9hFrfIIVRnWHoRVe.FkSVbqJ0CtyrY/WPLUva9e6xU7b/Ta', '测试用户', NULL, 'test@example.com', '13800138000', 1, '2025-12-31 11:41:22', NULL, '2026-01-03 16:43:14', 1, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_user` VALUES (3, 'testminuser', '$2a$10$UbN1rmnnbg/b5tybLDza0.8i0PGS4xNcVKEBHk7Fjp3USv88RdcFK', '测试小用户', NULL, '', '17877778888', 1, '2026-01-30 17:58:06', 1, '2026-01-30 17:58:15', 1, NULL, NULL, NULL, NULL, 1);

-- ----------------------------
-- Table structure for sys_user_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_menu`;
CREATE TABLE `sys_user_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
) ENGINE = InnoDB AUTO_INCREMENT = 2505 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_menu
-- ----------------------------
INSERT INTO `sys_user_menu` VALUES (878, 2, 1, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (879, 2, 2, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (926, 2, 68, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (930, 2, 69, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (933, 2, 63, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (934, 2, 70, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (935, 2, 64, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (936, 2, 65, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (937, 2, 66, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (939, 2, 67, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (943, 2, 89, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (947, 2, 3, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (948, 2, 4, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (949, 2, 16, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (950, 2, 17, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (951, 2, 18, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (952, 2, 19, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (953, 2, 87, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (955, 2, 5, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (956, 2, 37, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (957, 2, 38, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (958, 2, 39, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (959, 2, 40, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (960, 2, 6, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (961, 2, 41, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (962, 2, 42, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (963, 2, 43, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (964, 2, 15, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (965, 2, 44, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (966, 2, 45, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (967, 2, 46, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (968, 2, 47, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (969, 2, 48, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (970, 2, 49, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (971, 2, 7, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (972, 2, 83, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (973, 2, 84, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (974, 2, 85, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (975, 2, 86, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (976, 2, 8, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (977, 2, 9, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (978, 2, 10, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (979, 2, 11, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (980, 2, 12, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (981, 2, 13, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
INSERT INTO `sys_user_menu` VALUES (982, 2, 14, '2026-01-21 20:12:27', '2026-01-21 20:12:27', NULL, NULL, 0);
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
INSERT INTO `sys_user_menu` VALUES (1154, 3, 89, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2419, 1, 1, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2420, 1, 2, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2421, 1, 146, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2423, 1, 148, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2424, 1, 149, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2425, 1, 150, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2426, 1, 151, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2427, 1, 3, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2428, 1, 4, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2429, 1, 16, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2430, 1, 17, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2431, 1, 18, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2432, 1, 19, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2433, 1, 87, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2434, 1, 179, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2435, 1, 5, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2436, 1, 37, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2437, 1, 38, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2438, 1, 39, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2439, 1, 40, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2440, 1, 6, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2441, 1, 41, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2442, 1, 42, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2443, 1, 43, '2026-03-06 05:43:25', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2444, 1, 15, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2445, 1, 44, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2446, 1, 45, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2447, 1, 46, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2448, 1, 47, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2449, 1, 48, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2450, 1, 49, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2451, 1, 7, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2452, 1, 83, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2453, 1, 84, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2454, 1, 85, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2455, 1, 86, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2456, 1, 8, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2457, 1, 9, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2458, 1, 10, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2459, 1, 11, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2460, 1, 12, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2461, 1, 13, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2462, 1, 14, '2026-03-06 05:43:26', '2026-03-08 12:14:24', 1, 1, 1);
INSERT INTO `sys_user_menu` VALUES (2463, 1, 1, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2464, 1, 2, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2465, 1, 3, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2466, 1, 4, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2467, 1, 16, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2468, 1, 17, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2469, 1, 18, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2470, 1, 19, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2471, 1, 87, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2472, 1, 179, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2473, 1, 5, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2474, 1, 37, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2475, 1, 38, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2476, 1, 39, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2477, 1, 40, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2478, 1, 6, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2479, 1, 41, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2480, 1, 42, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2481, 1, 43, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2482, 1, 15, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2483, 1, 44, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2484, 1, 45, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2485, 1, 46, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2486, 1, 47, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2487, 1, 48, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2488, 1, 49, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2489, 1, 7, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2490, 1, 83, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2491, 1, 84, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2492, 1, 85, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2493, 1, 86, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2494, 1, 180, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2495, 1, 181, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2496, 1, 182, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2497, 1, 183, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2498, 1, 8, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2499, 1, 9, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2500, 1, 10, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2501, 1, 11, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2502, 1, 12, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2503, 1, 13, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);
INSERT INTO `sys_user_menu` VALUES (2504, 1, 14, '2026-03-08 12:14:24', '2026-03-08 12:14:24', 1, 1, 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
) ENGINE = InnoDB AUTO_INCREMENT = 83 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (76, 2, 3, '2026-01-03 16:43:14', '2026-01-03 16:43:14', NULL, NULL, 0);
INSERT INTO `sys_user_role` VALUES (77, 2, 6, '2026-01-03 16:43:14', '2026-01-03 16:43:14', NULL, NULL, 0);
INSERT INTO `sys_user_role` VALUES (80, 1, 1, '2026-01-21 19:48:52', '2026-02-24 00:22:07', NULL, NULL, 1);
INSERT INTO `sys_user_role` VALUES (81, 3, 6, '2026-01-30 17:58:06', '2026-01-30 17:58:06', 1, 1, 1);
INSERT INTO `sys_user_role` VALUES (82, 1, 1, '2026-02-24 00:22:08', '2026-02-24 00:22:08', 1, 1, 0);

SET FOREIGN_KEY_CHECKS = 1;
