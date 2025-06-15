package com.example.demo.Controller;

import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.APIResponse;
import com.example.demo.Util.JwtTokenUtil;
import com.example.demo.Model.User;
import com.example.demo.Service.CustomUserDetailsService;
import com.example.demo.Service.ECDHService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AdminController {

    private final CustomUserDetailsService customUserDetailsService;
    private final UserMapper userMapper;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private ECDHService dhService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(CustomUserDetailsService customUserDetailsService, UserMapper userMapper) {
        this.customUserDetailsService = customUserDetailsService;
        this.userMapper = userMapper;
    }

    /**
     * 处理管理员修改用户权限的请求
     *
     * @param requestBody 请求体，包含用户名（username）和新角色（role）的键值对
     * @return APIResponse<String> 包含请求处理的结果信息
     */
    @PostMapping("/update-user")
    @PreAuthorize("hasAuthority('Admin')")
    public APIResponse<String> modifyUserRole(@RequestBody Map<String, Object> requestBody) {

        Integer id = (Integer) requestBody.get("id");

        // 从请求体中提取用户名
        String username = (String) requestBody.get("username");

        // 从请求体中提取新角色
        String newRole = (String) requestBody.get("role");


        try {
            // 调用 CustomUserDetailsService 的 modifyUserRole 方法，根据用户名修改用户的角色信息
            customUserDetailsService.modifyUserRole(id, username, newRole);

            // 如果成功修改用户角色，返回成功响应，HTTP状态码为200
            return new APIResponse<>(200, "User role updated successfully", null);
        } catch (UsernameNotFoundException e) {
            // 如果用户名不存在，捕获 UsernameNotFoundException 异常，返回404错误和错误信息
            return new APIResponse<>(404, "User not found", null);
        } catch (Exception e) {
            // 捕获其他可能的异常，返回500错误和错误信息
            return new APIResponse<>(500, "An error occurred while updating user role", null);
        }
    }

    /**
     * 处理管理员删除用户的请求
     *
     * @param requestBody 请求体，包含用户名（username）的键值对
     * @return APIResponse<String> 包含请求处理的结果信息
     */
    @PostMapping("/delete-user")
    @PreAuthorize("hasAuthority('Admin')")
    public APIResponse<String> deleteUser(@RequestBody Map<String, Object> requestBody) {
        // 从请求体中提取用户名
        String username = (String) requestBody.get("username");

        try {
            // 调用 CustomUserDetailsService 的 deleteUser 方法，删除指定的用户
            customUserDetailsService.deleteUser(username);

            // 如果成功删除用户，返回成功响应，HTTP状态码为200
            return new APIResponse<>(200, "User deleted successfully", null);
        } catch (UsernameNotFoundException e) {
            // 如果用户名不存在，捕获 UsernameNotFoundException 异常，返回404错误和错误信息
            return new APIResponse<>(404, "User not found", null);
        } catch (Exception e) {
            // 捕获其他可能的异常，返回500错误和错误信息
            return new APIResponse<>(500, "An error occurred while deleting user", null);
        }
    }

    /**
     * 获取所有用户的用户名，ID，邮箱和权限
     *
     * @return APIResponse<List<Map<String, String>>> 包含所有用户的用户名、ID 和权限信息
     */
    @GetMapping("/allUsers")
    @PreAuthorize("hasAuthority('Admin')")
    public APIResponse<List<Map<String, Object>>> getAllUsers() {
        try {
            // 调用 CustomUserDetailsService 的方法获取所有用户
            List<User> users = customUserDetailsService.getAllUsers();
            users.sort(Comparator.comparingInt(User::getId));
            // 将用户信息转换为前端需要的格式
            List<Map<String, Object>> userInfos = users.stream().map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("role", user.getRole());
                return userInfo;
            }).collect(Collectors.toList());

            // 返回成功响应，HTTP 状态码为200
            return new APIResponse<>(200, "用户信息获取成功", userInfos);
        } catch (Exception e) {
            // 捕获异常，返回500错误和错误信息
            return new APIResponse<>(500, "用户信息获取失败", null);
        }
    }

    @PostMapping("/update-mpi")
    public APIResponse<String> updateMPI(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        try {
            int id = (int) requestBody.get("id");
            String username = (String) requestBody.get("username");
            String securityQuestion = (String) requestBody.get("securityQuestion");
            String securityAnswer = (String) requestBody.get("securityAnswer");
            String encryptedPassword = (String) requestBody.get("password");


            if(!Objects.equals(username, "")){
                userMapper.update1(username, id);
            }
            if(!Objects.equals(securityQuestion, "")){
                userMapper.update2(securityQuestion, securityAnswer, id);
            }
            if(!Objects.equals(encryptedPassword, "")){
                // 1. 从会话中获取先前存储的共享密钥
                byte[] sharedSecret = (byte[]) session.getAttribute("sharedSecret");

                // 如果共享密钥不存在，返回错误响应
                if (sharedSecret == null) {
                    return APIResponse.error(400, "共享密钥未找到，请重新进行密钥交换");
                }

                // 2. 使用共享密钥解密客户端发送的密码和公钥
                String decryptedPassword = dhService.decrypt(encryptedPassword, sharedSecret);

                String password = passwordEncoder.encode(decryptedPassword);
                userMapper.update3(password, id);
            }

            User user = userMapper.findByID(id);

            // 如果认证成功，生成 JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), id);
            return APIResponse.success(token);
        } catch (Exception e) {
        // 捕获异常，返回500错误和错误信息
        return new APIResponse<>(500, "用户信息修改失败", null);
    }

    }
}
