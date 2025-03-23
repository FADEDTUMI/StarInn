package com.fadedtumi.sillytavernaccount.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminPermissionController {

    @GetMapping("/check-permission")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkAdminPermission() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasPermission", true);
        return ResponseEntity.ok(response);
    }
}