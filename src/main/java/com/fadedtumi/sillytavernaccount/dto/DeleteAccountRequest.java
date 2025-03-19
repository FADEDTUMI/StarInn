package com.fadedtumi.sillytavernaccount.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequest {
    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "确认信息不能为空")
    private String confirmation;
}