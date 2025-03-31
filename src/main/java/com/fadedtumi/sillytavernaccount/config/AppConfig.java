package com.fadedtumi.sillytavernaccount.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    // 从配置文件读取邀请码，如果没有则使用默认值
    @Value("${app.invitation.code:QWerTYuiOP}")
    private String invitationCode;

    public String getInvitationCode() {
        return invitationCode;
    }
}