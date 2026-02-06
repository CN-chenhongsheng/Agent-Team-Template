package com.project.backend.allocation.service;

import com.project.backend.student.entity.Student;
import com.project.backend.allocation.algorithm.model.MatchResult;
import com.project.backend.allocation.algorithm.model.RoomMatchResult;
import com.project.backend.allocation.entity.AllocationConfig;

import java.util.List;

/**
 * 匹配度计算服务接口
 * 提供学生之间匹配度计算的核心方法
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
public interface CompatibilityService {

    /**
     * 计算两个学生之间的匹配度
     *
     * @param studentA 学生A
     * @param studentB 学生B
     * @param config   分配配置（包含权重和约束设置）
     * @return 匹配结果（包含总分、各维度得分、冲突原因、优势等）
     */
    MatchResult calculateCompatibility(Student studentA, Student studentB, AllocationConfig config);

    /**
     * 检查两个学生之间是否存在硬约束冲突
     *
     * @param studentA 学生A
     * @param studentB 学生B
     * @param config   分配配置
     * @return true表示存在硬约束冲突，不能同住
     */
    boolean hasHardConflict(Student studentA, Student studentB, AllocationConfig config);

    /**
     * 计算学生与房间现有室友的综合匹配度
     *
     * @param student   待分配学生
     * @param roommates 房间现有室友列表
     * @param config    分配配置
     * @return 综合匹配结果
     */
    RoomMatchResult calculateRoomCompatibility(Student student, List<Student> roommates, AllocationConfig config);

    /**
     * 计算学生与房间的匹配度（通过房间ID查询室友）
     *
     * @param student 待分配学生
     * @param roomId  房间ID
     * @param bedId   目标床位ID
     * @param config  分配配置
     * @return 综合匹配结果
     */
    RoomMatchResult calculateRoomCompatibilityByRoomId(Student student, Long roomId, Long bedId, AllocationConfig config);
}
