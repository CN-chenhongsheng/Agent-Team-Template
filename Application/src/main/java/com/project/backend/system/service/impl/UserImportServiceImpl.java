package com.project.backend.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.backend.system.dto.UserImportDTO;
import com.project.backend.system.dto.UserSaveDTO;
import com.project.backend.system.entity.Role;
import com.project.backend.system.mapper.RoleMapper;
import com.project.backend.system.service.UserImportService;
import com.project.backend.system.service.UserService;
import com.project.backend.system.vo.UserImportErrorVO;
import com.project.backend.system.vo.UserImportResultVO;
import com.project.core.constant.RegexConstants;
import com.project.core.exception.BusinessException;
import com.project.core.i18n.I18nUtils;
import com.project.core.util.FileUtils;
import com.project.core.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 用户导入服务实现
 *
 * @author 陈鸿昇
 * @since 2026-11-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserImportServiceImpl implements UserImportService {

    private static final int MAX_ROWS = 5000;
    private static final int MAX_ERRORS = 200;
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    private final UserService userService;
    private final RoleMapper roleMapper;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.public-path:/v1/common/files}")
    private String publicPath;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public UserImportResultVO importUsers(UserImportDTO importDTO) {
        Path file = resolveImportFile(importDTO.getFileUrl());

        try (InputStream inputStream = Files.newInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int headerRowIndex = findHeaderRowIndex(sheet, formatter, evaluator);
            Map<String, Integer> columnIndexes = resolveColumnIndexes(
                    sheet.getRow(headerRowIndex), formatter, evaluator);
            validateRequiredHeaders(columnIndexes);

            RoleLookup roleLookup = buildRoleLookup();
            int dataRowCount = countDataRows(sheet, headerRowIndex, columnIndexes, formatter, evaluator);
            if (dataRowCount > MAX_ROWS) {
                throw new BusinessException(I18nUtils.format("单次导入不能超过 {0} 行", MAX_ROWS));
            }

            List<UserImportErrorVO> errors = new ArrayList<>();
            int totalRows = 0;
            int successCount = 0;
            int failCount = 0;

            for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row, columnIndexes, formatter, evaluator)) {
                    continue;
                }

                Map<String, String> values = readRow(row, columnIndexes, formatter, evaluator);
                if (isExampleRow(values)) {
                    continue;
                }

                totalRows++;
                try {
                    importRow(values, roleLookup);
                    successCount++;
                } catch (BusinessException e) {
                    failCount++;
                    addError(errors, rowIndex + 1, e.getMessage(), values.get("username"));
                } catch (Exception e) {
                    log.error("导入用户失败，行号：{}", rowIndex + 1, e);
                    failCount++;
                    addError(errors, rowIndex + 1, I18nUtils.translate("导入失败"), values.get("username"));
                }
            }

            return UserImportResultVO.builder()
                    .totalRows(totalRows)
                    .successCount(successCount)
                    .failCount(failCount)
                    .errors(errors)
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("读取用户导入文件失败：{}", file, e);
            throw new BusinessException("读取 Excel 失败，请确认文件格式");
        }
    }

    /**
     * 解析并校验上传文件地址，只允许读取上传目录内的 Excel 文件
     */
    private Path resolveImportFile(String fileUrl) {
        String path = extractPath(fileUrl);
        if (StrUtil.isBlank(path)) {
            throw new BusinessException("文件地址不合法");
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        String normalizedPublicPath = normalizePath(publicPath);
        String normalizedContextPath = normalizePath(contextPath);
        List<String> prefixes = new ArrayList<>();
        prefixes.add(normalizedContextPath + normalizedPublicPath);
        if (!normalizedContextPath.isEmpty()) {
            prefixes.add(normalizedPublicPath);
        }

        String relative = null;
        for (String prefix : prefixes) {
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                relative = path.substring(prefix.length());
                break;
            }
        }
        if (StrUtil.isBlank(relative)) {
            throw new BusinessException("文件地址不在上传目录内");
        }

        relative = relative.replaceFirst("^/+", "");
        if (!relative.startsWith("uploads/") || relative.startsWith("uploads/public/")) {
            throw new BusinessException("文件地址不在导入目录内");
        }

        Path uploadRoot = FileUtils.resolveUploadDir(uploadDir);
        Path file = FileUtils.resolveWithin(uploadRoot, relative);
        if (!Files.isRegularFile(file)) {
            throw new BusinessException("导入文件不存在");
        }

        String lowerName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!lowerName.endsWith(".xlsx") && !lowerName.endsWith(".xls")) {
            throw new BusinessException("仅支持 Excel 文件");
        }

        try {
            if (Files.size(file) > MAX_FILE_SIZE) {
                throw new BusinessException("导入文件不能超过 20MB");
            }
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException("读取导入文件失败");
        }

        return file;
    }

    /**
     * 提取 URL 或相对地址中的路径部分，兼容包含中文文件名的原始地址
     */
    private String extractPath(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            throw new BusinessException("文件地址不能为空");
        }
        try {
            String path = URI.create(fileUrl).getPath();
            if (StrUtil.isNotBlank(path)) {
                return path;
            }
        } catch (Exception ignored) {
            // URI 解析失败时按原始字符串处理
        }

        String path = fileUrl;
        int schemeIndex = path.indexOf("://");
        if (schemeIndex >= 0) {
            int pathIndex = path.indexOf('/', schemeIndex + 3);
            path = pathIndex >= 0 ? path.substring(pathIndex) : "";
        }
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        int fragmentIndex = path.indexOf('#');
        if (fragmentIndex >= 0) {
            path = path.substring(0, fragmentIndex);
        }
        return path;
    }

    private String normalizePath(String path) {
        if (StrUtil.isBlank(path) || "/".equals(path)) {
            return "";
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private int findHeaderRowIndex(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null && !isRowEmpty(row, formatter, evaluator)) {
                return rowIndex;
            }
        }
        throw new BusinessException("Excel 中没有表头");
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (Cell cell : row) {
            if (!StrUtil.isBlank(getCellString(cell, formatter, evaluator))) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Integer> resolveColumnIndexes(Row headerRow,
                                                      DataFormatter formatter,
                                                      FormulaEvaluator evaluator) {
        if (headerRow == null) {
            throw new BusinessException("Excel 中没有表头");
        }

        Map<String, Integer> columnIndexes = new HashMap<>();
        for (Cell cell : headerRow) {
            String field = resolveField(getCellString(cell, formatter, evaluator));
            if (field != null) {
                columnIndexes.putIfAbsent(field, cell.getColumnIndex());
            }
        }
        return columnIndexes;
    }

    private void validateRequiredHeaders(Map<String, Integer> columnIndexes) {
        Map<String, String> labels = new HashMap<>();
        labels.put("username", "用户名");
        labels.put("nickname", "昵称");
        labels.put("phone", "手机号");
        labels.put("role", "角色");

        List<String> missing = new ArrayList<>();
        labels.forEach((field, label) -> {
            if (!columnIndexes.containsKey(field)) {
                missing.add(label);
            }
        });
        if (!missing.isEmpty()) {
            throw new BusinessException(I18nUtils.format("导入模板缺少列：{0}", String.join("、", missing)));
        }
    }

    private String resolveField(String header) {
        String normalized = normalizeHeader(header);
        return switch (normalized) {
            case "用户名", "username" -> "username";
            case "昵称", "nickname" -> "nickname";
            case "手机号", "手机", "phone", "mobile" -> "phone";
            case "邮箱", "email" -> "email";
            case "性别", "gender", "sex" -> "gender";
            case "角色", "role", "roles" -> "role";
            case "状态", "status" -> "status";
            default -> null;
        };
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        return header.replace("*", "").trim().toLowerCase(Locale.ROOT);
    }

    private int countDataRows(Sheet sheet,
                              int headerRowIndex,
                              Map<String, Integer> columnIndexes,
                              DataFormatter formatter,
                              FormulaEvaluator evaluator) {
        int count = 0;
        for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null || isBlankRow(row, columnIndexes, formatter, evaluator)) {
                continue;
            }
            if (!isExampleRow(readRow(row, columnIndexes, formatter, evaluator))) {
                count++;
            }
        }
        return count;
    }

    private boolean isBlankRow(Row row,
                               Map<String, Integer> columnIndexes,
                               DataFormatter formatter,
                               FormulaEvaluator evaluator) {
        for (Integer columnIndex : columnIndexes.values()) {
            if (!StrUtil.isBlank(getCellString(row.getCell(columnIndex), formatter, evaluator))) {
                return false;
            }
        }
        return true;
    }

    private Map<String, String> readRow(Row row,
                                        Map<String, Integer> columnIndexes,
                                        DataFormatter formatter,
                                        FormulaEvaluator evaluator) {
        Map<String, String> values = new HashMap<>();
        columnIndexes.forEach((field, columnIndex) ->
                values.put(field, getCellString(row.getCell(columnIndex), formatter, evaluator)));
        return values;
    }

    private String getCellString(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue())
                    .stripTrailingZeros()
                    .toPlainString();
        }
        if (cell.getCellType() == CellType.FORMULA) {
            return formatter.formatCellValue(cell, evaluator).trim();
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return formatter.formatCellValue(cell).trim();
    }

    /**
     * 跳过模板自带的示例行
     */
    private boolean isExampleRow(Map<String, String> values) {
        String username = StrUtil.trim(values.get("username"));
        String phone = normalizePhone(values.get("phone"));
        return "zhangsan".equalsIgnoreCase(username) && "13800138000".equals(phone);
    }

    private void importRow(Map<String, String> values, RoleLookup roleLookup) {
        String username = StrUtil.trim(values.get("username"));
        String nickname = StrUtil.trim(values.get("nickname"));
        String phone = normalizePhone(values.get("phone"));
        String email = StrUtil.trim(values.get("email"));
        String roleValue = StrUtil.trim(values.get("role"));

        if (StrUtil.isBlank(username)) {
            throw new BusinessException("用户名不能为空");
        }
        if (!username.matches(RegexConstants.USERNAME)) {
            throw new BusinessException("用户名只能包含字母、数字、下划线，长度应在4-20个字符之间");
        }
        if (StrUtil.isBlank(nickname)) {
            throw new BusinessException("昵称不能为空");
        }
        if (nickname.length() > 50) {
            throw new BusinessException("昵称不能超过50个字符");
        }
        if (StrUtil.isBlank(phone)) {
            throw new BusinessException("手机号不能为空");
        }
        if (!ValidationUtils.isValidMobile(phone)) {
            throw new BusinessException("手机号格式不正确");
        }
        if (StrUtil.isNotBlank(email) && !ValidationUtils.isValidEmail(email)) {
            throw new BusinessException("邮箱格式不正确");
        }
        if (StrUtil.isBlank(roleValue)) {
            throw new BusinessException("角色不能为空");
        }

        List<Long> roleIds = resolveRoleIds(roleValue, roleLookup);
        Integer gender = parseGender(values.get("gender"));
        Integer status = parseStatus(values.get("status"));

        UserSaveDTO saveDTO = new UserSaveDTO();
        saveDTO.setUsername(username);
        saveDTO.setNickname(nickname);
        saveDTO.setPhone(phone);
        saveDTO.setEmail(StrUtil.isBlank(email) ? null : email);
        saveDTO.setGender(gender);
        saveDTO.setStatus(status);
        saveDTO.setRoleIds(roleIds.toArray(new Long[0]));

        if (!userService.saveUser(saveDTO)) {
            throw new BusinessException("保存失败");
        }
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[\\s-]", "").trim();
    }

    private List<Long> resolveRoleIds(String roleValue, RoleLookup roleLookup) {
        String[] tokens = roleValue.split("[,，、;；/|]");
        Set<Long> roleIds = new LinkedHashSet<>();

        for (String token : tokens) {
            String name = token.trim();
            if (name.isEmpty()) {
                continue;
            }
            Long roleId = roleLookup.byName.get(name);
            if (roleId == null) {
                roleId = roleLookup.byCode.get(name.toLowerCase(Locale.ROOT));
            }
            if (roleId == null) {
                throw new BusinessException(I18nUtils.format("角色不存在：{0}", name));
            }
            roleIds.add(roleId);
        }

        if (roleIds.isEmpty()) {
            throw new BusinessException("角色不能为空");
        }
        return new ArrayList<>(roleIds);
    }

    private Integer parseGender(String value) {
        if (StrUtil.isBlank(value)) {
            return 0;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "1", "男", "male", "m" -> 1;
            case "2", "女", "female", "f" -> 2;
            case "0", "未知", "unknown" -> 0;
            default -> throw new BusinessException(I18nUtils.format("性别值不正确：{0}", value));
        };
    }

    private Integer parseStatus(String value) {
        if (StrUtil.isBlank(value)) {
            return 1;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "1", "正常", "启用", "active", "enabled" -> 1;
            case "0", "停用", "禁用", "disabled", "inactive" -> 0;
            default -> throw new BusinessException(I18nUtils.format("状态值不正确：{0}", value));
        };
    }

    private RoleLookup buildRoleLookup() {
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>().eq(Role::getStatus, 1));

        Map<String, Long> byCode = new HashMap<>();
        Map<String, Long> byName = new HashMap<>();
        for (Role role : roles) {
            if (StrUtil.isNotBlank(role.getRoleCode())) {
                byCode.put(role.getRoleCode().toLowerCase(Locale.ROOT), role.getId());
            }
            if (StrUtil.isNotBlank(role.getRoleName())) {
                byName.put(role.getRoleName(), role.getId());
            }
        }
        return new RoleLookup(byCode, byName);
    }

    private void addError(List<UserImportErrorVO> errors, int row, String message, String value) {
        if (errors.size() < MAX_ERRORS) {
            errors.add(new UserImportErrorVO(row, "row", message, value));
        }
    }

    private static class RoleLookup {
        private final Map<String, Long> byCode;
        private final Map<String, Long> byName;

        private RoleLookup(Map<String, Long> byCode, Map<String, Long> byName) {
            this.byCode = byCode;
            this.byName = byName;
        }
    }
}
