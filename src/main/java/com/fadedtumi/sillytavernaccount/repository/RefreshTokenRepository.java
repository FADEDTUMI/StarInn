package com.fadedtumi.sillytavernaccount.repository;

import com.fadedtumi.sillytavernaccount.entity.RefreshToken;
import com.fadedtumi.sillytavernaccount.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);
}