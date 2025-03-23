package com.fadedtumi.sillytavernaccount.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                logger.info("开始初始化Firebase...");

                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                if (!resource.exists()) {
                    logger.error("Firebase服务账号文件不存在");
                    return;
                }

                try (InputStream serviceAccount = resource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    FirebaseApp.initializeApp(options);
                    logger.info("Firebase初始化成功");
                }
            } else {
                logger.info("Firebase已经初始化");
            }
        } catch (IOException e) {
            logger.error("Firebase初始化失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Firebase初始化过程中发生未知错误: " + e.getMessage(), e);
        }
    }
}