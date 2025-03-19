package com.fadedtumi.sillytavernaccount.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class EmailOAuthConfig {

    @Value("${app.oauth2.qq.client-id}")
    private String qqClientId;

    @Value("${app.oauth2.qq.client-secret}")
    private String qqClientSecret;

    @Value("${app.oauth2.netease.client-id}")
    private String neteaseClientId;

    @Value("${app.oauth2.netease.client-secret}")
    private String neteaseClientSecret;

    @Value("${app.oauth2.aliyun.client-id}")
    private String aliyunClientId;

    @Value("${app.oauth2.aliyun.client-secret}")
    private String aliyunClientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();

        // QQ邮箱OAuth配置
        registrations.add(ClientRegistration.withRegistrationId("qq")
                .clientId(qqClientId)
                .clientSecret(qqClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/api/email/verify/oauth2/code/qq")
                .scope("email", "openid")
                .authorizationUri("https://graph.qq.com/oauth2.0/authorize")
                .tokenUri("https://graph.qq.com/oauth2.0/token")
                .userInfoUri("https://graph.qq.com/oauth2.0/me")
                .userNameAttributeName("openid")
                .clientName("QQ")
                .build());

        // 网易邮箱OAuth配置
        registrations.add(ClientRegistration.withRegistrationId("netease")
                .clientId(neteaseClientId)
                .clientSecret(neteaseClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/api/email/verify/oauth2/code/netease")
                .scope("email")
                .authorizationUri("https://reg.163.com/oauth2/authorize")
                .tokenUri("https://reg.163.com/oauth2/token")
                .userInfoUri("https://reg.163.com/oauth2/userinfo")
                .userNameAttributeName("email")
                .clientName("网易")
                .build());

        // 阿里邮箱OAuth配置
        registrations.add(ClientRegistration.withRegistrationId("aliyun")
                .clientId(aliyunClientId)
                .clientSecret(aliyunClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/api/email/verify/oauth2/code/aliyun")
                .scope("email")
                .authorizationUri("https://auth.aliyun.com/authorize")
                .tokenUri("https://auth.aliyun.com/token")
                .userInfoUri("https://auth.aliyun.com/userinfo")
                .userNameAttributeName("email")
                .clientName("阿里")
                .build());

        return new InMemoryClientRegistrationRepository(registrations);
    }
}