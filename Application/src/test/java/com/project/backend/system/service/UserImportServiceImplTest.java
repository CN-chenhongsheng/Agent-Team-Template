package com.project.backend.system.service;

import com.project.backend.system.dto.UserImportDTO;
import com.project.backend.system.dto.UserSaveDTO;
import com.project.backend.system.entity.Role;
import com.project.backend.system.mapper.RoleMapper;
import com.project.backend.system.service.impl.UserImportServiceImpl;
import com.project.backend.system.vo.UserImportResultVO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户导入服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户导入服务测试")
class UserImportServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleMapper roleMapper;

    @TempDir
    private Path tempDir;

    private UserImportServiceImpl importService;

    @BeforeEach
    void setUp() {
        importService = new UserImportServiceImpl(userService, roleMapper);
        ReflectionTestUtils.setField(importService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(importService, "publicPath", "/v1/common/files");
        ReflectionTestUtils.setField(importService, "contextPath", "/api");
    }

    @Test
    @DisplayName("导入一行合法用户")
    void importUsers_success() throws Exception {
        Path file = tempDir.resolve("uploads").resolve("20260101").resolve("users.xlsx");
        Files.createDirectories(file.getParent());
        writeWorkbook(file,
                new String[]{"*用户名", "*昵称", "*手机号", "邮箱", "性别", "*角色", "状态"},
                new String[]{"importuser", "导入用户", "13800138001", "import@example.com", "男", "普通用户", "正常"});

        Role role = new Role();
        role.setId(6L);
        role.setRoleCode("COMMON_USER");
        role.setRoleName("普通用户");
        role.setStatus(1);
        when(roleMapper.selectList(any())).thenReturn(List.of(role));
        when(userService.saveUser(any())).thenReturn(true);

        UserImportDTO importDTO = new UserImportDTO();
        importDTO.setFileUrl("/api/v1/common/files/uploads/20260101/users.xlsx");

        UserImportResultVO result = importService.importUsers(importDTO);

        assertThat(result.getTotalRows()).isEqualTo(1);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isZero();
        assertThat(result.getErrors()).isEmpty();

        ArgumentCaptor<UserSaveDTO> captor = ArgumentCaptor.forClass(UserSaveDTO.class);
        verify(userService).saveUser(captor.capture());
        UserSaveDTO saveDTO = captor.getValue();
        assertThat(saveDTO.getUsername()).isEqualTo("importuser");
        assertThat(saveDTO.getPhone()).isEqualTo("13800138001");
        assertThat(saveDTO.getGender()).isEqualTo(1);
        assertThat(saveDTO.getStatus()).isEqualTo(1);
        assertThat(saveDTO.getRoleIds()).containsExactly(6L);
    }

    @Test
    @DisplayName("非法行会进入错误明细，不影响其他行")
    void importUsers_parsesInvalidRow() throws Exception {
        Path file = tempDir.resolve("uploads").resolve("20260101").resolve("users.xlsx");
        Files.createDirectories(file.getParent());
        writeWorkbook(file,
                new String[]{"*用户名", "*昵称", "*手机号", "*角色"},
                new String[]{"bad name", "非法用户", "13800138002", "普通用户"});

        Role role = new Role();
        role.setId(6L);
        role.setRoleCode("COMMON_USER");
        role.setRoleName("普通用户");
        role.setStatus(1);
        when(roleMapper.selectList(any())).thenReturn(List.of(role));

        UserImportDTO importDTO = new UserImportDTO();
        importDTO.setFileUrl("/api/v1/common/files/uploads/20260101/users.xlsx");

        UserImportResultVO result = importService.importUsers(importDTO);

        assertThat(result.getTotalRows()).isEqualTo(1);
        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("用户名");
    }

    private void writeWorkbook(Path file, String[] headers, String[] values) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             OutputStream outputStream = Files.newOutputStream(file)) {
            Sheet sheet = workbook.createSheet("用户数据");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            Row dataRow = sheet.createRow(1);
            for (int i = 0; i < values.length; i++) {
                dataRow.createCell(i).setCellValue(values[i]);
            }
            workbook.write(outputStream);
        }
    }
}
