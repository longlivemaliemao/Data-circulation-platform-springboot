package com.example.demo.Model;

import com.baomidou.mybatisplus.annotation.TableName;

import java.sql.Timestamp;

@TableName("CONFIRM_USER")
public class ConfirmTaskUser {

    private Integer id;              // 自增 ID
    private Integer taskId;          // 任务 ID
    private Integer confirmNumber;    // 确认编号
    private String d;                // 字段 d
    private String userName;         // 用户名
    private String status;           // 状态
    private String taskType;
    private java.sql.Timestamp completedAt;        // 完成时间

    // 无参构造函数
    public ConfirmTaskUser() {
    }

    // 带参构造函数
    public ConfirmTaskUser(Integer id, Integer taskId, String taskType, Integer confirmNumber,
                           String d, String userName, String status, java.sql.Timestamp completedAt) {
        this.id = id;
        this.taskId = taskId;
        this.taskType = taskType;
        this.confirmNumber = confirmNumber;
        this.d = d;
        this.userName = userName;
        this.status = status;
        this.completedAt = completedAt;
    }

    // Getter 和 Setter 方法


    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getConfirmNumber() {
        return confirmNumber;
    }

    public void setConfirmNumber(Integer confirmNumber) {
        this.confirmNumber = confirmNumber;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(java.sql.Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "ConfirmTaskUser{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", confirmNumber=" + confirmNumber +
                ", d='" + d + '\'' +
                ", userName='" + userName + '\'' +
                ", status='" + status + '\'' +
                ", taskType='" + taskType + '\'' +
                ", completedAt=" + completedAt +
                '}';
    }
}
