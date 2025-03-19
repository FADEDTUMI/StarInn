package com.fadedtumi.sillytavernaccount.repository;

import com.fadedtumi.sillytavernaccount.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmail(String email);
    Optional<EmailVerification> findByVerificationToken(String token);
    Optional<EmailVerification> findByOauthState(String state);
    boolean existsByEmailAndVerified(String email, boolean verified);
}