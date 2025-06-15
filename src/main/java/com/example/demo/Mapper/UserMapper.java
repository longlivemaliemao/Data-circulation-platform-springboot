package com.example.demo.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.Model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 返回 User 对象，如果没有找到则返回 null
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT id FROM users WHERE username = #{username}")
    int findIdByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findByID(int id);

    @Insert("INSERT INTO users (username, password, public_key, role, securityQuestion, securityAnswer) VALUES (#{username}, #{password}, #{public_key}, #{role}, #{security_question}, #{security_answer})")
    int insert(User user);

    // 更新用户信息
    @Update("UPDATE users SET username = #{username}, password = #{password}, role = #{role}, shared_secret = #{shared_secret} WHERE id = #{id}")
    void update(User user);

    // 更新用户名
    @Update("UPDATE users SET username = #{username} WHERE id = #{id}")
    void update1(String username, int id);

    // 更新用户密保问题和密保答案
    @Update("UPDATE users SET securityQuestion = #{security_question}, securityAnswer = #{security_answer} WHERE id = #{id}")
    void update2(String security_question, String security_answer, int id);

    // 更新用户密码
    @Update("UPDATE users SET password = #{password} WHERE id = #{id}")
    void update3(String password, int id);

    // 更新用户密码
    @Update("UPDATE users SET password = #{password} WHERE username = #{username}")
    void update4(String password, String username);

    @Delete("DELETE FROM users WHERE username = #{username}")
    void deleteByUsername(String username);

    @Select("SELECT id, username, role FROM users")
    List<User> findAllUsers();

    @Select("SELECT username FROM users")
    List<String> findAllUsername();

    @Select("SELECT username FROM users WHERE role = '数据提供方'")
    List<User> findAllDataOwners();

    @Update("UPDATE users SET public_key = #{public_key} WHERE username = #{username}")
    void UpdatePublicKey(String username, String public_key);

    @Select("SELECT public_key FROM users WHERE username = #{username}")
    String KeyStatus(String username);

    @Select("SELECT securityquestion FROM users WHERE username = #{username}")
    String findSecurityQuestion(String username);

    @Select("SELECT securityanswer FROM users WHERE username = #{username}")
    String findSecurityAnswer(String username);

    /**
     * 根据用户名进行模糊查询
     * @param username 用户名的搜索关键字
     * @return 匹配的用户名和公钥列表
     */
    @Select("SELECT username, public_key , role FROM users WHERE username LIKE '%' || #{username} || '%'")
    List<Map<String, String>> findUsersByUsername2(@Param("username") String username);


}
