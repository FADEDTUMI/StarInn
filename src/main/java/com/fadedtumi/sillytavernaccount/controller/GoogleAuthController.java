package com.fadedtumi.sillytavernaccount.controller;

import com.fadedtumi.sillytavernaccount.dto.AuthResponse;
import com.fadedtumi.sillytavernaccount.dto.GoogleCompleteRegistrationRequest;
import com.fadedtumi.sillytavernaccount.dto.GoogleLoginRequest;
import com.fadedtumi.sillytavernaccount.service.GoogleAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/google-oauth")
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody GoogleLoginRequest request) {
        try {
            Map<String, Object> response = googleAuthService.authenticateWithGoogle(request.getIdToken());
            return ResponseEntity.ok(response);
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Google 认证失败: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/complete-registration")
    public ResponseEntity<?> completeGoogleRegistration(@Valid @RequestBody GoogleCompleteRegistrationRequest request) {
        try {
            AuthResponse response = googleAuthService.completeGoogleRegistration(
                    request.getEmail(),
                    request.getGoogleId(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getName(),
                    request.getPictureUrl()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }
}