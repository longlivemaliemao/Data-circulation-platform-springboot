package com.example.demo.Controller;

import com.example.demo.Mapper.ApplicationMapper;
import com.example.demo.Mapper.STUMapper;
import com.example.demo.Mapper.TaskMapper;
import com.example.demo.Model.Application;
import com.example.demo.Model.APIResponse;
import com.example.demo.Model.SignTaskUser;
import com.example.demo.Model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private STUMapper stuMapper;  // 注入 STUMapper，用于签名任务用户数据操作

    /**
     * 插入新的申请记录到数据库
     * @param application 前端传入的 Application 对象
     * @return 返回操作结果
     */
    @PostMapping("/add")
    public APIResponse<String> addApplication(@RequestBody Application application) {
        try {

            if (!Objects.equals(application.getApplicationType(), "签名")) {
                List<Integer> TaskIDs = taskMapper.findCompletedDataTaskIDs();
                if(!TaskIDs.contains(Integer.parseInt(application.getText()))){
                    return APIResponse.error(400,"未找到该数据流转任务ID");
                }
            }
            if(Objects.equals(application.getApplicationType(), "仲裁")){
                boolean ok = false;
                String taskId = application.getText();
                List<SignTaskUser> signTaskUsers = stuMapper.findTaskByTaskId(Integer.parseInt(taskId));
                for (SignTaskUser signTaskUser : signTaskUsers) {
                    if(signTaskUser.getUserName().equals(application.getUsername()))
                    {
                        ok = true;
                        break;
                    }
                }
                if(!ok){
                    return APIResponse.error(400, "没有权限对该任务申请仲裁");
                }
            }

            // 设置 applicationTime 为当前系统时间
            application.setApplicationTime(new Date());
            application.setFileName("");
            if(!Objects.equals(application.getApplicationType(), "签名")){
                String taskId = application.getText();
                Task task = taskMapper.findTaskById(Integer.parseInt(taskId));
                application.setFileName(task.getFileName());
            }
            // 设置 startDate 和 endDate 的默认值（假设默认值为当前日期）
            if (application.getStartDate() == null) {
                application.setStartDate(new Date()); // 可以根据需要设为其他日期
            }
            if (application.getEndDate() == null) {
                application.setEndDate(new Date()); // 可以根据需要设为其他日期
            }

            // 将数据插入数据库
            int result = applicationMapper.insert(application);
            if (result > 0) {
                return APIResponse.success("申请记录添加成功");
            } else {
                return APIResponse.error(500, "申请记录添加失败");
            }
        } catch (Exception e) {
            return APIResponse.error(500, "发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取指定用户名的所有申请记录
     * @param username 用户名
     * @return 返回该用户的所有申请记录
     */
    @GetMapping("/user/{username}")
    public APIResponse<List<Application>> getApplicationsByUsername(@PathVariable String username) {
        try {
            List<Application> applications = applicationMapper.findApplicationsByUsername(username);
            applications.sort((a1, a2) -> a2.getApplicationTime().compareTo(a1.getApplicationTime()));
            return APIResponse.success(applications);
        } catch (Exception e) {
            return APIResponse.error(500, "获取申请记录时发生错误: " + e.getMessage());
        }
    }

    /**
     * 审批员和数据提供方同意或拒绝申请
     * @param requestData 包含用户名、状态和解释的 Map 对象
     * @return 操作结果
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('审批员') OR hasAuthority('数据提供方')")
    public APIResponse<String> updateApplication(@RequestBody Map<String, String> requestData) {
        try {

            String id = requestData.get("id");
            String status = requestData.get("status");
            String explanation = requestData.get("explanation");
            String fileName = requestData.get("fileName");

            if(!Objects.equals(fileName, "")){
                applicationMapper.updateFileName(id, fileName);
            }

             //更新申请
            applicationMapper.updateApplication(id, status, explanation);

            return APIResponse.success("申请已更新");
        } catch (Exception e) {
            return APIResponse.error(500, "更新申请时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取所有状态为 "等待平台审核" 的申请记录。
     * @return 返回包含所有 "等待平台审核" 状态申请记录的 APIResponse 列表。
     */
    @GetMapping("/pending2")
    @PreAuthorize("hasAuthority('审批员')")
    public APIResponse<List<Application>> getPending2Applications() {
        try {
            List<Application> pendingApplications = applicationMapper.findApplicationsWaiting2();
            if (pendingApplications != null && !pendingApplications.isEmpty()) {
                return APIResponse.success(pendingApplications);
            } else {
                return APIResponse.error(404, "未找到等待平台审核的申请记录");
            }
        } catch (Exception e) {
            return APIResponse.error(500, "获取等待平台审核的申请记录时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取所有状态为 "等待数据提供方审核" 的申请记录。
     * @return 返回包含所有 "等待数据提供方审核" 状态申请记录的 APIResponse 列表。
     */
    @GetMapping("/pending1")
    @PreAuthorize("hasAuthority('数据提供方')")
    public APIResponse<List<Application>> getPending1Applications(@RequestParam("username") String username) {
        try {
            List<Application> pendingApplications = applicationMapper.findApplicationsWaiting1(username);
            if (pendingApplications != null && !pendingApplications.isEmpty()) {
                return APIResponse.success(pendingApplications);
            } else {
                return APIResponse.error(404, "未找到等待数据提供方审核的申请记录");
            }
        } catch (Exception e) {
            return APIResponse.error(500, "获取等待数据提供方审核的申请记录时发生错误: " + e.getMessage());
        }
    }


}
