package com.example.demo.Config;

import com.example.demo.Filter.ApiSignatureFilter;
import com.example.demo.Filter.JwtAuthenticationTokenFilter;
import com.example.demo.Util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final DaoAuthenticationProvider authenticationProvider;

    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    @Autowired
    private ApiSignatureFilter apiSignatureFilter; // 注入新过滤器

    // 通过构造函数注入 DaoAuthenticationProvider
    @Autowired
    public SecurityConfig(DaoAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    // 配置身份验证管理器，设置使用 DaoAuthenticationProvider 进行身份验证
    @Autowired
    public void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests(auth -> auth
                        .antMatchers("/exchange-keys","/login","/register","/forget_password").permitAll() // 允许这些路径公开访问
                        .anyRequest().authenticated() // 其余请求需登录认证
                )
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(apiSignatureFilter, JwtAuthenticationTokenFilter.class)
                .logout().addLogoutHandler(
                        (httpServletRequest, httpServletResponse,
                         authentication)->{
                            HttpSession session = httpServletRequest.getSession(false);// 通过 false 防止创建新的 session
                            if (session != null) {
                                session.removeAttribute("sharedSecret");
                            }
                            UserContext.clear();
                            // 删除 JSESSIONID Cookie
                            Cookie jsessionidCookie = new Cookie("JSESSIONID", null);
                            jsessionidCookie.setPath("/");          // 必须匹配原来的 Path
                            jsessionidCookie.setMaxAge(0);          // 0 表示立即删除
                            httpServletResponse.addCookie(jsessionidCookie);
                            SecurityContextHolder.clearContext();
                            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                            httpServletResponse.setContentType("application/json;charset=UTF-8");
                            try {
                                httpServletResponse.getWriter().write("{\"success\": \"退出登录成功\"}");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).logoutSuccessUrl("/dataflow5"); // 允许注销操作

        return http.build();
    }
}
