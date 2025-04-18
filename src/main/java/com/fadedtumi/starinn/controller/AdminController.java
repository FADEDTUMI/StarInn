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

import com.fadedtumi.starinn.dto.RegisterRequest;
import com.fadedtumi.starinn.dto.AuthResponse;
import com.fadedtumi.starinn.entity.RefreshToken;
import com.fadedtumi.starinn.entity.User;
import com.fadedtumi.starinn.repository.UserRepository;
import com.fadedtumi.starinn.security.JwtTokenProvider;
import com.fadedtumi.starinn.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AdminController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Value("${admin.registration.key:defaultAdminKey}")
    private String adminRegistrationKey;

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(
            @Valid @RequestBody RegisterRequest registerRequest,
            @RequestParam("adminKey") String providedKey) {

        // 验证管理员注册密钥
        if (!adminRegistrationKey.equals(providedKey)) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "管理员密钥无效"));
        }

        // 检查用户名是否存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "用户名已被使用"));
        }

        // 检查邮箱是否存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "邮箱已被使用"));
        }

        // 创建管理员用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());

        // 设置角色为ROLE_ADMIN和ROLE_USER
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_USER");
        user.setRoles(roles);

        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true);

        userRepository.save(user);

        // 登录新管理员用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // 创建刷新令牌
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(jwt, refreshToken.getToken(), "Bearer", user.getId(),
                user.getUsername(), user.getEmail(), user.getRoles()));
    }
}