package com.fadedtumi.sillytavernaccount.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "system_settings")
@Data
public class SystemSettings {
    @Id
    private Long id;
    private String siteTitle;
    private String siteDescription;
    private boolean allowRegistration;
    private boolean requireEmailVerification;
    private boolean maintenanceMode;
    private int maxLoginAttempts;
    private int passwordExpiryDays;
    private int sessionTimeout;
}