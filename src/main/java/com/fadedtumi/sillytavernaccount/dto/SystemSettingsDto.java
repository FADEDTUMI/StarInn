package com.fadedtumi.sillytavernaccount.dto;

import lombok.Data;

@Data
public class SystemSettingsDto {
    private String siteTitle;
    private String siteDescription;
    private boolean allowRegistration;
    private boolean requireEmailVerification;
    private boolean maintenanceMode;
    private int maxLoginAttempts;
    private int passwordExpiryDays;
    private int sessionTimeout;
}