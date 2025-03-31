/**
 * @作者: FADEDTUMI
 * @版本: 3.3.0
 * @创建时间: 2025-03-31
 * @修改记录:FADEDTUMI
 * -----------------------------------------------------------------------------
 * 版权声明: 本代码归FADEDTUMI所有，任何形式的商业使用需要获得授权
 * 项目主页: https://github.com/FADEDTUMI/SillytavernAccount
 * 赞助链接: https://afdian.com/a/FadedTUMI
 */
package com.fadedtumi.starinn.controller;

import com.fadedtumi.starinn.dto.ChangePasswordRequest;
import com.fadedtumi.starinn.dto.DeleteAccountRequest;
import com.fadedtumi.starinn.dto.UpdateUserRequest;
import com.fadedtumi.starinn.entity.User;
import com.fadedtumi.starinn.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        // 获取当前认证用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.getUserByUsername(username);

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("fullName", user.getFullName());
        profile.put("phoneNumber", user.getPhoneNumber());
        profile.put("roles", user.getRoles());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("lastLogin", user.getLastLogin());

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UpdateUserRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User updatedUser = userService.updateUserProfile(username, updateRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "用户资料更新成功");
        response.put("user", updatedUser);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest passwordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        userService.changePassword(username, passwordRequest);

        return ResponseEntity.ok(Collections.singletonMap("message", "密码修改成功"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(@Valid @RequestBody DeleteAccountRequest deleteRequest,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 验证确认文本
        if (!"DELETE MY ACCOUNT".equals(deleteRequest.getConfirmation())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "确认信息不正确，请输入'DELETE MY ACCOUNT'以确认注销"));
        }

        try {
            // 执行账户注销
            userService.deleteUserAccount(username, deleteRequest.getPassword());

            // 执行登出
            new SecurityContextLogoutHandler().logout(request, response, authentication);

            return ResponseEntity.ok(Collections.singletonMap("message", "账户已成功注销"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }
}