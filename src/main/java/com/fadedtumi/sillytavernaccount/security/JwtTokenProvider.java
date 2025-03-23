package com.fadedtumi.sillytavernaccount.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.temp-expiration:300000}") // 默认5分钟
    private long tempTokenExpirationInMs;

    private Key signingKey;

    // 获取签名密钥，确保使用足够长度的密钥
    private Key getSigningKey() {
        if (signingKey == null) {
            try {
                // 尝试从配置的密钥创建
                byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
                signingKey = Keys.hmacShaKeyFor(keyBytes);
            } catch (Exception e) {
                logger.warn("无法使用配置的JWT密钥，将生成新的安全密钥: {}", e.getMessage());
                // 如果配置的密钥不可用，则生成一个安全的密钥
                signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            }
        }
        return signingKey;
    }

    // 生成JWT令牌
    public String generateToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(username)
                .claim("auth", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 从用户名生成JWT令牌
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // 获取用户详情
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 获取用户权限
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(username)
                .claim("auth", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 为Google认证生成临时令牌
    public String generateGoogleAuthToken(String email, String googleId, String name, String pictureUrl) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tempTokenExpirationInMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("googleId", googleId)
                .claim("name", name)
                .claim("pictureUrl", pictureUrl)
                .claim("type", "google_auth")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 从JWT令牌中获取认证信息
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();

        // 从JWT中获取用户权限
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(
                        claims.get("auth", String.class).split(","))
                .filter(auth -> !auth.trim().isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 加载用户详情
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 创建认证对象
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    // 解析Google认证临时令牌
    public Map<String, String> parseGoogleAuthToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tokenType = claims.get("type", String.class);
            if (!"google_auth".equals(tokenType)) {
                return null;
            }

            Map<String, String> result = new HashMap<>();
            result.put("email", claims.getSubject());
            result.put("googleId", claims.get("googleId", String.class));
            result.put("name", claims.get("name", String.class));
            result.put("pictureUrl", claims.get("pictureUrl", String.class));

            return result;
        } catch (Exception e) {
            logger.error("无法解析Google临时令牌: {}", e.getMessage());
            return null;
        }
    }

    // 从JWT令牌中获取用户名
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // 验证JWT令牌
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("无效的JWT令牌");
        } catch (ExpiredJwtException ex) {
            logger.error("JWT令牌已过期");
        } catch (UnsupportedJwtException ex) {
            logger.error("不支持的JWT令牌");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims字符串为空");
        }
        return false;
    }
}