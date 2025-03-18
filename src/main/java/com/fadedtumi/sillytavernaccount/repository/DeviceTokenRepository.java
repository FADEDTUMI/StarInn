package com.fadedtumi.sillytavernaccount.repository;

import com.fadedtumi.sillytavernaccount.entity.DeviceToken;
import com.fadedtumi.sillytavernaccount.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByToken(String token);
    List<DeviceToken> findByUser(User user);
    void deleteByToken(String token);
    void deleteByUser(User user);
}