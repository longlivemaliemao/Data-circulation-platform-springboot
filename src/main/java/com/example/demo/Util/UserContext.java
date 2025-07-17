package com.example.demo.Util;

import org.springframework.stereotype.Component;

@Component
public class UserContext {

    // ThreadLocal 用于为每个线程单独存储用户信息
    private static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();

    // 设置用户名（通常在拦截器中调用）
    public static void setUsername(String username) {
        USERNAME_HOLDER.set(username);
    }

    // 获取当前线程中存储的用户名
    public static String getUsername() {
        return USERNAME_HOLDER.get();
    }

    // 清除用户名，避免线程复用导致内存泄漏（务必在请求结束时调用）
    public static void clear() {
        USERNAME_HOLDER.remove();
    }
}