package com.project.backend.organization.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.organization.dto.major.MajorQueryDTO;
import com.project.backend.organization.dto.major.MajorSaveDTO;
import com.project.backend.organization.entity.Class;
import com.project.backend.organization.entity.Department;
import com.project.backend.organization.entity.Major;
import com.project.backend.organization.mapper.ClassMapper;
import com.project.backend.organization.mapper.DepartmentMapper;
import com.project.backend.organization.mapper.MajorMapper;
import com.project.backend.organization.service.MajorService;
import com.project.backend.organization.vo.MajorVO;
import com.project.backend.util.DictUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 专业Service实现
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {

    private final DepartmentMapper departmentMapper;
    private final ClassMapper classMapper;

    @Override
    public PageResult<MajorVO> pageList(MajorQueryDTO queryDTO) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getMajorCode()), Major::getMajorCode, queryDTO.getMajorCode())
               .like(StrUtil.isNotBlank(queryDTO.getMajorName()), Major::getMajorName, queryDTO.getMajorName())
               .eq(StrUtil.isNotBlank(queryDTO.getDeptCode()), Major::getDeptCode, queryDTO.getDeptCode())
               .eq(queryDTO.getStatus() != null, Major::getStatus, queryDTO.getStatus())
               .orderByAsc(Major::getId);

        Page<Major> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<MajorVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public MajorVO getDetailById(Long id) {
        Major major = getById(id);
        if (major == null) {
            throw new BusinessException("专业不存在");
        }
        return convertToVO(major);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMajor(MajorSaveDTO saveDTO) {
        // 检查编码是否重复
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getMajorCode, saveDTO.getMajorCode());
        if (saveDTO.getId() != null) {
            wrapper.ne(Major::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("专业编码已存在");
        }

        // 检查院系是否存在
        LambdaQueryWrapper<Department> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(Department::getDeptCode, saveDTO.getDeptCode());
        if (departmentMapper.selectCount(deptWrapper) == 0) {
            throw new BusinessException("所属院系不存在");
        }

        Major major = new Major();
        BeanUtil.copyProperties(saveDTO, major);

        if (saveDTO.getId() == null) {
            // 新增时默认状态为启用
            if (major.getStatus() == null) {
                major.setStatus(1);
            }
            return save(major);
        } else {
            return updateById(major);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMajor(Long id) {
        if (id == null) {
            throw new BusinessException("专业ID不能为空");
        }

        Major major = getById(id);
        if (major == null) {
            throw new BusinessException("专业不存在");
        }

        // 删除所有属于该专业的班级
        LambdaQueryWrapper<Class> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.eq(Class::getMajorCode, major.getMajorCode());
        classMapper.delete(classWrapper);

        // 删除专业
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("专业ID不能为空");
        }

        // 查询所有要删除的专业
        List<Major> majors = listByIds(Arrays.asList(ids));
        List<String> majorCodes = majors.stream()
                .map(Major::getMajorCode)
                .collect(Collectors.toList());

        if (!majorCodes.isEmpty()) {
            // 删除所有属于这些专业的班级
            LambdaQueryWrapper<Class> classWrapper = new LambdaQueryWrapper<>();
            classWrapper.in(Class::getMajorCode, majorCodes);
            classMapper.delete(classWrapper);
        }

        // 删除专业
        return removeByIds(Arrays.asList(ids));
    }

    /**
     * 更新专业状态
     * 如果状态改为关闭，则级联关闭该专业下的所有班级
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Major major = getById(id);
        if (major == null) {
            throw new BusinessException("专业不存在");
        }

        // 如果要启用专业，需要检查所属院系是否启用
        if (status == 1 && StrUtil.isNotBlank(major.getDeptCode())) {
            LambdaQueryWrapper<Department> deptWrapper = new LambdaQueryWrapper<>();
            deptWrapper.eq(Department::getDeptCode, major.getDeptCode());
            Department department = departmentMapper.selectOne(deptWrapper);
            if (department != null && department.getStatus() != null && department.getStatus() == 0) {
                throw new BusinessException("该院系处于停用状态，不允许启用专业");
            }
        }

        major.setStatus(status);
        boolean result = updateById(major);

        // 如果状态改为关闭（0），则级联关闭下级数据
        if (status == 0) {
            // 更新所有属于该专业的班级状态（批量更新）
            LambdaQueryWrapper<Class> classWrapper = new LambdaQueryWrapper<>();
            classWrapper.eq(Class::getMajorCode, major.getMajorCode());
            Class classUpdate = new Class();
            classUpdate.setStatus(0);
            classMapper.update(classUpdate, classWrapper);
        }

        return result;
    }

    /**
     * 实体转VO
     */
    private MajorVO convertToVO(Major major) {
        MajorVO vo = new MajorVO();
        BeanUtil.copyProperties(major, vo);
        vo.setStatusText(DictUtils.getLabel("sys_user_status", major.getStatus(), "未知"));

        // 学位类型文本（从字典获取）
        if (StrUtil.isNotBlank(major.getType())) {
            vo.setTypeText(DictUtils.getLabel("degree_type", major.getType(), "未知"));
        }

        // 查询院系名称
        if (StrUtil.isNotBlank(major.getDeptCode())) {
            LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Department::getDeptCode, major.getDeptCode());
            Department department = departmentMapper.selectOne(wrapper);
            if (department != null) {
                vo.setDeptName(department.getDeptName());
            }
        }

        return vo;
    }
}
