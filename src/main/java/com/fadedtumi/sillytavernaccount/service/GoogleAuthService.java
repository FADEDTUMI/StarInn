package com.fadedtumi.sillytavernaccount.service;

import com.fadedtumi.sillytavernaccount.dto.AuthResponse;
import com.fadedtumi.sillytavernaccount.entity.RefreshToken;
import com.fadedtumi.sillytavernaccount.entity.User;
import com.fadedtumi.sillytavernaccount.repository.UserRepository;
import com.fadedtumi.sillytavernaccount.security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GoogleAuthService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Transactional
    public AuthResponse authenticateWithGoogle(String idTokenString) throws GeneralSecurityException, IOException {
        // 设置 Google ID Token 验证器
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        // 验证 ID Token
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            logger.error("无效的ID令牌");
            throw new RuntimeException("无效的ID令牌");
        }

        // 获取用户信息
        Payload payload = idToken.getPayload();
        String googleId = payload.getSubject();
        String email = payload.getEmail();
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");
        String givenName = (String) payload.get("given_name");
        String familyName = (String) payload.get("family_name");

        logger.info("Google用户信息: ID={}, Email={}, 已验证={}, 名称={}", googleId, email, emailVerified, name);

        // 如果邮箱未验证，拒绝登录
        if (!emailVerified) {
            throw new RuntimeException("Google邮箱未验证，无法登录");
        }

        // 查找或创建用户
        User user = findOrCreateGoogleUser(googleId, email, name, pictureUrl, givenName, familyName);

        // 更新用户最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 创建自定义身份验证对象
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(), "", authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT令牌
        String jwt = tokenProvider.generateTokenFromUsername(user.getUsername());

        // 创建刷新令牌
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(
                jwt,
                refreshToken.getToken(),
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    @Transactional
    protected User findOrCreateGoogleUser(String googleId, String email, String name,
                                          String pictureUrl, String givenName, String familyName) {
        // 首先通过 Google ID 查找用户
        Optional<User> userByGoogleId = userRepository.findAll().stream()
                .filter(u -> googleId.equals(u.getGoogleId()))
                .findFirst();

        if (userByGoogleId.isPresent()) {
            return userByGoogleId.get();
        }

        // 如果未找到，则尝试通过邮箱查找
        Optional<User> userByEmail = userRepository.findByEmail(email);

        if (userByEmail.isPresent()) {
            // 已存在该邮箱的用户，更新 Google ID 和个人资料
            User existingUser = userByEmail.get();
            existingUser.setGoogleId(googleId);
            existingUser.setProfileImageUrl(pictureUrl);
            return userRepository.save(existingUser);
        }

        // 如果都未找到，则创建新用户
        // 确保邮箱验证状态
        if (!emailVerificationService.isEmailVerified(email)) {
            // 创建并标记为已验证
            emailVerificationService.createVerification(email);
            emailVerificationService.markAsVerified(
                    emailVerificationService.findByEmail(email).orElseThrow(() ->
                            new RuntimeException("创建邮箱验证记录失败"))
            );
        }

        // 创建新用户名 (使用 name 并确保唯一性)
        String usernameBase = name.replaceAll("\\s+", "").toLowerCase();
        String username = usernameBase;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = usernameBase + counter++;
        }

        // 创建新用户
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setGoogleId(googleId);
        newUser.setProfileImageUrl(pictureUrl);
        newUser.setFullName(name);

        // 设置随机密码 (用户不需要知道，因为使用Google登录)
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        // 设置用户角色
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        newUser.setRoles(roles);

        newUser.setEnabled(true);
        newUser.setCreatedAt(LocalDateTime.now());

        return userRepository.save(newUser);
    }
}