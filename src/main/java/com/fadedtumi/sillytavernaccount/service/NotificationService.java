package com.fadedtumi.sillytavernaccount.service;

import com.fadedtumi.sillytavernaccount.entity.DeviceToken;
import com.fadedtumi.sillytavernaccount.enums.DeviceType;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private DeviceTokenService deviceTokenService;

    public void sendNotificationToUser(String username, String title, String body) {
        List<DeviceToken> deviceTokens = deviceTokenService.getUserDeviceTokens(username);

        for (DeviceToken deviceToken : deviceTokens) {
            if (DeviceType.ANDROID.name().equals(deviceToken.getDeviceType())) {
                sendAndroidNotification(deviceToken.getToken(), title, body, new HashMap<>());
            } else if (DeviceType.IOS.name().equals(deviceToken.getDeviceType())) {
                sendIosNotification(deviceToken.getToken(), title, body, new HashMap<>());
            }
        }
    }

    private void sendAndroidNotification(String token, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("成功发送Android通知: " + response);
        } catch (FirebaseMessagingException e) {
            System.err.println("无法发送Android通知: " + e.getMessage());
        }
    }

    private void sendIosNotification(String token, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("成功发送iOS通知: " + response);
        } catch (FirebaseMessagingException e) {
            System.err.println("无法发送iOS通知: " + e.getMessage());
        }
    }
}