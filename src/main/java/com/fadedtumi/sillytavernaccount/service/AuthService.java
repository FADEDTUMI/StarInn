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
package com.fadedtumi.sillytavernaccount.service;

import com.fadedtumi.sillytavernaccount.config.AppConfig;
import com.fadedtumi.sillytavernaccount.dto.AuthResponse;
import com.fadedtumi.sillytavernaccount.dto.LoginRequest;
import com.fadedtumi.sillytavernaccount.dto.RefreshTokenRequest;
import com.fadedtumi.sillytavernaccount.dto.RegisterRequest;
import com.fadedtumi.sillytavernaccount.entity.RefreshToken;
import com.fadedtumi.sillytavernaccount.entity.User;
import com.fadedtumi.sillytavernaccount.repository.UserRepository;
import com.fadedtumi.sillytavernaccount.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private AppConfig appConfig;

    // 添加验证邀请码的方法
    private void validateInvitationCode(String code) {
        if (!appConfig.getInvitationCode().equals(code)) {
            throw new RuntimeException("邀请码无效");
        }
    }

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已被使用");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }

        // 在允许注册前，检查邮箱是否已通过验证
//        if (!emailVerificationService.isEmailVerified(registerRequest.getEmail())) {
//            throw new RuntimeException("邮箱未通过验证，请先验证您的邮箱所有权");
//        }

        // 添加: 验证邀请码
        validateInvitationCode(registerRequest.getInvitationCode());

        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRoles(new HashSet<>(Collections.singletonList("ROLE_USER")));
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // 获取用户信息
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("未找到用户"));

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 创建刷新令牌
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(
                jwt,
                refreshToken.getToken(),
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles()  // 添加角色信息
        );
    }

    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String requestRefreshToken = refreshTokenRequest.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = tokenProvider.generateTokenFromUsername(user.getUsername());
                    return new AuthResponse(
                            token,
                            requestRefreshToken,
                            "Bearer",
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getRoles()  // 添加角色信息
                    );
                })
                .orElseThrow(() -> new RuntimeException("刷新令牌不存在"));
    }

    @Transactional
    public void logoutUser(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    /**
     * 注册管理员用户（仅用于系统初始化）
     * @param registerRequest 注册请求
     * @param adminKey 管理员注册密钥
     * @return 创建的管理员用户
     */
    @Transactional
    public User registerAdminUser(RegisterRequest registerRequest, String adminKey) {
        // 验证管理员注册密钥
        if (!isValidAdminKey(adminKey)) {
            throw new RuntimeException("无效的管理员注册密钥");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已被使用");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }

        // 创建管理员用户
        User admin = new User();
        admin.setUsername(registerRequest.getUsername());
        admin.setEmail(registerRequest.getEmail());
        admin.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        admin.setFullName(registerRequest.getFullName());
        admin.setPhoneNumber(registerRequest.getPhoneNumber());

        // 设置管理员角色
        HashSet<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        roles.add("ROLE_ADMIN");
        admin.setRoles(roles);

        admin.setCreatedAt(LocalDateTime.now());

        return userRepository.save(admin);
    }

    /**
     * 验证管理员注册密钥
     * 注意：在生产环境中应使用更安全的验证方法
     */
    private boolean isValidAdminKey(String adminKey) {
        // 从配置中获取管理员密钥
        String expectedKey = "C1y3cv9qQMX9lqiK"; // 应该从配置文件中读取
        return expectedKey.equals(adminKey);
    }

    // 添加到AuthService类中
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("未找到用户: " + username));
    }
}