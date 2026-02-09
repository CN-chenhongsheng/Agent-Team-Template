-- ============================================================
-- 床位数据生成脚本
-- 功能：
--   1. 为已有房间补全缺失的床位记录
--   2. 生成 100 栋新宿舍楼（50 男 + 50 女），每栋 6 层 × 25 间 × 6 床
--   3. 共生成约 90,000 张新床位，供 90,000 名学生分配测试
-- 使用方式：
--   mysql -u root -p project_management < generate-beds.sql
-- ============================================================

USE project_management;

SET @campus_code = 'CAMPUS001';
SET @now = NOW();
SET @operator = 1;

-- ============================================================
-- 第一部分：为已有房间补全缺失的床位
-- ============================================================

DROP PROCEDURE IF EXISTS fill_existing_room_beds;

DELIMITER $$
CREATE PROCEDURE fill_existing_room_beds()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_room_id BIGINT;
    DECLARE v_room_code VARCHAR(50);
    DECLARE v_floor_id BIGINT;
    DECLARE v_floor_code VARCHAR(50);
    DECLARE v_campus_code VARCHAR(50);
    DECLARE v_bed_count INT;
    DECLARE v_existing_beds INT;
    DECLARE v_bed_num INT;

    -- 游标：查询缺少床位记录的房间
    DECLARE cur CURSOR FOR
        SELECT r.id, r.room_code, r.floor_id, r.floor_code, r.campus_code, r.bed_count,
               IFNULL((SELECT COUNT(*) FROM sys_bed b WHERE b.room_id = r.id AND b.deleted = 0), 0) AS existing_beds
        FROM sys_room r
        WHERE r.deleted = 0
        HAVING existing_beds < r.bed_count;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_room_id, v_room_code, v_floor_id, v_floor_code, v_campus_code, v_bed_count, v_existing_beds;
        IF done THEN LEAVE read_loop; END IF;

        SET v_bed_num = v_existing_beds + 1;
        WHILE v_bed_num <= v_bed_count DO
            INSERT INTO sys_bed (bed_code, bed_number, room_id, room_code, floor_id, floor_code,
                                 campus_code, bed_status, sort, status, deleted,
                                 create_time, update_time, create_by, update_by)
            VALUES (
                CONCAT(v_room_code, '-', v_bed_num),
                v_bed_num,
                v_room_id,
                v_room_code,
                v_floor_id,
                v_floor_code,
                v_campus_code,
                1,          -- bed_status = 空闲
                v_bed_num,  -- sort
                1,          -- status = 启用
                0,          -- deleted = 未删除
                @now, @now, @operator, @operator
            );
            SET v_bed_num = v_bed_num + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;

CALL fill_existing_room_beds();
DROP PROCEDURE IF EXISTS fill_existing_room_beds;

SELECT CONCAT('已有房间床位补全完毕，当前 sys_bed 总数：', COUNT(*)) AS step1_result FROM sys_bed WHERE deleted = 0;


-- ============================================================
-- 第二部分：生成 100 栋新宿舍楼 + 房间 + 床位
-- 结构：
--   男生楼 M01~M50 (gender_type=1)，女生楼 W01~W50 (gender_type=2)
--   每栋楼 6 层（物理层），每层 25 间房，每间 6 床
--   每栋：150 房 × 6 床 = 900 床
--   总计：100 栋 × 900 床 = 90,000 床
-- ============================================================

DROP PROCEDURE IF EXISTS generate_dorm_data;

