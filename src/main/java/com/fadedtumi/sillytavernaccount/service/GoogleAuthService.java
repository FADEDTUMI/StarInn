package com.fadedtumi.sillytavernaccount.service;

import com.fadedtumi.sillytavernaccount.dto.AuthResponse;
import com.fadedtumi.sillytavernaccount.entity.RefreshToken;
import com.fadedtumi.sillytavernaccount.entity.User;
import com.fadedtumi.sillytavernaccount.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleAuthService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Map<String, Object> authenticateWithGoogle(String idTokenString) throws GeneralSecurityException, IOException {
        HttpTransport transport;

        // 如果启用了代理，创建带代理的HttpTransport
        if (proxyEnabled && !proxyHost.isEmpty() && proxyPort > 0) {
            logger.info("使用代理连接Google: {}:{} 类型: {}", proxyHost, proxyPort, proxyType);
            Proxy.Type type = "SOCKS".equalsIgnoreCase(proxyType) ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
            Proxy proxy = new Proxy(type, new InetSocketAddress(proxyHost, proxyPort));
            transport = new NetHttpTransport.Builder().setProxy(proxy).build();
        } else {
            transport = new NetHttpTransport();
        }

        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new RuntimeException("无效的Google ID token");
        }

        Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        // 查找是否已有此邮箱的用户
        User user = userRepository.findByEmail(email).orElse(null);

        Map<String, Object> result = new HashMap<>();

        if (user == null) {
            // 新用户，需要设置用户名和密码
            result.put("isNewUser", true);
            result.put("email", email);
            result.put("googleId", googleId);
            result.put("name", name);
            result.put("pictureUrl", pictureUrl);
            result.put("suggestedUsername", email.split("@")[0]);
        } else {
            // 已有用户，更新Google信息
            user.setGoogleId(googleId);
            user.setFullName(name);
            user.setProfileImageUrl(pictureUrl);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // 创建刷新令牌
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
            // 创建JWT Token
            String token = refreshTokenService.generateJwtToken(user.getUsername());

            // 返回登录成功信息
            result.put("isNewUser", false);
            result.put("authResponse", new AuthResponse(
                    token,
                    refreshToken.getToken(),
                    "Bearer",
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()
            ));
        }

        return result;
    }

    @Transactional
    public AuthResponse completeGoogleRegistration(String email, String googleId, String username,
                                                   String password, String name, String pictureUrl) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已被使用");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setFullName(name);
        user.setProfileImageUrl(pictureUrl);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(new java.util.HashSet<>(Collections.singletonList("ROLE_USER")));
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true);

        userRepository.save(user);

        // 创建刷新令牌
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        // 创建JWT Token
        String token = refreshTokenService.generateJwtToken(user.getUsername());

        return new AuthResponse(
                token,
                refreshToken.getToken(),
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}