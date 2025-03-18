package com.fadedtumi.sillytavernaccount.service;

import com.fadedtumi.sillytavernaccount.dto.ChangePasswordRequest;
import com.fadedtumi.sillytavernaccount.dto.UpdateUserRequest;
import com.fadedtumi.sillytavernaccount.entity.User;
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
}