DELIMITER $$
CREATE PROCEDURE generate_dorm_data()
BEGIN
    DECLARE v_building_idx INT DEFAULT 1;
    DECLARE v_gender_type INT;
    DECLARE v_prefix VARCHAR(5);
    DECLARE v_floor_code VARCHAR(10);
    DECLARE v_floor_name VARCHAR(50);
    DECLARE v_floor_id BIGINT;
    DECLARE v_physical_floor INT;
    DECLARE v_room_per_floor INT DEFAULT 25;
    DECLARE v_room_idx INT;
    DECLARE v_room_number VARCHAR(10);
    DECLARE v_room_code VARCHAR(20);
    DECLARE v_room_id BIGINT;
    DECLARE v_bed_idx INT;
    DECLARE v_bed_code VARCHAR(30);
    DECLARE v_beds_per_room INT DEFAULT 6;
    DECLARE v_total_buildings INT DEFAULT 50; -- 每种性别 50 栋

    -- ==================== 男生宿舍楼 M01~M50 ====================
    SET v_building_idx = 1;
    WHILE v_building_idx <= v_total_buildings DO
        SET v_gender_type = 1;
        SET v_prefix = CONCAT('M', LPAD(v_building_idx, 2, '0'));
        SET v_floor_code = v_prefix;
        SET v_floor_name = CONCAT('男', v_building_idx, '号楼');

        -- 插入楼栋（sys_floor）
        INSERT INTO sys_floor (floor_code, floor_name, floor_number, campus_code, gender_type,
                               total_rooms, total_beds, current_occupancy, sort, status, deleted,
                               create_time, update_time, create_by, update_by)
        VALUES (v_floor_code, v_floor_name, v_building_idx, @campus_code, v_gender_type,
                v_room_per_floor * 6, v_room_per_floor * 6 * v_beds_per_room, 0,
                100 + v_building_idx, 1, 0,
                @now, @now, @operator, @operator);
        SET v_floor_id = LAST_INSERT_ID();

        -- 循环 6 层
        SET v_physical_floor = 1;
        WHILE v_physical_floor <= 6 DO
            -- 循环每层 25 间
            SET v_room_idx = 1;
            WHILE v_room_idx <= v_room_per_floor DO
                SET v_room_number = CONCAT(LPAD(v_physical_floor, 2, '0'), LPAD(v_room_idx, 2, '0'));
                SET v_room_code = CONCAT(v_floor_code, '-', v_room_number);

                -- 插入房间（sys_room）
                INSERT INTO sys_room (room_code, room_number, floor_id, floor_number, floor_code,
                                      campus_code, room_type, bed_count, current_occupancy,
                                      max_occupancy, room_status, sort, status, deleted,
                                      create_time, update_time, create_by, update_by)
                VALUES (v_room_code, v_room_number, v_floor_id, v_physical_floor, v_floor_code,
                        @campus_code, 'standard', v_beds_per_room, 0,
                        v_beds_per_room, 1, (v_physical_floor - 1) * v_room_per_floor + v_room_idx,
                        1, 0,
                        @now, @now, @operator, @operator);
                SET v_room_id = LAST_INSERT_ID();

                -- 插入 6 张床位（sys_bed）
                SET v_bed_idx = 1;
                WHILE v_bed_idx <= v_beds_per_room DO
                    SET v_bed_code = CONCAT(v_room_code, '-', v_bed_idx);
                    INSERT INTO sys_bed (bed_code, bed_number, room_id, room_code,
                                         floor_id, floor_code, campus_code,
                                         bed_status, sort, status, deleted,
                                         create_time, update_time, create_by, update_by)
                    VALUES (v_bed_code, v_bed_idx, v_room_id, v_room_code,
                            v_floor_id, v_floor_code, @campus_code,
                            1, v_bed_idx, 1, 0,
                            @now, @now, @operator, @operator);
                    SET v_bed_idx = v_bed_idx + 1;
                END WHILE;

                SET v_room_idx = v_room_idx + 1;
            END WHILE;
            SET v_physical_floor = v_physical_floor + 1;
        END WHILE;

        SET v_building_idx = v_building_idx + 1;
    END WHILE;

    -- ==================== 女生宿舍楼 W01~W50 ====================
    SET v_building_idx = 1;
    WHILE v_building_idx <= v_total_buildings DO
        SET v_gender_type = 2;
        SET v_prefix = CONCAT('W', LPAD(v_building_idx, 2, '0'));
        SET v_floor_code = v_prefix;
        SET v_floor_name = CONCAT('女', v_building_idx, '号楼');

        -- 插入楼栋（sys_floor）
        INSERT INTO sys_floor (floor_code, floor_name, floor_number, campus_code, gender_type,
                               total_rooms, total_beds, current_occupancy, sort, status, deleted,
                               create_time, update_time, create_by, update_by)
        VALUES (v_floor_code, v_floor_name, v_building_idx, @campus_code, v_gender_type,
                v_room_per_floor * 6, v_room_per_floor * 6 * v_beds_per_room, 0,
                200 + v_building_idx, 1, 0,
                @now, @now, @operator, @operator);
        SET v_floor_id = LAST_INSERT_ID();

        -- 循环 6 层
        SET v_physical_floor = 1;
        WHILE v_physical_floor <= 6 DO
            -- 循环每层 25 间
            SET v_room_idx = 1;
            WHILE v_room_idx <= v_room_per_floor DO
                SET v_room_number = CONCAT(LPAD(v_physical_floor, 2, '0'), LPAD(v_room_idx, 2, '0'));
                SET v_room_code = CONCAT(v_floor_code, '-', v_room_number);

                -- 插入房间（sys_room）
                INSERT INTO sys_room (room_code, room_number, floor_id, floor_number, floor_code,
                                      campus_code, room_type, bed_count, current_occupancy,
                                      max_occupancy, room_status, sort, status, deleted,
                                      create_time, update_time, create_by, update_by)
                VALUES (v_room_code, v_room_number, v_floor_id, v_physical_floor, v_floor_code,
                        @campus_code, 'standard', v_beds_per_room, 0,
                        v_beds_per_room, 1, (v_physical_floor - 1) * v_room_per_floor + v_room_idx,
                        1, 0,
                        @now, @now, @operator, @operator);
                SET v_room_id = LAST_INSERT_ID();

                -- 插入 6 张床位（sys_bed）
                SET v_bed_idx = 1;
                WHILE v_bed_idx <= v_beds_per_room DO
                    SET v_bed_code = CONCAT(v_room_code, '-', v_bed_idx);
                    INSERT INTO sys_bed (bed_code, bed_number, room_id, room_code,
                                         floor_id, floor_code, campus_code,
                                         bed_status, sort, status, deleted,
                                         create_time, update_time, create_by, update_by)
                    VALUES (v_bed_code, v_bed_idx, v_room_id, v_room_code,
                            v_floor_id, v_floor_code, @campus_code,
                            1, v_bed_idx, 1, 0,
                            @now, @now, @operator, @operator);
                    SET v_bed_idx = v_bed_idx + 1;
                END WHILE;

                SET v_room_idx = v_room_idx + 1;
            END WHILE;
            SET v_physical_floor = v_physical_floor + 1;
        END WHILE;

        SET v_building_idx = v_building_idx + 1;
    END WHILE;
