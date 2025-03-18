package com.fadedtumi.sillytavernaccount.controller;

import com.fadedtumi.sillytavernaccount.dto.DeviceTokenRequest;
import com.fadedtumi.sillytavernaccount.service.DeviceTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceTokenService deviceTokenService;

    @PostMapping("/register")
    public ResponseEntity<?> registerDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        deviceTokenService.saveDeviceToken(username, request.getToken(), request.getDeviceType());

        return ResponseEntity.ok(Collections.singletonMap("message", "设备令牌注册成功"));
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<?> unregisterDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        deviceTokenService.removeDeviceToken(request.getToken());

        return ResponseEntity.ok(Collections.singletonMap("message", "设备令牌注销成功"));
    }
}