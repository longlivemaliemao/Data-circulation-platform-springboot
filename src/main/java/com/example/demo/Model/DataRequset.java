package com.example.demo.Model;

import java.sql.Timestamp;

public class DataRequset {
    private Integer taskId;  // 任务ID
    private String dataId;  // 数据ID
    private String fileName;
    private Timestamp time;    // 时间
    private String b;       // B字段
    private String y;       // Y字段
    private String creator; // 创建者
    private String outline; // 大纲
    private String usagePolicy;

    // 默认构造方法
    public DataRequset() {}

    // 带参数的构造方法
    public DataRequset(Integer taskId, String dataId, String fileName,Timestamp time, String b, String y, String creator, String outline, String usagePolicy) {
        this.taskId = taskId;
        this.dataId = dataId;
        this.fileName = fileName;
        this.time = time;
        this.b = b;
        this.y = y;
        this.creator = creator;
        this.outline = outline;
        this.usagePolicy = usagePolicy;
    }

    // Getter 和 Setter 方法
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getOutline() {
        return outline;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUsagePolicy() {
        return usagePolicy;
    }

    public void setUsagePolicy(String usagePolicy) {
        this.usagePolicy = usagePolicy;
    }

    // toString() 方法（可选）
    @Override
    public String toString() {
        return "DataRequset{" +
                "taskId='" + taskId + '\'' +
                ", dataId='" + dataId + '\'' +
                ", time='" + time + '\'' +
                ", b='" + b + '\'' +
                ", y='" + y + '\'' +
                ", creator='" + creator + '\'' +
                ", outline='" + outline + '\'' +
                ", usagePolicy='" + usagePolicy + '\'' +
                '}';
    }
}
