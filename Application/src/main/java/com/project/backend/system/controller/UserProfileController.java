package com.project.backend.system.controller;

import com.project.core.result.R;
import com.project.backend.system.dto.ChangePasswordDTO;
import com.project.backend.system.dto.UserProfileDTO;
import com.project.backend.system.service.UserService;
import com.project.backend.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户个人中心控制器
 *
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@Tag(name = "用户个人中心管理", description = "用户个人信息的增删改查")
@RestController
@RequestMapping("/v1/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    /**
     * 获取当前用户个人信息
     */
    @Operation(summary = "获取个人信息")
    @GetMapping
    public R<UserVO> getProfile() {
        UserVO userVO = userService.getCurrentUserProfile();
        return R.ok(userVO);
    }

    /**
     * 更新当前用户个人信息
     */
    @Operation(summary = "更新个人信息")
    @PutMapping
    public R<Void> updateProfile(@Valid @RequestBody UserProfileDTO profileDTO) {
        boolean success = userService.updateCurrentUserProfile(profileDTO);
        return success ? R.ok() : R.fail("更新失败");
    }

    /**
     * 修改当前用户密码
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        boolean success = userService.changeCurrentUserPassword(changePasswordDTO);
        return success ? R.ok() : R.fail("修改密码失败");
    }
}
