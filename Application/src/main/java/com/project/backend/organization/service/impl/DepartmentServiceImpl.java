package com.project.backend.organization.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.organization.dto.department.DepartmentQueryDTO;
import com.project.backend.organization.dto.department.DepartmentSaveDTO;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.entity.Class;
import com.project.backend.organization.entity.Department;
import com.project.backend.organization.entity.Major;
import com.project.backend.organization.mapper.CampusMapper;
import com.project.backend.organization.mapper.ClassMapper;
import com.project.backend.organization.mapper.DepartmentMapper;
import com.project.backend.organization.mapper.MajorMapper;
import com.project.backend.organization.service.DepartmentService;
import com.project.backend.organization.vo.DepartmentVO;
import com.project.backend.util.DictUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 院系Service实现
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final CampusMapper campusMapper;
    private final MajorMapper majorMapper;
    private final ClassMapper classMapper;

    @Override
    public PageResult<DepartmentVO> pageList(DepartmentQueryDTO queryDTO) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getDeptCode()), Department::getDeptCode, queryDTO.getDeptCode())
               .like(StrUtil.isNotBlank(queryDTO.getDeptName()), Department::getDeptName, queryDTO.getDeptName())
               .eq(StrUtil.isNotBlank(queryDTO.getCampusCode()), Department::getCampusCode, queryDTO.getCampusCode())
               .eq(queryDTO.getStatus() != null, Department::getStatus, queryDTO.getStatus())
               .orderByAsc(Department::getSort)
               .orderByAsc(Department::getId);

        Page<Department> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<DepartmentVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<DepartmentVO> treeList(DepartmentQueryDTO queryDTO) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getDeptCode()), Department::getDeptCode, queryDTO.getDeptCode())
               .like(StrUtil.isNotBlank(queryDTO.getDeptName()), Department::getDeptName, queryDTO.getDeptName())
               .eq(StrUtil.isNotBlank(queryDTO.getCampusCode()), Department::getCampusCode, queryDTO.getCampusCode())
               .eq(queryDTO.getStatus() != null, Department::getStatus, queryDTO.getStatus())
               .orderByAsc(Department::getSort)
               .orderByAsc(Department::getId);

        List<Department> allDepartments = list(wrapper);
        return allDepartments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentVO getDetailById(Long id) {
        Department department = getById(id);
        if (department == null) {
            throw new BusinessException("院系不存在");
        }
        return convertToVO(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDepartment(DepartmentSaveDTO saveDTO) {
        // 检查编码是否重复
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getDeptCode, saveDTO.getDeptCode())
               .eq(Department::getDeleted, 0);
        if (saveDTO.getId() != null) {
            wrapper.ne(Department::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("院系编码已存在");
        }

        // 检查校区是否存在
        LambdaQueryWrapper<Campus> campusWrapper = new LambdaQueryWrapper<>();
        campusWrapper.eq(Campus::getCampusCode, saveDTO.getCampusCode());
        if (campusMapper.selectCount(campusWrapper) == 0) {
            throw new BusinessException("所属校区不存在");
        }

        Department department = new Department();
        BeanUtil.copyProperties(saveDTO, department);

        if (saveDTO.getId() == null) {
            department.setStatus(department.getStatus() != null ? department.getStatus() : 1);
            return save(department);
        } else {
            return updateById(department);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDepartment(Long id) {
        if (id == null) {
            throw new BusinessException("院系ID不能为空");
        }

        Department department = getById(id);
        if (department == null) {
            throw new BusinessException("院系不存在");
        }

        // 查询所有属于该院系的专业
        LambdaQueryWrapper<Major> majorWrapper = new LambdaQueryWrapper<>();
        majorWrapper.eq(Major::getDeptCode, department.getDeptCode());
        List<Major> majors = majorMapper.selectList(majorWrapper);

        // 获取这些专业的所有编码列表
        List<String> majorCodes = majors.stream()
                .map(Major::getMajorCode)
                .collect(Collectors.toList());

        if (!majorCodes.isEmpty()) {
            // 删除所有属于这些专业的班级
            LambdaQueryWrapper<Class> classWrapper = new LambdaQueryWrapper<>();
            classWrapper.in(Class::getMajorCode, majorCodes);
            classMapper.delete(classWrapper);

            // 删除所有专业
            majorMapper.delete(majorWrapper);
        }

        // 删除当前院系
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("院系ID不能为空");
        }
        return removeByIds(Arrays.asList(ids));
    }

    /**
     * 更新院系状态
     * 如果状态改为关闭，则级联关闭该院系下的所有专业、班级
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Department department = getById(id);
        if (department == null) {
            throw new BusinessException("院系不存在");
        }

        // 如果要启用院系，需要检查所属校区是否启用
        if (status == 1 && StrUtil.isNotBlank(department.getCampusCode())) {
            LambdaQueryWrapper<Campus> campusWrapper = new LambdaQueryWrapper<>();
            campusWrapper.eq(Campus::getCampusCode, department.getCampusCode());
            Campus campus = campusMapper.selectOne(campusWrapper);
            if (campus != null && campus.getStatus() != null && campus.getStatus() == 0) {
                throw new BusinessException("该校区处于停用状态，不允许启用院系");
            }
        }

        department.setStatus(status);
        boolean result = updateById(department);

        // 如果状态改为关闭（0），则级联关闭下级数据
        if (status == 0) {
            // 更新所有属于该院系的专业状态（批量更新）
            LambdaQueryWrapper<Major> majorWrapper = new LambdaQueryWrapper<>();
            majorWrapper.eq(Major::getDeptCode, department.getDeptCode());
            Major majorUpdate = new Major();
            majorUpdate.setStatus(0);
            majorMapper.update(majorUpdate, majorWrapper);

            // 重新查询更新后的专业列表，用于后续级联更新
            List<Major> majors = majorMapper.selectList(majorWrapper);

            // 获取这些专业的所有编码列表
            List<String> majorCodes = majors.stream()
                    .map(Major::getMajorCode)
                    .collect(Collectors.toList());

            if (!majorCodes.isEmpty()) {
                // 更新所有属于这些专业的班级状态（批量更新）
                LambdaQueryWrapper<Class> classWrapper = new LambdaQueryWrapper<>();
                classWrapper.in(Class::getMajorCode, majorCodes);
                Class classUpdate = new Class();
                classUpdate.setStatus(0);
                classMapper.update(classUpdate, classWrapper);
            }
        }

        return result;
    }

    /**
     * 实体转VO
     */
    private DepartmentVO convertToVO(Department department) {
        DepartmentVO vo = new DepartmentVO();
        BeanUtil.copyProperties(department, vo);
        vo.setStatusText(DictUtils.getLabel("sys_user_status", department.getStatus(), "未知"));

        // 查询校区名称
        if (StrUtil.isNotBlank(department.getCampusCode())) {
            LambdaQueryWrapper<Campus> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Campus::getCampusCode, department.getCampusCode());
            Campus campus = campusMapper.selectOne(wrapper);
            if (campus != null) {
                vo.setCampusName(campus.getCampusName());
            }
        }

        return vo;
    }
}
