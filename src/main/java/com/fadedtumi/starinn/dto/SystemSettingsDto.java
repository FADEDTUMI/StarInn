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
package com.fadedtumi.starinn.dto;

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