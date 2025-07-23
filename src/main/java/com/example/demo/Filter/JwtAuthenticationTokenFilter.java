package com.example.demo.Filter;

import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.User;
import com.example.demo.Util.JwtTokenUtil;
import com.example.demo.Util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 从请求头中获取token
        String token = request.getHeader("authToken");

        // 如果token为空，则直接放行
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 验证token并获取用户名
            String username = jwtTokenUtil.validateToken(token);
            if (username == null) {
                throw new IllegalArgumentException("非法的token");
            }

            // 查询用户信息
            User user = userMapper.findByUsername(username);
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }

            // 设置用户权限
            List<GrantedAuthority> authorities = new ArrayList<>();
            String role = user.getRole();
            if (role != null && !role.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority(role));
            }

            // 创建认证对象并存入SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserContext.setUsername(username);
            // 放行请求
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 记录错误日志
            logger.error("Token验证失败: {}", e.getMessage());

            // 返回401未授权状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"无效的token\"}");
        }
    }
}