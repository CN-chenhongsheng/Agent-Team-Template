package com.project.backend.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.student.dto.student.StudentLifestyleDTO;
import com.project.backend.student.dto.student.StudentQueryDTO;
import com.project.backend.student.dto.student.StudentSaveDTO;
import com.project.backend.student.entity.Student;
import com.project.backend.student.vo.StudentVO;
import com.project.core.result.PageResult;

/**
 * 学生Service
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
public interface StudentService extends IService<Student> {

    /**
     * 分页查询学生列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<StudentVO> pageList(StudentQueryDTO queryDTO);

    /**
     * 根据ID获取学生详情
     * 
     * @param id 学生ID
     * @return 学生信息
     */
    StudentVO getDetailById(Long id);

    /**
     * 保存学生（新增或编辑）
     * 
     * @param saveDTO 学生保存DTO
     * @return 是否成功
     */
    boolean saveStudent(StudentSaveDTO saveDTO);

    /**
     * 删除学生
     * 
     * @param id 学生ID
     * @return 是否成功
     */
    boolean deleteStudent(Long id);

    /**
     * 批量删除学生
     * 
     * @param ids 学生ID数组
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 更新学生生活习惯
     * 
     * @param studentId 学生ID
     * @param dto 生活习惯DTO
     * @return 是否成功
     */
    boolean updateLifestyle(Long studentId, StudentLifestyleDTO dto);

    /**
     * 更新学生状态
     * 
     * @param id 学生ID
     * @param status 状态：1正常 0停用
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}
