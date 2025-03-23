package com.fadedtumi.sillytavernaccount.controller;

import com.fadedtumi.sillytavernaccount.dto.*;
import com.fadedtumi.sillytavernaccount.entity.User;
import com.fadedtumi.sillytavernaccount.service.AuthService;
import com.fadedtumi.sillytavernaccount.service.DeviceTokenService;
import com.fadedtumi.sillytavernaccount.service.GoogleAuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private DeviceTokenService deviceTokenService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = authService.registerUser(registerRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "用户注册成功");
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = authService.getUserByUsername(username);

        authService.logoutUser(user.getId());

        return ResponseEntity.ok(Collections.singletonMap("message", "登出成功"));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(
            @Valid @RequestBody RegisterRequest registerRequest,
            @RequestParam("adminKey") String adminKey) {

        User admin = authService.registerAdminUser(registerRequest, adminKey);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "管理员账号创建成功");
        response.put("username", admin.getUsername());
        response.put("email", admin.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/device-token")
    public ResponseEntity<?> registerDeviceToken(
            @Valid @RequestBody DeviceTokenRequest deviceTokenRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        deviceTokenService.saveDeviceToken(
                username,
                deviceTokenRequest.getToken(),
                deviceTokenRequest.getDeviceType());

        return ResponseEntity.ok(Collections.singletonMap("message", "设备令牌注册成功"));
    }

    @DeleteMapping("/device-token")
    public ResponseEntity<?> removeDeviceToken(@RequestParam String token) {
        deviceTokenService.removeDeviceToken(token);
        return ResponseEntity.ok(Collections.singletonMap("message", "设备令牌已移除"));
    }

    // 第一阶段：验证Google ID令牌
    @PostMapping("/google-oauth")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> request) {
        try {
            String idToken = request.get("idToken");
            logger.info("收到Google认证请求，idToken长度: {}", idToken != null ? idToken.length() : 0);

            if (idToken == null || idToken.isEmpty()) {
                logger.error("idToken为空");
                return ResponseEntity.badRequest().body(Map.of("message", "ID令牌不能为空"));
            }

            GoogleAuthResponse response = googleAuthService.validateGoogleToken(idToken);
            logger.info("Google认证成功，用户邮箱: {}, 是否新用户: {}", response.getEmail(), response.isNewUser());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Google认证过程中发生错误", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 第二阶段：完成Google登录流程
    @PostMapping("/google-oauth/complete")
    public ResponseEntity<?> completeGoogleAuth(@RequestBody Map<String, String> request) {
        try {
            String tempToken = request.get("tempToken");
            String username = request.get("username");
            String password = request.get("password");

            logger.info("完成Google认证流程，用户名: {}", username);

            if (tempToken == null || username == null || password == null) {
                logger.error("完成Google认证的请求参数不完整");
                return ResponseEntity.badRequest().body(Map.of("message", "请求参数不完整"));
            }

            AuthResponse response = googleAuthService.completeGoogleAuth(tempToken, username, password);
            logger.info("Google认证完成流程成功，用户ID: {}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("完成Google认证流程时发生错误", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 新增：处理已有用户的Google直接登录
    @PostMapping("/google-oauth/login")
    public ResponseEntity<?> loginExistingUserWithGoogle(@RequestBody GoogleLoginExistingRequest request) {
        try {
            String tempToken = request.getTempToken();
            logger.info("已有用户的Google登录请求");

            if (tempToken == null || tempToken.isEmpty()) {
                logger.error("临时令牌为空");
                return ResponseEntity.badRequest().body(Map.of("message", "临时令牌不能为空"));
            }

            AuthResponse response = googleAuthService.loginWithGoogle(tempToken);
            logger.info("已有用户的Google登录成功，用户ID: {}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("已有用户Google登录过程中发生错误", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}