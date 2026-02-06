package com.project.backend.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.entity.Class;
import com.project.backend.organization.entity.Department;
import com.project.backend.organization.entity.Major;
import com.project.backend.organization.mapper.CampusMapper;
import com.project.backend.organization.mapper.ClassMapper;
import com.project.backend.organization.mapper.DepartmentMapper;
import com.project.backend.organization.mapper.MajorMapper;
import com.project.backend.organization.service.SchoolHierarchyService;
import com.project.backend.organization.vo.SchoolHierarchyNodeVO;
import com.project.backend.organization.vo.SchoolHierarchyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学校层级服务实现
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolHierarchyServiceImpl implements SchoolHierarchyService {

    private final CampusMapper campusMapper;
    private final DepartmentMapper departmentMapper;
    private final MajorMapper majorMapper;
    private final ClassMapper classMapper;

    @Override
    public SchoolHierarchyVO getFullHierarchy() {
        // 1. 查询所有校区（只查询启用的）
        LambdaQueryWrapper<Campus> campusWrapper = new LambdaQueryWrapper<>();
        campusWrapper.eq(Campus::getStatus, 1);
        campusWrapper.orderByAsc(Campus::getSort);
        campusWrapper.orderByAsc(Campus::getId);
        List<Campus> allCampuses = campusMapper.selectList(campusWrapper);

        // 2. 查询所有院系（只查询启用的）
        LambdaQueryWrapper<Department> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(Department::getStatus, 1);
        deptWrapper.orderByAsc(Department::getSort);
        deptWrapper.orderByAsc(Department::getId);
        List<Department> allDepartments = departmentMapper.selectList(deptWrapper);

        // 3. 查询所有专业（只查询启用的）
        LambdaQueryWrapper<Major> majorWrapper = new LambdaQueryWrapper<>();
        majorWrapper.eq(Major::getStatus, 1);
        majorWrapper.orderByAsc(Major::getId);
        List<Major> allMajors = majorMapper.selectList(majorWrapper);

        // 4. 查询所有班级（只查询启用的）
        LambdaQueryWrapper<Class> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.eq(Class::getStatus, 1);
        classWrapper.orderByAsc(Class::getId);
        List<Class> allClasses = classMapper.selectList(classWrapper);

        // 5. 按编码分组，方便查找（处理null 值）
        Map<String, List<Department>> deptMapByCampus = allDepartments.stream()
                .filter(dept -> dept.getCampusCode() != null && !dept.getCampusCode().isEmpty())
                .collect(Collectors.groupingBy(Department::getCampusCode));

        Map<String, List<Major>> majorMapByDept = allMajors.stream()
                .filter(major -> major.getDeptCode() != null && !major.getDeptCode().isEmpty())
                .collect(Collectors.groupingBy(Major::getDeptCode));

        Map<String, List<Class>> classMapByMajor = allClasses.stream()
                .filter(cls -> cls.getMajorCode() != null && !cls.getMajorCode().isEmpty())
                .collect(Collectors.groupingBy(Class::getMajorCode));

        // 6. 构建校区列表
        List<SchoolHierarchyNodeVO> campusNodes = allCampuses.stream()
                .map(campus -> buildCampusNode(campus, deptMapByCampus, majorMapByDept, classMapByMajor))
                .collect(Collectors.toList());

        SchoolHierarchyVO result = new SchoolHierarchyVO();
        result.setCampuses(campusNodes);
        return result;
    }

    /**
     * 构建校区节点
     */
    private SchoolHierarchyNodeVO buildCampusNode(
            Campus campus,
            Map<String, List<Department>> deptMapByCampus,
            Map<String, List<Major>> majorMapByDept,
            Map<String, List<Class>> classMapByMajor) {
        SchoolHierarchyNodeVO node = new SchoolHierarchyNodeVO();
        node.setId(campus.getId());
        node.setCode(campus.getCampusCode());
        node.setName(campus.getCampusName());
        node.setType("campus");
        node.setStatus(campus.getStatus());

        // 构建院系节点
        String campusCode = campus.getCampusCode();
        List<Department> departments = (campusCode != null && !campusCode.isEmpty())
                ? deptMapByCampus.getOrDefault(campusCode, new ArrayList<>())
                : new ArrayList<>();
        List<SchoolHierarchyNodeVO> deptNodes = departments.stream()
                .map(dept -> buildDepartmentNode(dept, majorMapByDept, classMapByMajor))
                .collect(Collectors.toList());

        if (!deptNodes.isEmpty()) {
            node.setChildren(deptNodes);
        }

        return node;
    }

    /**
     * 构建院系节点
     */
    private SchoolHierarchyNodeVO buildDepartmentNode(
            Department department,
            Map<String, List<Major>> majorMapByDept,
            Map<String, List<Class>> classMapByMajor) {
        SchoolHierarchyNodeVO node = new SchoolHierarchyNodeVO();
        node.setId(department.getId());
        node.setCode(department.getDeptCode());
        node.setName(department.getDeptName());
        node.setType("department");
        node.setParentCode(department.getCampusCode());
        node.setStatus(department.getStatus());

        // 构建专业节点
        String deptCode = department.getDeptCode();
        List<Major> majors = (deptCode != null && !deptCode.isEmpty())
                ? majorMapByDept.getOrDefault(deptCode, new ArrayList<>())
                : new ArrayList<>();
        List<SchoolHierarchyNodeVO> majorNodes = majors.stream()
                .map(major -> buildMajorNode(major, classMapByMajor))
                .collect(Collectors.toList());

        if (!majorNodes.isEmpty()) {
            node.setChildren(majorNodes);
        }

        return node;
    }

    /**
     * 构建专业节点
     */
    private SchoolHierarchyNodeVO buildMajorNode(
            Major major,
            Map<String, List<Class>> classMapByMajor) {
        SchoolHierarchyNodeVO node = new SchoolHierarchyNodeVO();
        node.setId(major.getId());
        node.setCode(major.getMajorCode());
        node.setName(major.getMajorName());
        node.setType("major");
        node.setParentCode(major.getDeptCode());
        node.setStatus(major.getStatus());

        // 构建班级节点
        String majorCode = major.getMajorCode();
        List<Class> classes = (majorCode != null && !majorCode.isEmpty())
                ? classMapByMajor.getOrDefault(majorCode, new ArrayList<>())
                : new ArrayList<>();
        List<SchoolHierarchyNodeVO> classNodes = classes.stream()
                .map(this::buildClassNode)
                .collect(Collectors.toList());

        if (!classNodes.isEmpty()) {
            node.setChildren(classNodes);
        }

        return node;
    }

    /**
     * 构建班级节点
     */
    private SchoolHierarchyNodeVO buildClassNode(Class classEntity) {
        SchoolHierarchyNodeVO node = new SchoolHierarchyNodeVO();
        node.setId(classEntity.getId());
        node.setCode(classEntity.getClassCode());
        node.setName(classEntity.getClassName());
        node.setType("class");
        node.setParentCode(classEntity.getMajorCode());
        node.setStatus(classEntity.getStatus());
        // 班级是叶子节点，没有children
        return node;
    }
}
