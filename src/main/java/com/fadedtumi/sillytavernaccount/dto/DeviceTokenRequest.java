package com.fadedtumi.sillytavernaccount.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceTokenRequest {
    @NotBlank(message = "设备令牌不能为空")
    private String token;

    @NotBlank(message = "设备类型不能为空")
    private String deviceType;
}