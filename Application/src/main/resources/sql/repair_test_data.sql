-- =============================================
-- 报修功能测试数据（表结构已去除 student_name/student_no/room_code/bed_code 冗余字段）
-- @author 陈鸿昇
-- @since 2026-01-29
-- =============================================

-- 先删除本脚本插入的测试数据，避免重复执行报错/重复数据
DELETE FROM `sys_repair`
WHERE `student_id` IN (1, 2, 3)
  AND `fault_description` IN (
    '水龙头漏水，需要维修',
    '门锁损坏，无法正常开关',
    '椅子松动，需要加固',
    '网络无法连接，显示DNS错误',
    '其他问题'
  );

-- 插入测试报修数据
INSERT INTO `sys_repair` (
    `student_id`, `room_id`, `bed_id`,
    `repair_type`, `fault_description`, `fault_images`,
    `urgent_level`, `status`,
    `create_time`, `update_time`
) VALUES
-- 测试数据1：待接单的水电报修
(
    1, 1, 1,
    1, '水龙头漏水，需要维修', '["image1.jpg", "image2.jpg"]',
    2, 1,
    NOW(), NOW()
),
-- 测试数据2：已接单的门窗报修
(
    2, 2, 5,
    2, '门锁损坏，无法正常开关', '["image3.jpg"]',
    3, 2,
    NOW(), NOW()
),
-- 测试数据3：已完成的家具报修
(
    3, 3, 9,
    3, '椅子松动，需要加固', NULL,
    1, 4,
    NOW(), NOW()
),
-- 测试数据4：网络问题
(
    1, 1, 1,
    4, '网络无法连接，显示DNS错误', '["screenshot.jpg"]',
    2, 1,
    NOW(), NOW()
),
-- 测试数据5：已取消的报修
(
    2, 2, 5,
    5, '其他问题', NULL,
    1, 5,
    NOW(), NOW()
);

-- 更新已接单的报修记录（添加维修人员信息）
UPDATE `sys_repair`
SET
    `repair_person_id` = 10,
    `repair_person_name` = '维修师傅'
WHERE `status` = 2;

-- 更新已完成的报修记录（添加完成信息）
UPDATE `sys_repair`
SET
    `repair_person_id` = 10,
    `repair_person_name` = '维修师傅',
    `repair_result` = '已更换新椅子，问题已解决',
    `complete_time` = NOW(),
    `rating` = 5,
    `rating_comment` = '维修及时，服务态度好'
WHERE `status` = 4;

-- 查询验证
SELECT
    id,
    student_id,
    room_id,
    bed_id,
    repair_type,
    status,
    urgent_level,
    fault_description,
    repair_person_name
FROM `sys_repair`
ORDER BY create_time DESC;
