package com.project.backend.student.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.backend.common.imports.core.AbstractImportService;
import com.project.backend.organization.service.SchoolHierarchyService;
import com.project.backend.organization.vo.SchoolHierarchyNodeVO;
import com.project.backend.organization.vo.SchoolHierarchyVO;
import com.project.backend.room.service.DormHierarchyService;
import com.project.backend.room.vo.DormHierarchyNodeVO;
import com.project.backend.room.vo.DormHierarchyVO;
import com.project.backend.student.dto.imports.ImportError;
import com.project.backend.student.dto.imports.StudentImportDTO;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.student.service.StudentImportService;
import com.project.core.exception.BusinessException;
import com.project.core.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 学生导入服务实现
 * 继承通用导入框架，专注业务逻辑：字段转换、数据校验、级联解析
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentImportServiceImpl
        extends AbstractImportService<StudentImportDTO, Student, StudentImportContext, StudentMapper>
        implements StudentImportService {

    private final SchoolHierarchyService schoolHierarchyService;
    private final DormHierarchyService dormHierarchyService;

    @Value("${system.default-password:123456}")
    private String defaultPassword;

    @Value("${student.import.sync-threshold:5000}")
    private int syncThreshold;

    // ========== 实现抽象方法（业务逻辑） ==========

    @Override
    protected Path resolveFileUrlToPath(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new BusinessException("fileUrl 不能为空");
        }
        log.debug("解析文件URL: {}", fileUrl);
        int idx = fileUrl.indexOf(UPLOAD_PREFIX);
        if (idx < 0) {
            log.error("文件URL中未找到 uploads/ 前缀: {}", fileUrl);
            throw new BusinessException("无效的文件 URL，未找到 uploads/ 前缀");
        }
        String relative = fileUrl.substring(idx);
        log.debug("提取的相对路径: {}", relative);
        if (relative.contains("..")) {
            log.error("相对路径包含非法字符 .. : {}", relative);
            throw new BusinessException("非法路径");
        }
        Path base = com.project.core.util.FileUtils.resolveUploadDir(uploadDir);
        log.debug("上传目录基础路径: {}", base);
        Path resolved = base.resolve(relative).normalize();
        log.debug("解析后的完整路径: {}", resolved);
        // Windows 上 Path.startsWith() 可能因路径格式不一致而失败，改用字符串比较
        String baseStr = base.toString().replace("\\", "/").toLowerCase();
        String resolvedStr = resolved.toString().replace("\\", "/").toLowerCase();
        if (!resolvedStr.startsWith(baseStr)) {
            log.error("路径校验失败 - base: {}, resolved: {}, relative: {}, baseStr: {}, resolvedStr: {}",
                    base, resolved, relative, baseStr, resolvedStr);
            throw new BusinessException("非法路径，解析后的路径不在上传目录内");
        }
        log.debug("路径校验通过");
        return resolved;
    }

    @Override
    protected StudentImportContext buildContext(String taskId, Integer estimatedTotalRows) {
        String pwd = StrUtil.isNotBlank(defaultPassword) ? BCrypt.hashpw(defaultPassword) : BCrypt.hashpw("123456");

        Map<String, String> campusNameToCode = new java.util.HashMap<>();
        Map<String, String> orgDeptKeyToCode = new java.util.HashMap<>();
        Map<String, String> orgMajorKeyToCode = new java.util.HashMap<>();
        Map<String, Long> orgClassKeyToId = new java.util.HashMap<>();
        Map<String, String> orgClassKeyToCode = new java.util.HashMap<>();

        // 额外构建 Excel 级联格式的映射（用于匹配 Excel 中的值格式：校区名称_院系名称）
        Map<String, String> orgDeptExcelKeyToCode = new java.util.HashMap<>();
        Map<String, String> orgMajorExcelKeyToCode = new java.util.HashMap<>();
        Map<String, Long> orgClassExcelKeyToId = new java.util.HashMap<>();
        Map<String, String> orgClassExcelKeyToCode = new java.util.HashMap<>();

        SchoolHierarchyVO org = schoolHierarchyService.getFullHierarchy();
        if (org.getCampuses() != null) {
            for (SchoolHierarchyNodeVO campus : org.getCampuses()) {
                String cCode = campus.getCode();
                // 校区名称使用 sanitizeName（与前端一致，因为 Excel 中的校区值也经过 sanitizeName 处理）
                String cName = sanitizeName(campus.getName(), null);
                if (cName != null && cCode != null) {
                    campusNameToCode.put(cName, cCode);
                }
                if (campus.getChildren() != null) {
                    for (SchoolHierarchyNodeVO dept : campus.getChildren()) {
                        String dCode = dept.getCode();
                        // 院系名称使用 sanitizeName（与前端一致）
                        String dName = sanitizeName(dept.getName(), "D");
                        if (cCode != null && dName != null && dCode != null) {
                            // 标准格式：campusCode|deptName（使用 sanitize 处理，用于向后兼容）
                            String dNameSimple = sanitize(dept.getName());
                            if (dNameSimple != null) {
                                orgDeptKeyToCode.put(cCode + "|" + dNameSimple, dCode);
                            }
                            // Excel 级联格式：campusName_deptName（匹配 Excel 下拉框的值，使用 sanitizeName）
                            if (cName != null) {
                                orgDeptExcelKeyToCode.put(cName + "_" + dName, dCode);
                            }
                        }
                        if (dept.getChildren() != null) {
                            for (SchoolHierarchyNodeVO major : dept.getChildren()) {
                                String mCode = major.getCode();
                                // 专业名称使用 sanitizeName（与前端一致）
                                String mName = sanitizeName(major.getName(), "M");
                                if (dCode != null && mName != null && mCode != null) {
                                    // 标准格式：deptCode|majorName（使用 sanitize 处理，用于向后兼容）
                                    String mNameSimple = sanitize(major.getName());
                                    if (mNameSimple != null) {
                                        orgMajorKeyToCode.put(dCode + "|" + mNameSimple, mCode);
                                    }
                                    // Excel 级联格式：campusName_deptName_majorName（使用 sanitizeName）
                                    if (cName != null && dName != null) {
                                        orgMajorExcelKeyToCode.put(cName + "_" + dName + "_" + mName, mCode);
                                    }
                                }
                                if (major.getChildren() != null) {
                                    for (SchoolHierarchyNodeVO classNode : major.getChildren()) {
                                        String clCode = classNode.getCode();
                                        // 班级名称使用 sanitizeName（与前端一致）
                                        String clName = sanitizeName(classNode.getName(), "C");
                                        Long clId = classNode.getId();
                                        if (mCode != null && clName != null && clId != null && clCode != null) {
                                            // 标准格式：majorCode|className（使用 sanitize 处理，用于向后兼容）
                                            String clNameSimple = sanitize(classNode.getName());
                                            if (clNameSimple != null) {
                                                orgClassKeyToId.put(mCode + "|" + clNameSimple, clId);
                                                orgClassKeyToCode.put(mCode + "|" + clNameSimple, clCode);
                                            }
                                            // Excel 级联格式：campusName_deptName_majorName_className（使用 sanitizeName）
                                            if (cName != null && dName != null && mName != null) {
                                                orgClassExcelKeyToId.put(cName + "_" + dName + "_" + mName + "_" + clName, clId);
                                                orgClassExcelKeyToCode.put(cName + "_" + dName + "_" + mName + "_" + clName, clCode);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<String, Long> dormFloorKeyToId = new java.util.HashMap<>();
        Map<String, String> dormFloorKeyToCode = new java.util.HashMap<>();
        Map<String, Long> dormRoomKeyToId = new java.util.HashMap<>();
        Map<String, String> dormRoomKeyToCode = new java.util.HashMap<>();
        Map<String, Long> dormBedKeyToId = new java.util.HashMap<>();
        Map<String, String> dormBedKeyToCode = new java.util.HashMap<>();

        // 额外构建 Excel 级联格式的映射（用于匹配 Excel 中的值格式：校区名称_楼层名称）
        Map<String, Long> dormFloorExcelKeyToId = new java.util.HashMap<>();
        Map<String, String> dormFloorExcelKeyToCode = new java.util.HashMap<>();
        Map<String, Long> dormRoomExcelKeyToId = new java.util.HashMap<>();
        Map<String, String> dormRoomExcelKeyToCode = new java.util.HashMap<>();
        Map<String, Long> dormBedExcelKeyToId = new java.util.HashMap<>();
        Map<String, String> dormBedExcelKeyToCode = new java.util.HashMap<>();

        DormHierarchyVO dorm = dormHierarchyService.getFullHierarchy();
        if (dorm.getCampuses() != null) {
            for (DormHierarchyNodeVO campus : dorm.getCampuses()) {
                String cCode = campus.getCode();
                // 校区名称使用 sanitizeName（与前端一致）
                String cName = sanitizeName(campus.getName(), null);
                if (campus.getChildren() != null) {
                    for (DormHierarchyNodeVO floor : campus.getChildren()) {
                        // 楼层名称使用 sanitizeName（与前端一致）
                        String fName = sanitizeName(floor.getName(), "F");
                        Long fId = floor.getId();
                        String fCode = floor.getCode();
                        if (cCode != null && fName != null && fId != null && fCode != null) {
                            // 标准格式：campusCode|floorName（使用 sanitize 处理，用于向后兼容）
                            String fNameSimple = sanitize(floor.getName());
                            if (fNameSimple != null) {
                                dormFloorKeyToId.put(cCode + "|" + fNameSimple, fId);
                                dormFloorKeyToCode.put(cCode + "|" + fNameSimple, fCode);
                            }
                            // Excel 级联格式：campusName_floorName（匹配 Excel 下拉框的值，使用 sanitizeName）
                            if (cName != null) {
                                dormFloorExcelKeyToId.put(cName + "_" + fName, fId);
                                dormFloorExcelKeyToCode.put(cName + "_" + fName, fCode);
                            }
                        }
                        if (floor.getChildren() != null) {
                            for (DormHierarchyNodeVO room : floor.getChildren()) {
                                // 房间号使用 sanitizeName（与前端一致）
                                String rNum = sanitizeName(room.getName(), "R");
                                Long rId = room.getId();
                                String rCode = room.getCode();
                                if (fId != null && rNum != null && rId != null && rCode != null) {
                                    // 标准格式：floorId|roomNumber（使用 sanitize 处理，用于向后兼容）
                                    String rNumSimple = sanitize(room.getName());
                                    if (rNumSimple != null) {
                                        dormRoomKeyToId.put(fId + "|" + rNumSimple, rId);
                                        dormRoomKeyToCode.put(fId + "|" + rNumSimple, rCode);
                                    }
                                    // Excel 级联格式：campusName_floorName_roomNumber（使用 sanitizeName）
                                    if (cName != null && fName != null) {
                                        dormRoomExcelKeyToId.put(cName + "_" + fName + "_" + rNum, rId);
                                        dormRoomExcelKeyToCode.put(cName + "_" + fName + "_" + rNum, rCode);
                                    }
                                }
                                if (room.getChildren() != null) {
                                    for (DormHierarchyNodeVO bed : room.getChildren()) {
                                        // 床位号使用 sanitizeName（与前端一致）
                                        String bNum = sanitizeName(bed.getName(), "B");
                                        Long bId = bed.getId();
                                        String bCode = bed.getCode();
                                        if (rId != null && bNum != null && bId != null && bCode != null) {
                                            // 标准格式：roomId|bedNumber（使用 sanitize 处理，用于向后兼容）
                                            String bNumSimple = sanitize(bed.getName());
                                            if (bNumSimple != null) {
                                                dormBedKeyToId.put(rId + "|" + bNumSimple, bId);
                                                dormBedKeyToCode.put(rId + "|" + bNumSimple, bCode);
                                            }
                                            // Excel 级联格式：campusName_floorName_roomNumber_bedNumber（使用 sanitizeName）
                                            if (cName != null && fName != null && rNum != null) {
                                                dormBedExcelKeyToId.put(cName + "_" + fName + "_" + rNum + "_" + bNum, bId);
                                                dormBedExcelKeyToCode.put(cName + "_" + fName + "_" + rNum + "_" + bNum, bCode);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Set<String> existing = baseMapper.selectList(
                        new LambdaQueryWrapper<Student>().select(Student::getStudentNo))
                .stream()
                .map(Student::getStudentNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Set<String> existingStudentNos = ConcurrentHashMap.newKeySet();
        existingStudentNos.addAll(existing);

        StudentImportContext context = StudentImportContext.builder()
                .campusNameToCode(campusNameToCode)
                .orgDeptKeyToCode(orgDeptKeyToCode)
                .orgMajorKeyToCode(orgMajorKeyToCode)
                .orgClassKeyToId(orgClassKeyToId)
                .orgClassKeyToCode(orgClassKeyToCode)
                .orgDeptExcelKeyToCode(orgDeptExcelKeyToCode)
                .orgMajorExcelKeyToCode(orgMajorExcelKeyToCode)
                .orgClassExcelKeyToId(orgClassExcelKeyToId)
                .orgClassExcelKeyToCode(orgClassExcelKeyToCode)
                .dormFloorKeyToId(dormFloorKeyToId)
                .dormFloorKeyToCode(dormFloorKeyToCode)
                .dormRoomKeyToId(dormRoomKeyToId)
                .dormRoomKeyToCode(dormRoomKeyToCode)
                .dormBedKeyToId(dormBedKeyToId)
                .dormBedKeyToCode(dormBedKeyToCode)
                .dormFloorExcelKeyToId(dormFloorExcelKeyToId)
                .dormFloorExcelKeyToCode(dormFloorExcelKeyToCode)
                .dormRoomExcelKeyToId(dormRoomExcelKeyToId)
                .dormRoomExcelKeyToCode(dormRoomExcelKeyToCode)
                .dormBedExcelKeyToId(dormBedExcelKeyToId)
                .dormBedExcelKeyToCode(dormBedExcelKeyToCode)
                .existingStudentNos(existingStudentNos)
                .defaultPasswordEncrypted(pwd)
                .taskId(taskId)
                .estimatedTotalRows(estimatedTotalRows)
                .build();

        // 打印当前系统校区/院系等配置，便于排查"全部失败"时是否为名称不一致
        log.info("导入可用校区（Excel 中校区名称需与此完全一致）: {}", campusNameToCode.keySet());
        if (campusNameToCode.isEmpty()) {
            log.warn("系统中未配置校区，导入将全部因「校区不存在」失败，请先在组织架构中维护校区、院系、专业、班级");
        }

        return context;
    }

    @Override
    protected Student convertDtoToEntity(
            StudentImportDTO dto,
            int row,
            StudentImportContext ctx,
            List<ImportError> errors,
            Set<String> batchStudentNos) {
        String studentNo = sanitize(dto.getStudentNo());
        if (StrUtil.isBlank(studentNo)) {
            errors.add(ImportError.builder().row(row).column("学号").message("学号不能为空").value(dto.getStudentNo()).build());
            return null;
        }
        if (ctx.getExistingStudentNos().contains(studentNo) || batchStudentNos.contains(studentNo)) {
            errors.add(ImportError.builder().row(row).column("学号").message("学号重复").value(studentNo).build());
            return null;
        }
        String studentName = sanitize(dto.getStudentName());
        if (StrUtil.isBlank(studentName)) {
            errors.add(ImportError.builder().row(row).column("姓名").message("姓名不能为空").value(dto.getStudentName()).build());
            return null;
        }

        String campusName = sanitize(dto.getCampusName());
        String campusCode = campusName != null ? ctx.getCampusNameToCode().get(campusName) : null;
        if (StrUtil.isBlank(dto.getCampusName()) || campusCode == null) {
            errors.add(ImportError.builder().row(row).column("校区").message("校区不存在或未填写").value(dto.getCampusName()).build());
            return null;
        }

        // 解析院系：Excel 中的值是完整的级联值（如：主校区_计算机科学与技术学院），需要使用 sanitizeName 处理后再查找
        String deptNameRaw = dto.getDeptName();
        // Excel 中的值已经经过前端 sanitizeName 处理，但可能包含特殊字符，需要再次处理以确保匹配
        String deptNameSanitized = sanitizeName(deptNameRaw, null);
        String deptCode = null;
        if (deptNameSanitized != null) {
            // 优先尝试：直接使用 Excel 中的完整级联值查找（Excel 格式：campusName_deptName）
            deptCode = ctx.getOrgDeptExcelKeyToCode().get(deptNameSanitized);
            // 如果找不到，尝试标准格式：campusCode|deptName（需要从级联值中提取院系名称）
            if (deptCode == null && campusName != null && deptNameSanitized.startsWith(campusName + "_")) {
                String deptNameOnly = deptNameSanitized.substring(campusName.length() + 1);
                deptCode = ctx.getOrgDeptKeyToCode().get(campusCode + "|" + deptNameOnly);
            }
        }
        if (StrUtil.isBlank(deptNameRaw) || deptCode == null) {
            errors.add(ImportError.builder().row(row).column("院系").message("院系不存在或未填写").value(deptNameRaw).build());
            return null;
        }

        // 解析专业：Excel 中的值是完整的级联值（如：主校区_计算机科学与技术学院_软件工程），需要使用 sanitizeName 处理后再查找
        String majorNameRaw = dto.getMajorName();
        String majorNameSanitized = sanitizeName(majorNameRaw, null);
        String majorCode = null;
        if (majorNameSanitized != null) {
            // 优先尝试：直接使用 Excel 中的完整级联值查找（Excel 格式：campusName_deptName_majorName）
            majorCode = ctx.getOrgMajorExcelKeyToCode().get(majorNameSanitized);
            // 如果找不到，尝试标准格式：deptCode|majorName（需要从级联值中提取专业名称）
            if (majorCode == null && majorNameSanitized.contains("_")) {
                int lastUnderscoreIndex = majorNameSanitized.lastIndexOf("_");
                String majorNameOnly = majorNameSanitized.substring(lastUnderscoreIndex + 1);
                majorCode = ctx.getOrgMajorKeyToCode().get(deptCode + "|" + majorNameOnly);
            }
        }
        if (StrUtil.isBlank(majorNameRaw) || majorCode == null) {
            errors.add(ImportError.builder().row(row).column("专业").message("专业不存在或未填写").value(majorNameRaw).build());
            return null;
        }

        // 解析班级：Excel 中的值是完整的级联值（如：主校区_计算机科学与技术学院_软件工程_C2023级AI算法1班），需要使用 sanitizeName 处理后再查找
        String classNameRaw = dto.getClassName();
        String classNameSanitized = sanitizeName(classNameRaw, null);
        Long classId = null;
        String classCode = null;
        if (classNameSanitized != null) {
            // 优先尝试：直接使用 Excel 中的完整级联值查找（Excel 格式：campusName_deptName_majorName_className）
            classId = ctx.getOrgClassExcelKeyToId().get(classNameSanitized);
            classCode = ctx.getOrgClassExcelKeyToCode().get(classNameSanitized);
            // 如果找不到，尝试标准格式：majorCode|className（需要从级联值中提取班级名称）
            if (classId == null && classNameSanitized.contains("_")) {
                int lastUnderscoreIndex = classNameSanitized.lastIndexOf("_");
                String classNameOnly = classNameSanitized.substring(lastUnderscoreIndex + 1);
                classId = ctx.getOrgClassKeyToId().get(majorCode + "|" + classNameOnly);
                classCode = ctx.getOrgClassKeyToCode().get(majorCode + "|" + classNameOnly);
            }
        }
        if (StrUtil.isBlank(classNameRaw) || classId == null) {
            errors.add(ImportError.builder().row(row).column("班级").message("班级不存在或未填写").value(classNameRaw).build());
            return null;
        }

        // 解析住宿信息：Excel 中的值是完整的级联值，需要使用 sanitizeName 处理后再查找
        Long floorId = null;
        String floorCode = null;
        Long roomId = null;
        String roomCode = null;
        Long bedId = null;
        String bedCode = null;
        String floorNameRaw = dto.getFloorName();
        String floorNameSanitized = sanitizeName(floorNameRaw, null);
        if (StrUtil.isNotBlank(floorNameSanitized)) {
            // 优先尝试：直接使用 Excel 中的完整级联值查找（Excel 格式：campusName_floorName）
            floorId = ctx.getDormFloorExcelKeyToId().get(floorNameSanitized);
            floorCode = ctx.getDormFloorExcelKeyToCode().get(floorNameSanitized);
            // 如果找不到，尝试标准格式：campusCode|floorName（需要从级联值中提取楼层名称）
            if (floorId == null && campusName != null && floorNameSanitized.startsWith(campusName + "_")) {
                String floorNameOnly = floorNameSanitized.substring(campusName.length() + 1);
                floorId = ctx.getDormFloorKeyToId().get(campusCode + "|" + floorNameOnly);
                floorCode = ctx.getDormFloorKeyToCode().get(campusCode + "|" + floorNameOnly);
            }

            if (floorId != null) {
                String roomNumberRaw = dto.getRoomNumber();
                String roomNumberSanitized = sanitizeName(roomNumberRaw, null);
                if (StrUtil.isNotBlank(roomNumberSanitized)) {
                    // 优先尝试：直接使用 Excel 中的完整级联值查找（Excel 格式：campusName_floorName_roomNumber）
                    roomId = ctx.getDormRoomExcelKeyToId().get(roomNumberSanitized);
                    roomCode = ctx.getDormRoomExcelKeyToCode().get(roomNumberSanitized);
                    // 如果找不到，尝试标准格式：floorId|roomNumber（需要从级联值中提取房间号）
                    if (roomId == null && roomNumberSanitized.contains("_")) {
                        int lastUnderscoreIndex = roomNumberSanitized.lastIndexOf("_");
                        String roomNumberOnly = roomNumberSanitized.substring(lastUnderscoreIndex + 1);
                        roomId = ctx.getDormRoomKeyToId().get(floorId + "|" + roomNumberOnly);
                        roomCode = ctx.getDormRoomKeyToCode().get(floorId + "|" + roomNumberOnly);
                    }

                    if (roomId != null) {
                        String bedNumberRaw = dto.getBedNumber();
                        String bedNumberSanitized = sanitizeName(bedNumberRaw, null);
                        if (StrUtil.isNotBlank(bedNumberSanitized)) {
                            // 优先尝试：直接使用 Excel 中的完整级联值查找（Excel 格式：campusName_floorName_roomNumber_bedNumber）
                            bedId = ctx.getDormBedExcelKeyToId().get(bedNumberSanitized);
                            bedCode = ctx.getDormBedExcelKeyToCode().get(bedNumberSanitized);
                            // 如果找不到，尝试标准格式：roomId|bedNumber（需要从级联值中提取床位号）
                            if (bedId == null && bedNumberSanitized.contains("_")) {
                                int lastUnderscoreIndex = bedNumberSanitized.lastIndexOf("_");
                                String bedNumberOnly = bedNumberSanitized.substring(lastUnderscoreIndex + 1);
                                bedId = ctx.getDormBedKeyToId().get(roomId + "|" + bedNumberOnly);
                                bedCode = ctx.getDormBedKeyToCode().get(roomId + "|" + bedNumberOnly);
                            }
                        }
                    }
                }
            }
        }

        // 格式校验
        String idCard = sanitize(dto.getIdCard());
        if (StrUtil.isNotBlank(idCard) && !ValidationUtils.isValidIdCard(idCard)) {
            errors.add(ImportError.builder().row(row).column("身份证号").message("身份证号格式不正确（需18位且通过校验码验证）").value(idCard).build());
        }
        String phone = sanitize(dto.getPhone());
        if (StrUtil.isNotBlank(phone) && !ValidationUtils.isValidMobile(phone)) {
            errors.add(ImportError.builder().row(row).column("手机号").message("手机号格式不正确（需11位数字，1开头）").value(phone).build());
        }
        String email = sanitize(dto.getEmail());
        if (StrUtil.isNotBlank(email) && !ValidationUtils.isValidEmail(email)) {
            errors.add(ImportError.builder().row(row).column("邮箱").message("邮箱格式不正确").value(email).build());
        }
        String parentPhone = sanitize(dto.getParentPhone());
        if (StrUtil.isNotBlank(parentPhone) && !ValidationUtils.isValidMobile(parentPhone)) {
            errors.add(ImportError.builder().row(row).column("家长电话").message("家长电话格式不正确（需11位数字，1开头）").value(parentPhone).build());
        }
        String emergencyPhone = sanitize(dto.getEmergencyPhone());
        if (StrUtil.isNotBlank(emergencyPhone) && !ValidationUtils.isValidMobile(emergencyPhone)) {
            errors.add(ImportError.builder().row(row).column("紧急联系电话").message("紧急联系电话格式不正确（需11位数字，1开头）").value(emergencyPhone).build());
        }
        String birthDateStr = sanitize(dto.getBirthDate());
        if (StrUtil.isNotBlank(birthDateStr) && !ValidationUtils.isValidDate(birthDateStr)) {
            errors.add(ImportError.builder().row(row).column("出生日期").message("出生日期格式不正确（YYYY-MM-DD）").value(birthDateStr).build());
        }
        String enrollmentYearStr = sanitize(dto.getEnrollmentYear());
        if (StrUtil.isNotBlank(enrollmentYearStr) && !ValidationUtils.isValidYear(enrollmentYearStr)) {
            errors.add(ImportError.builder().row(row).column("入学年份").message("入学年份格式不正确（YYYY）").value(enrollmentYearStr).build());
        }
        String genderStr = ctx.getDictValueCached("sys_user_sex", dto.getGender());
        if (StrUtil.isNotBlank(dto.getGender()) && genderStr == null) {
            errors.add(ImportError.builder().row(row).column("性别").message("性别值不正确（应为：男/女）").value(dto.getGender()).build());
        }
        Integer gender = genderStr != null ? Integer.parseInt(genderStr) : null;
        String academicStr = ctx.getDictValueCached("academic_status", dto.getAcademicStatus());
        if (StrUtil.isNotBlank(dto.getAcademicStatus()) && academicStr == null) {
            errors.add(ImportError.builder().row(row).column("学籍状态").message("学籍状态值不正确（应为：在读/休学/毕业/退学）").value(dto.getAcademicStatus()).build());
        }
        Integer academicStatus = academicStr != null ? Integer.parseInt(academicStr) : 1;

        Student s = new Student();
        s.setStudentNo(studentNo);
        s.setStudentName(studentName);
        s.setGender(gender);
        s.setIdCard(idCard);
        s.setPhone(phone);
        s.setEmail(email);
        s.setNation(sanitize(dto.getNation()));
        s.setPoliticalStatus(sanitize(dto.getPoliticalStatus()));
        s.setParentName(sanitize(dto.getParentName()));
        s.setParentPhone(parentPhone);
        s.setEmergencyContact(sanitize(dto.getEmergencyContact()));
        s.setEmergencyPhone(emergencyPhone);
        s.setHomeAddress(sanitize(dto.getHomeAddress()));
        s.setRemark(sanitize(dto.getRemark()));
        s.setCampusCode(campusCode);
        s.setDeptCode(deptCode);
        s.setMajorCode(majorCode);
        s.setClassId(classId);
        s.setClassCode(classCode);
        s.setFloorId(floorId);
        s.setFloorCode(floorCode);
        s.setRoomId(roomId);
        s.setRoomCode(roomCode);
        s.setBedId(bedId);
        s.setBedCode(bedCode);
        s.setStatus(1);
        s.setAcademicStatus(academicStatus != null ? academicStatus : 1);
        s.setPassword(ctx.getDefaultPasswordEncrypted());

        if (StrUtil.isNotBlank(birthDateStr) && ValidationUtils.isValidDate(birthDateStr)) {
            try {
                s.setBirthDate(LocalDate.parse(birthDateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (Exception e) {
                errors.add(ImportError.builder().row(row).column("出生日期").message("出生日期解析失败: " + e.getMessage()).value(birthDateStr).build());
            }
        }
        if (StrUtil.isNotBlank(enrollmentYearStr) && ValidationUtils.isValidYear(enrollmentYearStr)) {
            try {
                s.setEnrollmentYear(Integer.parseInt(enrollmentYearStr.trim()));
            } catch (Exception e) {
                errors.add(ImportError.builder().row(row).column("入学年份").message("入学年份解析失败: " + e.getMessage()).value(enrollmentYearStr).build());
            }
        }
        if (StrUtil.isNotBlank(dto.getSchoolingLength())) {
            try {
                s.setSchoolingLength(Integer.parseInt(dto.getSchoolingLength().trim()));
            } catch (Exception e) {
                errors.add(ImportError.builder().row(row).column("学制").message("学制解析失败: " + e.getMessage()).value(dto.getSchoolingLength()).build());
            }
        }
        s.setCurrentGrade(sanitize(dto.getCurrentGrade()));

        setLifestyleFromLabel(ctx, s, dto);

        return s;
    }

    @Override
    protected Class<StudentImportDTO> getDtoClass() {
        return StudentImportDTO.class;
    }

    @Override
    protected int getSyncThreshold() {
        return syncThreshold;
    }

    @Override
    protected Predicate<StudentImportDTO> getEmptyRowPredicate() {
        return dto -> {
            // 如果学号和姓名都为空，视为空行
            boolean studentNoEmpty = StrUtil.isBlank(dto.getStudentNo());
            boolean studentNameEmpty = StrUtil.isBlank(dto.getStudentName());
            return studentNoEmpty && studentNameEmpty;
        };
    }

    // ========== 实现接口方法（委托给父类） ==========

    @Override
    public void saveStudentBatch(List<Student> batch) {
        // 委托给父类的事务方法
        selfProxy.saveBatchWithTransaction(batch);
    }

    // ========== 辅助方法（业务特定逻辑） ==========

    /**
     * 简单的 trim 处理（用于非级联字段）
     */
    private static String sanitize(String s) {
        return s == null ? null : s.trim().isEmpty() ? null : s.trim();
    }

    /**
     * 与前端 sanitizeName 函数行为一致的名称清理方法
     * 用于级联下拉框值的匹配
     *
     * 规则（与前端 cascadeName.ts 对齐）：
     * - 只能包含：字母、数字、下划线、中文
     * - 空格/特殊字符替换为下划线
     * - 合并多个连续下划线为一个
     * - 去除首尾下划线
     * - 若以数字开头，则追加指定前缀（如 F/R/B/C）
     * - 长度截断（避免 Excel 255 限制边界问题）
     *
     * @param name 原始名称
     * @param prefix 前缀（如果名称以数字开头，会添加此前缀）
     * @return 清理后的名称
     */
    private static String sanitizeName(String name, String prefix) {
        if (StrUtil.isBlank(name)) {
            return StrUtil.isNotBlank(prefix) ? prefix + "_空" : "空";
        }

        // 替换特殊字符为下划线（与前端正则一致：[\s/\\:;,.!@#$%^&*()+=\u005B\u005D{}|<>?'"-]）
        String sanitized = name.replaceAll("[\\s/\\\\:;,.!@#$%^&*()+=\\[\\]{}|<>?'\"-]", "_");
        // 合并多个连续下划线为一个
        sanitized = sanitized.replaceAll("_+", "_");
        // 去除首尾下划线
        sanitized = sanitized.replaceAll("^_+|_+$", "");

        // 如果以数字开头，添加指定前缀
        if (StrUtil.isNotBlank(sanitized) && !sanitized.matches("^[a-zA-Z_\\u4e00-\\u9fa5].*")) {
            sanitized = (StrUtil.isNotBlank(prefix) ? prefix : "N") + sanitized;
        }

        // 限制长度（避免 Excel 255 限制边界问题）
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200);
        }

        return StrUtil.isNotBlank(sanitized) ? sanitized : (StrUtil.isNotBlank(prefix) ? prefix + "_空" : "空");
    }

    private void setLifestyleFromLabel(StudentImportContext ctx, Student s, StudentImportDTO dto) {
        setIntFromDict(ctx, s::setSmokingStatus, "student_smoking_status", dto.getSmokingStatus());
        setIntFromDict(ctx, s::setSmokingTolerance, "student_smoking_tolerance", dto.getSmokingTolerance());
        setIntFromDict(ctx, s::setSleepSchedule, "student_sleep_schedule", dto.getSleepSchedule());
        setIntFromDict(ctx, s::setSleepQuality, "student_sleep_quality", dto.getSleepQuality());
        setIntFromDict(ctx, s::setSnores, "student_snores", dto.getSnores());
        setIntFromDict(ctx, s::setSensitiveToLight, "student_sensitive_to_light", dto.getSensitiveToLight());
        setIntFromDict(ctx, s::setSensitiveToSound, "student_sensitive_to_sound", dto.getSensitiveToSound());
        setIntFromDict(ctx, s::setCleanlinessLevel, "student_cleanliness_level", dto.getCleanlinessLevel());
        setIntFromDict(ctx, s::setBedtimeCleanup, "student_bedtime_cleanup", dto.getBedtimeCleanup());
        setIntFromDict(ctx, s::setSocialPreference, "student_social_preference", dto.getSocialPreference());
        setIntFromDict(ctx, s::setAllowVisitors, "student_allow_visitors", dto.getAllowVisitors());
        setIntFromDict(ctx, s::setPhoneCallTime, "student_phone_call_time", dto.getPhoneCallTime());
        setIntFromDict(ctx, s::setStudyInRoom, "student_study_in_room", dto.getStudyInRoom());
        setIntFromDict(ctx, s::setStudyEnvironment, "student_study_environment", dto.getStudyEnvironment());
        setIntFromDict(ctx, s::setComputerUsageTime, "student_computer_usage_time", dto.getComputerUsageTime());
        setIntFromDict(ctx, s::setGamingPreference, "student_gaming_preference", dto.getGamingPreference());
        setIntFromDict(ctx, s::setMusicPreference, "student_music_preference", dto.getMusicPreference());
        setIntFromDict(ctx, s::setMusicVolume, "student_music_volume", dto.getMusicVolume());
        setIntFromDict(ctx, s::setEatInRoom, "student_eat_in_room", dto.getEatInRoom());
    }

    private void setIntFromDict(StudentImportContext ctx,
                                java.util.function.Consumer<Integer> setter,
                                String dictCode,
                                String label) {
        if (StrUtil.isBlank(label)) {
            return;
        }
        String value = ctx.getDictValueCached(dictCode, label);
        if (value != null) {
            try {
                setter.accept(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
            }
        }
    }
}
