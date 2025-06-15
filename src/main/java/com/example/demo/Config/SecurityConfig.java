package com.example.demo.Config;

import com.example.demo.Filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final DaoAuthenticationProvider authenticationProvider;

    // 通过构造函数注入 DaoAuthenticationProvider
    @Autowired
    public SecurityConfig(DaoAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    // 配置身份验证管理器，设置使用 DaoAuthenticationProvider 进行身份验证
    @Autowired
    public void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 禁用 CSRF
                .authorizeRequests(auth -> auth
                        .antMatchers("/exchange-keys","/login","/register","/forget_password").permitAll() // 允许这些路径公开访问
                        .anyRequest().authenticated() // 其余请求需登录认证
                )
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(LogoutConfigurer::permitAll); // 允许注销操作

        return http.build();
    }
}
