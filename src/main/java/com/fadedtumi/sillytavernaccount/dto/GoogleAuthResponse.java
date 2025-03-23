package com.fadedtumi.sillytavernaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthResponse {
    private String tempToken;
    private String email;
    private String name;
    private String pictureUrl;
    private String googleId;
    private boolean newUser;
    private String suggestedUsername;
}