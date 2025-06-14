package com.example.demo.Model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

@TableName("TASK")
public class Task {

    @TableId(value = "task_id", type = IdType.AUTO)
    private Integer taskId; // 任务ID
    private String taskType; // 任务类型
    private String fileName; // 文件ID
    private String confirmId; // 确认ID
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // JSON序列化格式
    private java.sql.Timestamp createdAt; // 创建时间
    private String status; // 任务状态
    private String y;
    private String b;
    private String x;
    private String e1;
    private String e2;
    private String f1;
    private String f2;
    private String username;
    private Integer applicationId;
    private Integer signApplicationId;
    private String usagePolicy;

    // 无参构造函数
    public Task() {
    }

    // 带参构造函数（包含所有字段）
    public Task(Integer taskId, String taskType, String fileName, String confirmId,
                java.sql.Timestamp createdAt, String status, String y,
                String b, String x, String e1, String e2, String f1, String f2, String username,
                Integer applicationId, Integer signApplicationId, String usagePolicy) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.fileName = fileName;
        this.confirmId = confirmId;
        this.createdAt = createdAt;
        this.status = status;
        this.y = y;
        this.b = b;
        this.x = x;
        this.e1 = e1;
        this.e2 = e2;
        this.f1 = f1;
        this.f2 = f2;
        this.username = username;
        this.applicationId = applicationId;
        this.signApplicationId = signApplicationId;
        this.usagePolicy = usagePolicy;

    }

    // Getter 和 Setter 方法


    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConfirmId() {
        return confirmId;
    }

    public void setConfirmId(String confirmId) {
        this.confirmId = confirmId;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getE1() {
        return e1;
    }

    public void setE1(String e1) {
        this.e1 = e1;
    }

    public String getE2() {
        return e2;
    }

    public void setE2(String e2) {
        this.e2 = e2;
    }

    public String getF1() {
        return f1;
    }

    public void setF1(String f1) {

        this.f1 = f1;
    }

    public String getF2() {
        return f2;
    }

    public void setF2(String f2) {

        this.f2 = f2;
    }

    public Integer getSignApplicationId() {
        return signApplicationId;
    }

    public void setSignApplicationId(Integer signApplicationId) {
        this.signApplicationId = signApplicationId;
    }

    public String getUsagePolicy() {
        return usagePolicy;
    }

    public void setUsagePolicy(String usagePolicy) {
        this.usagePolicy = usagePolicy;
    }

    // toString 方法
    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", taskType='" + taskType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", confirmId='" + confirmId + '\'' +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                ", y='" + y + '\'' +
                ", b='" + b + '\'' +
                ", x='" + x + '\'' +
                ", e1='" + e1 + '\'' +
                ", e2='" + e2 + '\'' +
                ", f1='" + f1 + '\'' +
                ", f2='" + f2 + '\'' +
                ", username='" + username + '\'' +
                ", applicationId=" + applicationId +
                ", signApplicationId=" + signApplicationId +
                ", usagePolicy='" + usagePolicy + '\'' +
                '}';
    }
}
