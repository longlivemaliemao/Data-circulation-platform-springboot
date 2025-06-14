package com.example.demo.Model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

@TableName("FILES")
public class File {

    private String fileId;
    private String fileName;
    private String filePath;
    private String creatorName;
    private String fileOutline;
    private int totalChunks;
    private int uploadedChunks;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date usageTime;

    // Getters and Setters
    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileOutline() {
        return fileOutline;
    }

    public void setFileOutline(String fileOutline) {
        this.fileOutline = fileOutline;
    }

    public Date getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(Date usageTime) {
        this.usageTime = usageTime;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(int totalChunks) {
        this.totalChunks = totalChunks;
    }

    public int getUploadedChunks() {
        return uploadedChunks;
    }

    public void setUploadedChunks(int uploadedChunks) {
        this.uploadedChunks = uploadedChunks;
    }

    @Override
    public String toString() {
        return "File{" +
                "fileId='" + fileId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", creatorName='" + creatorName + '\'' +
                ", fileOutline='" + fileOutline + '\'' +
                ", usageTime=" + usageTime + '\'' +
                ", totalChunks='" + totalChunks + '\'' +
                ", uploadedChunks='" + uploadedChunks + '\'' +
                '}';
    }

    public static class SignTask {
    }
}
