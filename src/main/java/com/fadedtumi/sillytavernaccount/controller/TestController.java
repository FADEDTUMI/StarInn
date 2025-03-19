package com.fadedtumi.sillytavernaccount.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fadedtumi.sillytavernaccount.entity.User;
import com.fadedtumi.sillytavernaccount.service.UserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("此为公共端点");
    }

    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "此为受保护的端点");
        response.put("username", principal.getName());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("此为管理员端点");
    }

    @GetMapping("/auth-status")
    public ResponseEntity<Map<String, Object>> checkAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal().equals("anonymousUser"))) {

            Object principal = authentication.getPrincipal();
            String username;

            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
                response.put("authorities", ((UserDetails) principal).getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
            } else {
                username = principal.toString();
            }

            response.put("status", "已认证");
            response.put("username", username);
            logger.info("用户 [{}] 成功通过认证状态检查", username);

        } else {
            response.put("status", "未认证");
            logger.info("未认证用户访问了认证状态检查接口");
        }

        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未认证"));
        }

        try {
            User user = userService.getUserByUsername(principal.getName());
            Map<String, Object> userInfo = new HashMap<>();

            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("fullName", user.getFullName());
            userInfo.put("roles", user.getRoles());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("lastLogin", user.getLastLogin());

            logger.debug("已成功获取用户 [{}] 的信息", principal.getName());
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            logger.error("获取用户信息时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "获取用户信息失败: " + e.getMessage()));
        }
    }

    @GetMapping("/check-roles")
    public ResponseEntity<Map<String, Object>> checkRoles(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication != null) {
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            response.put("isUser", authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

            response.put("isAdmin", authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

            logger.info("用户 [{}] 检查了角色信息", authentication.getName());
        } else {
            response.put("authenticated", false);
            logger.info("未认证用户访问了角色检查接口");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/server-time")
    public ResponseEntity<Map<String, Object>> getServerTime() {
        Map<String, Object> response = new HashMap<>();
        response.put("serverTime", System.currentTimeMillis());
        response.put("formattedTime", new java.util.Date().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session-test")
    public ResponseEntity<Map<String, Object>> sessionTest(Principal principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal != null) {
            response.put("message", "会话有效");
            response.put("username", principal.getName());
        } else {
            response.put("message", "会话无效或未认证");
        }

        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}