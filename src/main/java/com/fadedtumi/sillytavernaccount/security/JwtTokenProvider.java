package com.fadedtumi.sillytavernaccount.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("roles", userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")))
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            logger.error("无法从令牌中获取用户名: {}", e.getMessage());
            return null;
        }
    }

    public Authentication getAuthentication(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String rolesString = claims.get("roles", String.class);
            Collection<? extends GrantedAuthority> authorities;

            if (rolesString != null && !rolesString.isEmpty()) {
                authorities = Arrays.stream(rolesString.split(","))
                        .filter(role -> !role.isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            } else {
                // 如果令牌中没有角色信息，至少给一个用户角色
                authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
                logger.warn("从令牌中找不到角色信息，使用默认的ROLE_USER");
            }

            User principal = new User(claims.getSubject(), "", authorities);
            logger.debug("已创建用户认证 - 用户名: {}, 角色: {}", claims.getSubject(), authorities);

            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        } catch (Exception e) {
            logger.error("无法创建认证对象: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            logger.error("无效的JWT签名: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("无效的JWT令牌: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("不支持的JWT令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT声明字符串为空: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("JWT验证发生未知错误: {}", e.getMessage());
        }

        return false;
    }

    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("roles", "ROLE_USER")  // 确保添加角色信息
                .signWith(key)
                .compact();
    }
}