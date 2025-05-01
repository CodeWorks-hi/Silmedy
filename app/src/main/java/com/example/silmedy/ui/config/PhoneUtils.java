package com.example.silmedy.ui.config;

public class PhoneUtils {
    public static String convertToE164Format(String phone) {
        String sanitized = phone.replace("-", "");
        if (sanitized.startsWith("0")) {
            return "+82" + sanitized.substring(1);
        } else {
            return sanitized;
        }
    }
}
