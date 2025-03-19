package com.fadedtumi.sillytavernaccount.config;

import com.fadedtumi.sillytavernaccount.security.JwtAuthenticationFilter;
import com.fadedtumi.sillytavernaccount.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("配置 SecurityFilterChain");

        http
                // 禁用CSRF，因为我们使用JWT，不需要CSRF保护
                .csrf(csrf -> csrf.disable())
                // 启用CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 设置会话管理为无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权
                .authorizeHttpRequests(auth -> {
                    // 静态资源
                    auth.requestMatchers("/", "/index.html", "/google-login.html", "/verification-result.html",
                            "/account-deletion.html", "/css/**", "/js/**", "/favicon.ico").permitAll();
                    // 公共接口
                    auth.requestMatchers("/api/test/**").permitAll();
                    // 认证接口
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.requestMatchers("/api/email/**").permitAll();
                    // 对于OPTIONS请求全部放行（CORS预检请求）
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    // 用户资料API - 确保能访问
                    auth.requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers("/api/profile/**").hasAnyRole("USER", "ADMIN");
                    // 添加其他需要特别处理的路径
                    // ...
                    // 其他任何请求需要认证
                    auth.anyRequest().authenticated();

                    logger.info("安全规则配置完成");
                });

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        logger.info("JWT过滤器已添加");

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Auth-Token", "*"));
        configuration.setExposedHeaders(Arrays.asList("X-Auth-Token", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 预检请求结果缓存1小时

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        logger.info("CORS配置已创建");
        return source;
    }
}