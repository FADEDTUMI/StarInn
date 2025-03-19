package com.fadedtumi.sillytavernaccount.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    @NotBlank(message = "ID Token不能为空")
    private String idToken;
}