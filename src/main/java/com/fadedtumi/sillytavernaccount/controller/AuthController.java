package com.fadedtumi.sillytavernaccount.controller;

import com.fadedtumi.sillytavernaccount.dto.*;
import com.fadedtumi.sillytavernaccount.entity.User;
import com.fadedtumi.sillytavernaccount.service.AuthService;
import com.fadedtumi.sillytavernaccount.service.DeviceTokenService;
import com.fadedtumi.sillytavernaccount.service.GoogleAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        try {
            AuthResponse response = (AuthResponse) googleAuthService.authenticateWithGoogle(request.getIdToken());
            return ResponseEntity.ok(response);
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Google 认证失败: " + e.getMessage()));
        }
    }
}