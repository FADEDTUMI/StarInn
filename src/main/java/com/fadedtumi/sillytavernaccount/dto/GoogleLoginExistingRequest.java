package com.fadedtumi.sillytavernaccount.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginExistingRequest {
    @NotBlank(message = "临时令牌不能为空")
    private String tempToken;
}