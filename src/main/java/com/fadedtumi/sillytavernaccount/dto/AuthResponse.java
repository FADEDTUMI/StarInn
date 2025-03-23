package com.fadedtumi.sillytavernaccount.dto;

import lombok.Data;

import java.util.Set;

@Data
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String tokenType;
    private Long id;
    private String username;
    private String email;
    private Set<String> roles; // 添加角色信息字段

    public AuthResponse(String token, String refreshToken, String tokenType, Long id, String username, String email, Set<String> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles; // 初始化角色字段
    }
}