END$$
DELIMITER ;

-- 执行生成
CALL generate_dorm_data();
DROP PROCEDURE IF EXISTS generate_dorm_data;

-- ============================================================
-- 第三部分：统计验证
-- ============================================================

SELECT '=== 数据生成完毕 ===' AS info;

SELECT
    (SELECT COUNT(*) FROM sys_floor WHERE deleted = 0) AS total_floors,
    (SELECT COUNT(*) FROM sys_room WHERE deleted = 0) AS total_rooms,
    (SELECT COUNT(*) FROM sys_bed WHERE deleted = 0) AS total_beds,
    (SELECT COUNT(*) FROM sys_bed WHERE deleted = 0 AND bed_status = 1 AND status = 1) AS available_beds;

SELECT
    f.gender_type,
    CASE f.gender_type WHEN 1 THEN '男' WHEN 2 THEN '女' WHEN 3 THEN '混合' END AS gender_label,
    COUNT(DISTINCT f.id) AS building_count,
    COUNT(DISTINCT r.id) AS room_count,
    COUNT(DISTINCT b.id) AS bed_count
FROM sys_floor f
LEFT JOIN sys_room r ON r.floor_id = f.id AND r.deleted = 0
LEFT JOIN sys_bed b ON b.room_id = r.id AND b.deleted = 0
WHERE f.deleted = 0
GROUP BY f.gender_type
ORDER BY f.gender_type;
