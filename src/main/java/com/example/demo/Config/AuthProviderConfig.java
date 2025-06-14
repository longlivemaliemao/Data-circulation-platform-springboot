package com.example.demo.Config;

import com.example.demo.Service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthProviderConfig {

    private final CustomUserDetailsService customUserDetailsService;

    // 通过构造函数注入 CustomUserDetailsService
    public AuthProviderConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    // 定义一个密码编码器 Bean，用于加密和解密密码
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 定义一个 DaoAuthenticationProvider Bean，用于身份验证
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 设置用于获取用户详细信息的服务
        authProvider.setUserDetailsService(customUserDetailsService);
        // 设置用于密码加密的编码器
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
