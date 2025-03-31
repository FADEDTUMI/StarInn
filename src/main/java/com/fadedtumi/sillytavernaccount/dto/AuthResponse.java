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