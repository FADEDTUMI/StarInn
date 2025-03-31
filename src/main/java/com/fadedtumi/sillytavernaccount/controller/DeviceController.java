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