package com.example.demo.Model;

import java.util.List;

public class CreateTaskRequest {
    private Signer signer;
    private String selectFile;
    private String taskType;
    private String confirmId;
    private String username;
    private Integer applicationId;
    private String usagePolicy;

// Getter 和 Setter


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
    public Signer getSigner() {
        return signer;
    }

    public void setSigner(Signer signer) {
        this.signer = signer;
    }

    public String getSelectFile() {
        return selectFile;
    }

    public void setSelectFile(String selectFile) {
        this.selectFile = selectFile;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getUsagePolicy() {
        return usagePolicy;
    }

    public void setUsagePolicy(String usagePolicy) {
        this.usagePolicy = usagePolicy;
    }

    public static class Signer {
        private List<SignerMember> members;

        public List<SignerMember> getMembers() {
            return members;
        }

        public void setMembers(List<SignerMember> members) {
            this.members = members;
        }
    }

    public static class SignerMember {
        private String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
