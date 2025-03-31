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

import com.fadedtumi.sillytavernaccount.dto.AuthResponse;
import com.fadedtumi.sillytavernaccount.dto.GoogleAuthResponse;
import com.fadedtumi.sillytavernaccount.entity.RefreshToken;
import com.fadedtumi.sillytavernaccount.entity.User;
import com.fadedtumi.sillytavernaccount.repository.UserRepository;
import com.fadedtumi.sillytavernaccount.security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoogleAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${app.google.proxy.enabled:false}")
    private boolean proxyEnabled;

    @Value("${app.google.proxy.host:}")
    private String proxyHost;

    @Value("${app.google.proxy.port:0}")
    private int proxyPort;

    @Value("${app.google.proxy.type:HTTP}")
    private String proxyType;

    /**
     * 第一阶段：验证Google ID令牌并返回必要的用户信息
     */
    @Transactional
    public GoogleAuthResponse validateGoogleToken(String idTokenString) throws GeneralSecurityException, IOException {
        logger.info("开始验证Google ID令牌");

        // 设置HTTP代理（如果启用）
        HttpTransport transport;
        if (proxyEnabled && !proxyHost.isEmpty() && proxyPort > 0) {
            logger.info("使用代理: {}:{}", proxyHost, proxyPort);
            Proxy.Type type = "SOCKS".equalsIgnoreCase(proxyType) ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
            Proxy proxy = new Proxy(type, new InetSocketAddress(proxyHost, proxyPort));
            transport = new NetHttpTransport.Builder().setProxy(proxy).build();
        } else {
            transport = new NetHttpTransport();
        }

        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        // ���建验证器
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        // 验证ID令牌
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            logger.error("Google ID令牌验证失败");
            throw new RuntimeException("无效的Google ID令牌");
        }

        // 获取令牌载荷
        Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");
        String googleId = payload.getSubject();

        logger.info("Google登录成功 - 邮箱: {} (已验证: {})", email, emailVerified);

        // 处理未验证的邮箱
        if (!emailVerified) {
            logger.warn("Google邮箱未验证");
            throw new RuntimeException("您的Google邮箱未经验证，请先验证邮箱");
        }

        // 查找用户是否已存在
        Optional<User> existingUser = userRepository.findByEmail(email);
        boolean isNewUser = !existingUser.isPresent();

        // 生成临时令牌，包含Google认证信息（��单实现，实际应使用加密方式）
        String tempToken = jwtTokenProvider.generateGoogleAuthToken(email, googleId, name, pictureUrl);

        // 根据是否为新用户，返回不同的建议用户名
        String suggestedUsername = isNewUser ? email.split("@")[0] : existingUser.get().getUsername();

        // 返回到前端的Google认证响应
        return new GoogleAuthResponse(
                tempToken,
                email,
                name,
                pictureUrl,
                googleId,
                isNewUser,
                suggestedUsername
        );
    }

    /**
     * 第二阶段：完成用户注册或登录流程
     */
    @Transactional
    public AuthResponse completeGoogleAuth(String tempToken, String username, String password) {
        // 解析临时令牌
        Map<String, String> googleInfo = jwtTokenProvider.parseGoogleAuthToken(tempToken);
        if (googleInfo == null) {
            throw new RuntimeException("无效的临时令牌");
        }

        String email = googleInfo.get("email");
        String googleId = googleInfo.get("googleId");
        String name = googleInfo.get("name");
        String pictureUrl = googleInfo.get("pictureUrl");

        // 验证用户名格式
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            throw new RuntimeException("用户名只能包含英文字母和数字");
        }

        // 检查用户名是否被使用（排除当前用户）
        Optional<User> userByUsername = userRepository.findByUsername(username);
        Optional<User> userByEmail = userRepository.findByEmail(email);

        if (userByUsername.isPresent() && (userByEmail.isEmpty() || !userByUsername.get().getId().equals(userByEmail.get().getId()))) {
            throw new RuntimeException("用户名已被使用");
        }

        User user;
        if (userByEmail.isPresent()) {
            // 更新现有用户
            user = userByEmail.get();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setLastLogin(LocalDateTime.now());
            user.setGoogleId(googleId);
            user.setProfileImageUrl(pictureUrl);
            user.setUpdatedAt(LocalDateTime.now());
        } else {
            // 创建新用户
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(name);
            user.setEnabled(true);
            user.setGoogleId(googleId);
            user.setProfileImageUrl(pictureUrl);
            user.setCreatedAt(LocalDateTime.now());
            user.setLastLogin(LocalDateTime.now());

            // 设置角色
            Set<String> roles = new HashSet<>();
            roles.add("ROLE_USER");
            user.setRoles(roles);
        }

        user = userRepository.save(user);
        logger.info("Google认证用户已{}保存 - 用户名: {}", userByEmail.isPresent() ? "更新" : "创建", user.getUsername());

        // 创建用户详情对象
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 创建UserDetails对象
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                authorities
        );

        // 使用UserDetails对象创建认证令牌
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT令牌
        String jwt = jwtTokenProvider.generateToken(authentication);

        // 创建刷新令牌
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        // 返回认证响应对象
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

    /**
     * 已有用户的Google登录流程
     * 用于直接登录已关联的Google账户
     */
    @Transactional
    public AuthResponse loginWithGoogle(String tempToken) {
        // 解析临时令牌
        Map<String, String> googleInfo = jwtTokenProvider.parseGoogleAuthToken(tempToken);
        if (googleInfo == null) {
            throw new RuntimeException("无效的临时令牌");
        }

        String email = googleInfo.get("email");
        String googleId = googleInfo.get("googleId");
        String pictureUrl = googleInfo.get("pictureUrl");

        // 查找用户是否存在
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("找不到与此Google账号关联的用户，请先注册");
        }

        User user = optionalUser.get();

        // 更新用户的Google ID和头像URL（如果有变化）
        boolean updated = false;
        if (user.getGoogleId() == null || !user.getGoogleId().equals(googleId)) {
            user.setGoogleId(googleId);
            updated = true;
        }

        if (pictureUrl != null && (user.getProfileImageUrl() == null || !user.getProfileImageUrl().equals(pictureUrl))) {
            user.setProfileImageUrl(pictureUrl);
            updated = true;
        }

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());

        if (updated) {
            user.setUpdatedAt(LocalDateTime.now());
        }

        user = userRepository.save(user);
        logger.info("用户通过Google登录成功: {}", user.getUsername());

        // 创建用户详情对象
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 创建认证对象
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                authorities
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT令牌
        String jwt = jwtTokenProvider.generateToken(authentication);

        // 创建刷新令牌
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        // 返回认证响应对象
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
}