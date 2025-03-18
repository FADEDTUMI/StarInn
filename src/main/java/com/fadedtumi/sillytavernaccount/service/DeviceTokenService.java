package com.fadedtumi.sillytavernaccount.service;

import com.fadedtumi.sillytavernaccount.entity.DeviceToken;
import com.fadedtumi.sillytavernaccount.entity.User;
import com.fadedtumi.sillytavernaccount.enums.DeviceType;
import com.fadedtumi.sillytavernaccount.repository.DeviceTokenRepository;
import com.fadedtumi.sillytavernaccount.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceTokenService {

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void saveDeviceToken(String username, String token, String deviceType) {
        // 验证设备类型
        if (!DeviceType.contains(deviceType)) {
            throw new RuntimeException("无效的设备类型，必须是 ANDROID 或 IOS");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("未找到用户"));

        // 检查是否已存在相同的令牌
        Optional<DeviceToken> existingToken = deviceTokenRepository.findByToken(token);
        if (existingToken.isPresent()) {
            // 如果存在，更新用户信息
            DeviceToken deviceToken = existingToken.get();
            deviceToken.setUser(user);
            deviceToken.setDeviceType(deviceType);
            deviceTokenRepository.save(deviceToken);
        } else {
            // 否则创建新的记录
            DeviceToken newToken = new DeviceToken();
            newToken.setToken(token);
            newToken.setUser(user);
            newToken.setDeviceType(deviceType);
            deviceTokenRepository.save(newToken);
        }
    }

    @Transactional
    public void removeDeviceToken(String token) {
        deviceTokenRepository.deleteByToken(token);
    }

    @Transactional
    public void removeUserDeviceTokens(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("未找到用户"));
        deviceTokenRepository.deleteByUser(user);
    }

    public List<DeviceToken> getUserDeviceTokens(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("未找到用户"));
        return deviceTokenRepository.findByUser(user);
    }

    public Optional<DeviceToken> findByToken(String token) {
        return deviceTokenRepository.findByToken(token);
    }
}