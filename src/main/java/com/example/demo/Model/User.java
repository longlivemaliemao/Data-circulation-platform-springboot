package com.example.demo.Model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * User 类表示数据库中的 user 表。
 * 使用 MyBatis-Plus 注解进行 ORM 映射。
 */
@TableName("USERS")
public class User {

    @TableId(type = IdType.INPUT) // 改为手动控制主键
    private int id;
    private String username;
    private String password;
    private String public_key;
    private String role;
    private String shared_secret;
    private String security_question;
    private String security_answer;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShared_secret() {
        return shared_secret;
    }

    public void setShared_secret(String shared_secret) {
        this.shared_secret = shared_secret;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getPublic_key() {
        return public_key;
    }

    public void setPublic_key(String public_key) {
        this.public_key = public_key;
    }

    public String getSecurity_question() {
        return security_question;
    }

    public void setSecurity_question(String security_question) {
        this.security_question = security_question;
    }

    public String getSecurity_answer() {
        return security_answer;
    }

    public void setSecurity_answer(String security_answer) {
        this.security_answer = security_answer;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' + // 如果密码不需要打印，可以将其移除
                ", public_key='" + public_key + '\'' +
                ", role='" + role + '\'' +
                ", shared_secret='" + shared_secret + '\'' +
                ", security_question='" + security_question + '\'' +
                ", security_answer='" + security_answer + '\'' +
                '}';
    }
}
