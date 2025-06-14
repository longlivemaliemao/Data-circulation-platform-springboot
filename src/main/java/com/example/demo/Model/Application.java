package com.example.demo.Model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

@TableName("APPLICATION")
public class Application {

    @TableId(type = IdType.INPUT)  // 手动管理主键，使用 Oracle 序列
    private int id;
    private String username;
    private String applicationType;
    private String status;
    private String dataUser;
    private String text;
    private String explanation;
    private String fileName;
    private Date startDate;
    private Date endDate;
    private Date applicationTime;

    // 构造函数
    public Application() {
    }

    public Application(int id, String username, String fileName, String applicationType, String explanation, String text, String dataUser, String status, Date startDate, Date endDate, Date applicationTime) {
        this.id = id;
        this.username = username;
        this.fileName = fileName;
        this.applicationType = applicationType;
        this.explanation = explanation;
        this.text = text;
        this.dataUser = dataUser;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.applicationTime = applicationTime;
    }

    // Getters and Setters

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDataUser() {
        return dataUser;
    }

    public void setDataUser(String dataUser) {
        this.dataUser = dataUser;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getApplicationTime() {
        return applicationTime;
    }

    public void setApplicationTime(Date applicationTime) {
        this.applicationTime = applicationTime;
    }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fileName='" + fileName + '\'' +
                ", applicationType='" + applicationType + '\'' +
                ", status='" + status + '\'' +
                ", dataUser='" + dataUser + '\'' +
                ", text='" + text + '\'' +
                ", explanation='" + explanation + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", applicationTime=" + applicationTime +
                '}';
    }
}
