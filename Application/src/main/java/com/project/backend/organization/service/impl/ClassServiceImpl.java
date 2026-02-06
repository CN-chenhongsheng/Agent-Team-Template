package com.project.backend.organization.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.organization.dto.classes.ClassQueryDTO;
import com.project.backend.organization.dto.classes.ClassSaveDTO;
import com.project.backend.organization.entity.Class;
import com.project.backend.organization.entity.Major;
import com.project.backend.organization.mapper.ClassMapper;
import com.project.backend.organization.mapper.MajorMapper;
import com.project.backend.organization.service.ClassService;
import com.project.backend.organization.vo.ClassVO;
import com.project.backend.system.entity.User;
import com.project.backend.system.mapper.UserMapper;
import com.project.backend.util.DictUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 班级Service实现
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class> implements ClassService {

    private final MajorMapper majorMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<ClassVO> pageList(ClassQueryDTO queryDTO) {
        LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getClassCode()), Class::getClassCode, queryDTO.getClassCode())
               .like(StrUtil.isNotBlank(queryDTO.getClassName()), Class::getClassName, queryDTO.getClassName())
               .eq(StrUtil.isNotBlank(queryDTO.getMajorCode()), Class::getMajorCode, queryDTO.getMajorCode())
               .eq(StrUtil.isNotBlank(queryDTO.getGrade()), Class::getGrade, queryDTO.getGrade())
               .eq(queryDTO.getEnrollmentYear() != null, Class::getEnrollmentYear, queryDTO.getEnrollmentYear())
               .eq(queryDTO.getStatus() != null, Class::getStatus, queryDTO.getStatus())
               .orderByDesc(Class::getEnrollmentYear)
               .orderByAsc(Class::getId);

        Page<Class> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<ClassVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ClassVO getDetailById(Long id) {
        Class classEntity = getById(id);
        if (classEntity == null) {
            throw new BusinessException("班级不存在");
        }
        return convertToVO(classEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveClass(ClassSaveDTO saveDTO) {
        // 检查编码是否重复
        LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Class::getClassCode, saveDTO.getClassCode())
               .eq(Class::getDeleted, 0);
        if (saveDTO.getId() != null) {
            wrapper.ne(Class::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("班级编码已存在");
        }

        // 检查专业是否存在
        LambdaQueryWrapper<Major> majorWrapper = new LambdaQueryWrapper<>();
        majorWrapper.eq(Major::getMajorCode, saveDTO.getMajorCode());
        if (majorMapper.selectCount(majorWrapper) == 0) {
            throw new BusinessException("所属专业不存在");
        }

        Class classEntity = new Class();
        BeanUtil.copyProperties(saveDTO, classEntity);

        // 根据 teacherId 查询用户信息，填充teacherName
        if (saveDTO.getTeacherId() != null) {
            User user = userMapper.selectById(saveDTO.getTeacherId());
            if (user == null) {
                throw new BusinessException("负责人不存在");
            }
            // 优先使用 nickname，如果没有则使用 username
            classEntity.setTeacherName(StrUtil.isNotBlank(user.getNickname()) ? user.getNickname() : user.getUsername());
        } else {
            // 如果没有 teacherId，清空teacherName
            classEntity.setTeacherName(null);
        }

        if (saveDTO.getId() == null) {
            classEntity.setCurrentCount(classEntity.getCurrentCount() != null ? classEntity.getCurrentCount() : 0);
            // 新增时默认状态为1启用
            if (classEntity.getStatus() == null) {
                classEntity.setStatus(1);
            }
            return save(classEntity);
        } else {
            return updateById(classEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteClass(Long id) {
        if (id == null) {
            throw new BusinessException("班级ID不能为空");
        }

        // 如果学生表存在且class_id 字段关联班级，需要先清空学生的班级关联关系
        // 示例代码（如果学生表存在）：
        // LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        // studentWrapper.eq(Student::getClassId, id);
        // List<Student> students = studentMapper.selectList(studentWrapper);
        // for (Student student : students) {
        //     student.setClassId(null);
        //     studentMapper.updateById(student);
        // }

        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("班级ID不能为空");
        }
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Class classEntity = getById(id);
        if (classEntity == null) {
            throw new BusinessException("班级不存在");
        }

        // 如果要启用班级，需要检查所属专业是否启用
        if (status == 1 && StrUtil.isNotBlank(classEntity.getMajorCode())) {
            LambdaQueryWrapper<Major> majorWrapper = new LambdaQueryWrapper<>();
            majorWrapper.eq(Major::getMajorCode, classEntity.getMajorCode());
            Major major = majorMapper.selectOne(majorWrapper);
            if (major != null && major.getStatus() != null && major.getStatus() == 0) {
                throw new BusinessException("该专业处于停用状态，不允许启用班级");
            }
        }

        classEntity.setStatus(status);
        return updateById(classEntity);
    }

    /**
     * 实体转VO
     */
    private ClassVO convertToVO(Class classEntity) {
        ClassVO vo = new ClassVO();
        BeanUtil.copyProperties(classEntity, vo);
        vo.setStatusText(DictUtils.getLabel("sys_user_status", classEntity.getStatus(), "未知"));

        // 查询专业名称
        if (StrUtil.isNotBlank(classEntity.getMajorCode())) {
            LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Major::getMajorCode, classEntity.getMajorCode());
            Major major = majorMapper.selectOne(wrapper);
            if (major != null) {
                vo.setMajorName(major.getMajorName());
            }
        }

        // teacherName 和 teacherId 已经通过 BeanUtil.copyProperties 复制，无需额外处理

        return vo;
    }
}
