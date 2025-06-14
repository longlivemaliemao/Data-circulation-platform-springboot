package com.example.demo.Model;

import java.util.Objects;

public class TaskRequest {
    private Integer taskId;        // 任务 ID
    private Integer applicationId; // 申请 ID
    private String username;       // 用户名
    private String y;              // 字段 y
    private String b;              // 字段 b
    private String d;              // 字段 d
    private String d1;             // 字段 d1
    private String t;              // 字段 t
    private String t1;             // 字段 t1
    private String t2;             // 字段 t2
    private String ch;
    private String delta;
    private String num;
    private String r;
    private String s;
    private String b1;

    // 默认构造函数
    public TaskRequest() {
    }

    // 带参数的构造函数
    public TaskRequest(Integer taskId, Integer applicationId, String username, String y, String b,
                       String d, String d1, String t, String t1, String t2, String ch, String delta,
                       String num, String r, String s, String b1) {
        this.taskId = taskId;
        this.applicationId = applicationId;
        this.username = username;
        this.y = y;
        this.b = b;
        this.d = d;
        this.d1 = d1;
        this.t = t;
        this.t1 = t1;
        this.t2 = t2;
        this.ch = ch;
        this.delta = delta;
        this.num = num;
        this.r = r;
        this.s = s;
        this.b1 = b1;
    }

    // Getters 和 Setters
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

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

    public String getB1() {
        return b1;
    }

    public void setB1(String b1) {
        this.b1 = b1;
    }


    @Override
    public String toString() {
        return "TaskRequest{" +
                "taskId=" + taskId +
                ", applicationId=" + applicationId +
                ", username='" + username + '\'' +
                ", y='" + y + '\'' +
                ", b='" + b + '\'' +
                ", d='" + d + '\'' +
                ", d1='" + d1 + '\'' +
                ", t='" + t + '\'' +
                ", t1='" + t1 + '\'' +
                ", t2='" + t2 + '\'' +
                ", ch='" + ch + '\'' +
                ", delta='" + delta + '\'' +
                ", num='" + num + '\'' +
                ", r='" + r + '\'' +
                ", s='" + s + '\'' +
                ", b1='" + b1 + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskRequest)) return false;
        TaskRequest that = (TaskRequest) o;
        return Objects.equals(taskId, that.taskId) &&
                Objects.equals(applicationId, that.applicationId) &&
                Objects.equals(username, that.username) &&
                Objects.equals(y, that.y) &&
                Objects.equals(b, that.b) &&
                Objects.equals(d, that.d) &&
                Objects.equals(d1, that.d1) &&
                Objects.equals(t, that.t) &&
                Objects.equals(t1, that.t1) &&
                Objects.equals(t2, that.t2) &&
                Objects.equals(ch, that.ch) &&
                Objects.equals(delta, that.delta) &&
                Objects.equals(num, that.num) &&
                Objects.equals(r, that.r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, applicationId, username, y, b, d, d1, t, t1, t2);
    }
}
