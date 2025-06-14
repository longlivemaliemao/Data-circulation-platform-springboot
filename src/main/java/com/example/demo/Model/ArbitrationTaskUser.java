package com.example.demo.Model;

import com.baomidou.mybatisplus.annotation.TableName;

import java.sql.Timestamp;

@TableName("ARBITRATION_USER")
public class ArbitrationTaskUser {

    private Integer id;              // 自增 ID
    private Integer taskId;          // 任务 ID
    private Integer arbitrationNumber; // 确认编号
    private String d;                // 字段 d
    private String d1;                // 字段 d1
    private String t;                // 字段 t
    private String t1;               // 字段 t1
    private String t2;               // 字段 t2
    private String delta;
    private String ch;
    private String num;
    private String r;
    private String s;
    private String c;
    private String userName;         // 用户名
    private String status;           // 状态
    private String taskType;         // 任务类型
    private Timestamp completedAt;   // 完成时间

    // 无参构造函数
    public ArbitrationTaskUser() {
    }

    // 带参构造函数
    public ArbitrationTaskUser(Integer id, Integer taskId, String taskType, Integer arbitrationNumber,
                               String d, String d1, String delta,String t, String t1, String t2, String ch,
                               String userName, String status, Timestamp completedAt, String num, String r, String s, String c) {
        this.id = id;
        this.taskId = taskId;
        this.taskType = taskType;
        this.arbitrationNumber = arbitrationNumber;
        this.d = d;
        this.d1 = d1;
        this.t = t;
        this.t1 = t1;
        this.t2 = t2;
        this.delta = delta;
        this.ch = ch;
        this.userName = userName;
        this.status = status;
        this.completedAt = completedAt;
        this.num = num;
        this.r = r;
        this.s = s;
        this.c = c;
    }

    // Getter 和 Setter 方法
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

    public Integer getArbitrationNumber() {
        return arbitrationNumber;
    }

    public void setArbitrationNumber(Integer arbitrationNumber) {
        this.arbitrationNumber = arbitrationNumber;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getD1() {
        return d1;
    }

    public void setD1(String d1) {
        this.d1 = d1;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getT1() {
        return t1;
    }

    public void setT1(String t1) {
        this.t1 = t1;
    }

    public String getT2() {
        return t2;
    }

    public void setT2(String t2) {
        this.t2 = t2;
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

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public String getCh() {
        return ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    public String getDelta() {
        return delta;
    }

    public void setDelta(String delta) {
        this.delta = delta;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    @Override
    public String toString() {
        return "ArbitrationTaskUser{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", arbitrationNumber=" + arbitrationNumber +
                ", d='" + d + '\'' +
                ", d1='" + d1 + '\'' +
                ", t='" + t + '\'' +
                ", t1='" + t1 + '\'' +
                ", t2='" + t2 + '\'' +
                ", delta='" + delta + '\'' +
                ", ch='" + ch + '\'' +
                ", userName='" + userName + '\'' +
                ", status='" + status + '\'' +
                ", taskType='" + taskType + '\'' +
                ", completedAt=" + completedAt +
                ", num='" + num + '\'' +
                ", r='" + r + '\'' +
                ", s='" + s + '\'' +
                ", c='" + c + '\'' +
                '}';
    }
}
