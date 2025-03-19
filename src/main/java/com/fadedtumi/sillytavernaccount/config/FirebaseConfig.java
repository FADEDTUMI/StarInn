package com.fadedtumi.sillytavernaccount.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(
                                new ClassPathResource("firebase-service-account.json").getInputStream()))
                        .build();
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase 初始化成功");
            }
        } catch (IOException e) {
            System.err.println("Firebase 初始化失败: " + e.getMessage());
        }
    }
}