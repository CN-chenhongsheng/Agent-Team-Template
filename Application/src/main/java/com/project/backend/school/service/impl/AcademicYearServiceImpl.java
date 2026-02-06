package com.project.backend.school.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.school.dto.AcademicYearQueryDTO;
import com.project.backend.school.dto.AcademicYearSaveDTO;
import com.project.backend.school.dto.SemesterSaveDTO;
import com.project.backend.school.entity.AcademicYear;
import com.project.backend.school.entity.Semester;
import com.project.backend.school.mapper.AcademicYearMapper;
import com.project.backend.school.mapper.SemesterMapper;
import com.project.backend.school.service.AcademicYearService;
import com.project.backend.school.vo.AcademicYearVO;
import com.project.backend.school.vo.SemesterVO;
import com.project.backend.util.DictUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学年Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicYearServiceImpl extends ServiceImpl<AcademicYearMapper, AcademicYear> implements AcademicYearService {

    private final SemesterMapper semesterMapper;

    @Override
    public PageResult<AcademicYearVO> pageList(AcademicYearQueryDTO queryDTO) {
        LambdaQueryWrapper<AcademicYear> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getYearCode()), AcademicYear::getYearCode, queryDTO.getYearCode())
               .like(StrUtil.isNotBlank(queryDTO.getYearName()), AcademicYear::getYearName, queryDTO.getYearName())
               .eq(queryDTO.getStatus() != null, AcademicYear::getStatus, queryDTO.getStatus())
               .orderByDesc(AcademicYear::getStartDate)
               .orderByDesc(AcademicYear::getId);

        Page<AcademicYear> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<AcademicYearVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public AcademicYearVO getDetailById(Long id) {
        AcademicYear academicYear = getById(id);
        if (academicYear == null) {
            throw new BusinessException("学年不存在");
        }
        return convertToVO(academicYear);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAcademicYear(AcademicYearSaveDTO saveDTO) {
        // 验证日期范围
        if (saveDTO.getStartDate() != null && saveDTO.getEndDate() != null) {
            if (saveDTO.getStartDate().isAfter(saveDTO.getEndDate())) {
                throw new BusinessException("开始日期不能晚于结束日期");
            }
        }

        // 检查编码是否重复
        LambdaQueryWrapper<AcademicYear> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AcademicYear::getYearCode, saveDTO.getYearCode());
        if (saveDTO.getId() != null) {
            wrapper.ne(AcademicYear::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("学年编码已存在");
        }

        // 验证学期日期是否在学年日期范围内
        if (saveDTO.getSemesters() != null && !saveDTO.getSemesters().isEmpty()) {
            LocalDate yearStartDate = saveDTO.getStartDate();
            LocalDate yearEndDate = saveDTO.getEndDate();
            for (SemesterSaveDTO semester : saveDTO.getSemesters()) {
                if (semester.getStartDate() != null && semester.getEndDate() != null) {
                    if (semester.getStartDate().isAfter(semester.getEndDate())) {
                        throw new BusinessException("学期开始日期不能晚于结束日期");
                    }
                    if (yearStartDate != null && semester.getStartDate().isBefore(yearStartDate)) {
                        throw new BusinessException("学期开始日期不能早于学年开始日期");
                    }
                    if (yearEndDate != null && semester.getEndDate().isAfter(yearEndDate)) {
                        throw new BusinessException("学期结束日期不能晚于学年结束日期");
                    }
                }
            }
        }

        // 保存或更新学年
        AcademicYear academicYear = new AcademicYear();
        BeanUtil.copyProperties(saveDTO, academicYear);

        if (saveDTO.getId() == null) {
            // 新增
            academicYear.setStatus(academicYear.getStatus() != null ? academicYear.getStatus() : 1);
            save(academicYear);
        } else {
            // 更新
            updateById(academicYear);
        }

        // 处理学期数据
        if (saveDTO.getSemesters() != null) {
            // 如果是编辑，先删除旧的学期数据
            if (saveDTO.getId() != null) {
                LambdaQueryWrapper<Semester> semesterWrapper = new LambdaQueryWrapper<>();
                semesterWrapper.eq(Semester::getAcademicYearId, academicYear.getId());
                semesterMapper.delete(semesterWrapper);
            }

            // 保存新的学期数据
            for (SemesterSaveDTO semesterDTO : saveDTO.getSemesters()) {
                // 检查学期编码是否重复（在同一学年内）
                LambdaQueryWrapper<Semester> semesterCodeWrapper = new LambdaQueryWrapper<>();
                semesterCodeWrapper.eq(Semester::getSemesterCode, semesterDTO.getSemesterCode())
                                  .eq(Semester::getAcademicYearId, academicYear.getId());
                if (semesterMapper.selectCount(semesterCodeWrapper) > 0) {
                    throw new BusinessException("学期编码已存在：" + semesterDTO.getSemesterCode());
                }

                Semester semester = new Semester();
                BeanUtil.copyProperties(semesterDTO, semester);
                semester.setAcademicYearId(academicYear.getId());
                semesterMapper.insert(semester);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAcademicYear(Long id) {
        if (id == null) {
            throw new BusinessException("学年ID不能为空");
        }

        AcademicYear academicYear = getById(id);
        if (academicYear == null) {
            throw new BusinessException("学年不存在");
        }

        // 删除关联的学期（外键级联删除，但这里显式删除更安全）
        LambdaQueryWrapper<Semester> semesterWrapper = new LambdaQueryWrapper<>();
        semesterWrapper.eq(Semester::getAcademicYearId, id);
        semesterMapper.delete(semesterWrapper);

        // 删除学年
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("学年ID不能为空");
        }

        // 批量删除学期
        for (Long id : ids) {
            LambdaQueryWrapper<Semester> semesterWrapper = new LambdaQueryWrapper<>();
            semesterWrapper.eq(Semester::getAcademicYearId, id);
            semesterMapper.delete(semesterWrapper);
        }

        // 批量删除学年
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        AcademicYear academicYear = getById(id);
        if (academicYear == null) {
            throw new BusinessException("学年不存在");
        }

        academicYear.setStatus(status);
        return updateById(academicYear);
    }

    /**
     * 实体转VO
     */
    private AcademicYearVO convertToVO(AcademicYear academicYear) {
        AcademicYearVO vo = new AcademicYearVO();
        BeanUtil.copyProperties(academicYear, vo);
        vo.setStatusText(DictUtils.getLabel("sys_user_status", academicYear.getStatus(), "未知"));

        // 查询关联的学期列表
        LambdaQueryWrapper<Semester> semesterWrapper = new LambdaQueryWrapper<>();
        semesterWrapper.eq(Semester::getAcademicYearId, academicYear.getId())
                      .orderByAsc(Semester::getStartDate);
        List<Semester> semesters = semesterMapper.selectList(semesterWrapper);

        List<SemesterVO> semesterVOList = semesters.stream()
                .map(this::convertSemesterToVO)
                .collect(Collectors.toList());

        vo.setSemesters(semesterVOList);

        return vo;
    }

    /**
     * 学期实体转VO
     */
    private SemesterVO convertSemesterToVO(Semester semester) {
        SemesterVO vo = new SemesterVO();
        BeanUtil.copyProperties(semester, vo);
        return vo;
    }
}
