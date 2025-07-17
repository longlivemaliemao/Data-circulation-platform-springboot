package com.example.demo.Controller;

import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.APIResponse;
import com.example.demo.Service.ECDHService;
import com.example.demo.Util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/user")
public class HomeController {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ECDHService dhService;
    // 注入 PasswordEncoder，用于对密码进行加密和验证

    @Autowired
    private PasswordEncoder passwordEncoder;
    /**
     * 根据用户名模糊查询用户
     * @param username 用户名搜索关键字
     * @return 匹配的用户列表
     */
    @GetMapping("/search")
    public APIResponse<List<Map<String, String>>> searchUsers(@RequestParam String username) {
        try {
            List<Map<String, String>> users = userMapper.findUsersByUsername2(username);
            return APIResponse.success(users);
        } catch (Exception e) {
            return APIResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/key-status")
    public APIResponse<String> keyStatus(@RequestParam String username) {
        try {
            username = UserContext.getUsername();
            String keyStatus = userMapper.KeyStatus(username);
            if(!Objects.equals(keyStatus, null)){

                return APIResponse.success("公钥已保存");
            } else {
                return APIResponse.error(500, "公钥未保存");
            }

        } catch (Exception e) {
            return APIResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/update-publicKey")
    public APIResponse<String> keyStatus(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        try {
            byte[] sharedSecret = (byte[]) session.getAttribute("sharedSecret");

            String encryptedPublicKey = (String) requestBody.get("public_key");
            String decryptedPublicKey = dhService.decrypt(encryptedPublicKey, sharedSecret);
            String username =(String) requestBody.get("username");
            username = UserContext.getUsername();
            userMapper.UpdatePublicKey(username, decryptedPublicKey);
            return APIResponse.success("成功插入公钥");
        } catch (Exception e) {
            return APIResponse.error(500, "插入公钥失败: " + e.getMessage());
        }
    }
}
