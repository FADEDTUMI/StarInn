package com.fadedtumi.sillytavernaccount.repository;

import com.fadedtumi.sillytavernaccount.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    // 添加这些方法到现有的 UserRepository 接口中
    long countByEnabled(boolean enabled);

    long countByGoogleIdIsNotNull();

    List<User> findTop5ByOrderByCreatedAtDesc();

}