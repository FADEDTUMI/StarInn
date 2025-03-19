package com.fadedtumi.sillytavernaccount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GoogleCompleteRegistrationRequest {
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "Google ID不能为空")
    private String googleId;

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含英文字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
            message = "密码必须至少包含一个数字、一个小写字母、一个大写字母，且长度不少于8位")
    private String password;

    private String name;
    private String pictureUrl;
}