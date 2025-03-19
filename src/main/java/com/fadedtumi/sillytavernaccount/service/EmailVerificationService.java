package com.fadedtumi.sillytavernaccount.service;

import com.fadedtumi.sillytavernaccount.entity.EmailVerification;
import com.fadedtumi.sillytavernaccount.repository.EmailVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailVerificationService {

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Value("${app.email.verification.expiration-minutes:30}")
    private int expirationMinutes;

    /**
     * 创建一个新的邮箱验证请求
     */
    @Transactional
    public EmailVerification createVerification(String email) {
        // 检查是否已存在此邮箱的验证记录
        Optional<EmailVerification> existingVerification = emailVerificationRepository.findByEmail(email);

        EmailVerification verification;
        if (existingVerification.isPresent()) {
            verification = existingVerification.get();
            // 如果已经验证过，则直接返回
            if (verification.isVerified()) {
                return verification;
            }
        } else {
            verification = new EmailVerification();
            verification.setEmail(email);
            verification.setVerified(false);
            verification.setCreatedAt(LocalDateTime.now());
        }

        // 生成新的验证令牌和OAuth状态
        verification.setVerificationToken(UUID.randomUUID().toString());
        verification.setOauthState(UUID.randomUUID().toString());

        return emailVerificationRepository.save(verification);
    }

    /**
     * 根据OAuth状态参数查找验证记录
     */
    public Optional<EmailVerification> findByOAuthState(String state) {
        return emailVerificationRepository.findByOauthState(state);
    }

    /**
     * 标记邮箱已验证
     */
    @Transactional
    public EmailVerification markAsVerified(EmailVerification verification) {
        verification.setVerified(true);
        verification.setVerifiedAt(LocalDateTime.now());
        return emailVerificationRepository.save(verification);
    }

    /**
     * 检查邮箱是否已验证
     */
    public boolean isEmailVerified(String email) {
        return emailVerificationRepository.existsByEmailAndVerified(email, true);
    }

    /**
     * 根据验证令牌查找验证记录
     */
    public Optional<EmailVerification> findByToken(String token) {
        return emailVerificationRepository.findByVerificationToken(token);
    }

    /**
     * 检查验证是否过期
     */
    public boolean isVerificationExpired(EmailVerification verification) {
        LocalDateTime expirationTime = verification.getCreatedAt().plusMinutes(expirationMinutes);
        return LocalDateTime.now().isAfter(expirationTime);
    }
    public Optional<EmailVerification> findByEmail(String email) {
        return emailVerificationRepository.findByEmail(email);
    }
}