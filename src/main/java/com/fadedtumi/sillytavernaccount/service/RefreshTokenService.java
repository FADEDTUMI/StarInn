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

import com.fadedtumi.sillytavernaccount.entity.RefreshToken;
import com.fadedtumi.sillytavernaccount.entity.User;
import com.fadedtumi.sillytavernaccount.repository.RefreshTokenRepository;
import com.fadedtumi.sillytavernaccount.repository.UserRepository;
import com.fadedtumi.sillytavernaccount.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        // 获取用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("未找到用户ID: " + userId));

        // 添加日志
        System.out.println("正在为用户ID:" + userId + "创建刷新令牌");

        // ��查用户是否已有刷新令牌
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            System.out.println("发现用户ID:" + userId + "的现有令牌，正在删除...");
            refreshTokenRepository.delete(existingToken.get());
            refreshTokenRepository.flush();
            System.out.println("删除完成");
        }

        // 创建新的刷新令牌
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        // 保存并返回新令牌
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        System.out.println("为用户ID:" + userId + "成功创建新令牌");
        return saved;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("刷新令牌已过期，请重新登录");
        }

        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("未找到用户"));
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * 从用户名生成JWT令牌
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateJwtToken(String username) {
        return jwtTokenProvider.generateTokenFromUsername(username);
    }
}