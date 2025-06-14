package com.example.demo.Model;

public class Handle {
    private Integer taskId;
    private String fileName;
    private String taskType;
    private String status;
    private String d;
    private String d1;
    private String b;
    private String y;
    private String ch;
    private String r;
    private String num;

    private java.sql.Timestamp completedAt;

    // 默认构造函数
    public Handle() {
    }

    // 带参数的构造函数
    public Handle(Integer taskId, String fileName, String taskType, String status,
                  String d, String d1, String b, String y, String ch, String r, String num,
                  java.sql.Timestamp completedAt) {
        this.taskId = taskId;
        this.fileName = fileName;
        this.taskType = taskType;
        this.status = status;
        this.d = d;
        this.d1 = d1;
        this.b = b;
        this.y = y;
        this.ch = ch;
        this.r = r;
        this.num = num;
        this.completedAt = completedAt;
    }

    // Getter 和 Setter 方法
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public java.sql.Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(java.sql.Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public String getCh() {
        return ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    // toString 方法
    @Override
    public String toString() {
        return "Handle{" +
                "taskId=" + taskId +
                ", fileName='" + fileName + '\'' +
                ", taskType='" + taskType + '\'' +
                ", status='" + status + '\'' +
                ", d='" + d + '\'' +
                ", d1='" + d1 + '\'' +
                ", b='" + b + '\'' +
                ", y='" + y + '\'' +
                ", ch='" + ch + '\'' +
                ", r='" + r + '\'' +
                ", num='" + num + '\'' +
                ", completedAt=" + completedAt +
                '}';
    }
}
