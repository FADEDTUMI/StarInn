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