package com.fadedtumi.sillytavernaccount.enums;

public enum DeviceType {
    ANDROID,
    IOS;

    public static boolean contains(String deviceType) {
        for (DeviceType type : values()) {
            if (type.name().equals(deviceType)) {
                return true;
            }
        }
        return false;
    }
}