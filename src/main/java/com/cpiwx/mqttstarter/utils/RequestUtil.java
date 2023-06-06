package com.cpiwx.mqttstarter.utils;

import lombok.Data;

@Data
public class RequestUtil {
    private static ThreadLocal<String> userInfo = new ThreadLocal<>();

    public static String getUserId() {
        return userInfo.get();
    }

    public static void setUserInfo(String userId) {
        userInfo.set(userId);
    }

    public static void clear() {
        userInfo.remove();
    }


}
