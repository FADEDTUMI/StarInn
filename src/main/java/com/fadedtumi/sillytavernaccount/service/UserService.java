package com.fadedtumi.sillytavernaccount.service;

import com.fadedtumi.sillytavernaccount.dto.ChangePasswordRequest;
import com.fadedtumi.sillytavernaccount.dto.UpdateUserRequest;
import com.fadedtumi.sillytavernaccount.entity.User;
import com.fadedtumi.sillytavernaccount.repository.DeviceTokenRepository;
import com.fadedtumi.sillytavernaccount.repository.RefreshTokenRepository;
import com.fadedtumi.sillytavernaccount.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("未找到用户"));
    }

    @Transactional
    public User updateUserProfile(String username, UpdateUserRequest updateRequest) {
        User user = getUserByUsername(username);

        // 如果要更新邮箱，检查邮箱是否已存在
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new RuntimeException("邮箱已被使用");
            }
            user.setEmail(updateRequest.getEmail());
        }

        if (updateRequest.getFullName() != null) {
            user.setFullName(updateRequest.getFullName());
        }

        if (updateRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest passwordRequest) {
        User user = getUserByUsername(username);

        // 验证当前密码是否正确
        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("当前密码不正确");
        }

        // 验证两次输入的新密码是否一致
        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())) {
            throw new RuntimeException("两次输入的新密码不一致");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 注销用户账���
     * @param username 用户名
     * @param password 当前密码（用于验证）
     */
    @Transactional
    public void deleteUserAccount(String username, String password) {
        User user = getUserByUsername(username);

        // 验证密码是否正确
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码不正确，无法注销账户");
        }

        // 删除用户相关的所有数据
        // 1. 删除刷新令牌
        refreshTokenRepository.deleteByUser(user);

        // 2. 删除设备令牌
        deviceTokenRepository.deleteByUser(user);

        // 3. 最后删除用户��身
        userRepository.delete(user);
    }
}