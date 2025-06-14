package com.example.demo.Service;

import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    // 通过构造函数注入 UserMapper，方便查询用户数据
    public CustomUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // 根据用户名加载用户的详细信息，实现 UserDetailsService 接口的方法
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        // 如果用户不存在，抛出 UsernameNotFoundException 异常
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        // 使用 Spring Security 提供的 User 构建 UserDetails 对象
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole()) // 设置用户的角色信息
                .build();
    }

    // 保存用户信息的方法
    public void saveUser(User user) {
        userMapper.insert(user);
    }

    // 存储用户的共享密钥
    public void storeSharedSecret(String username, String sharedSecret) {
        User user = userMapper.findByUsername(username);
        if (user != null) {
            user.setShared_secret(sharedSecret);
            userMapper.update(user); // 更新用户的共享密钥
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    // 修改用户角色
    public void modifyUserRole(Integer id, String username, String newRole) {
        User user = userMapper.findByID(id);
        if (user != null) {
            user.setUsername(username);
            user.setRole(newRole);
            if(user.getShared_secret() == null){
                user.setShared_secret("");
            }
            System.out.println(user);
            userMapper.update(user); // 更新用户角色信息
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    // 删除用户
    public void deleteUser(String username) {
        User user = userMapper.findByUsername(username);
        if (user != null) {
            userMapper.deleteByUsername(username);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    // 通过用户名查找用户权限（角色）
    public String findUserRoleByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user != null) {
            return user.getRole();
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    // 通过用户名查找用户id
    public int findUserIDByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user != null) {
            return user.getId();
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    /**
     * 获取所有用户
     *
     * @return List<User> 所有用户信息的列表
     */
    public List<User> getAllUsers() {
        return userMapper.findAllUsers();
    }
}
