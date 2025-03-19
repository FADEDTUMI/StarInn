package com.fadedtumi.sillytavernaccount.controller;

import com.fadedtumi.sillytavernaccount.dto.AuthResponse;
import com.fadedtumi.sillytavernaccount.dto.GoogleLoginRequest;
import com.fadedtumi.sillytavernaccount.service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api/auth/google-oauth")  // 修改后的URL路径
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody GoogleLoginRequest loginRequest) {
        try {
            AuthResponse authResponse = googleAuthService.authenticateWithGoogle(loginRequest.getIdToken());
            return ResponseEntity.ok(authResponse);
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.badRequest().body("Google认证失败: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}