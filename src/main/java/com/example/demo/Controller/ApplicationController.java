package com.example.demo.Controller;

import com.example.demo.Mapper.*;
import com.example.demo.Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private STUMapper stuMapper;  // 注入 STUMapper，用于签名任务用户数据操作
    @Autowired
    private CTUMapper ctuMapper;
    @Autowired
    private ATUMapper atuMapper;

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
            if (SecurityContextHolder.getContext().getAuthentication().getName().equals(application.getDataUser())) {
                return APIResponse.error(400,"不允许向自己申请数据");
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
            application.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
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
            username = SecurityContextHolder.getContext().getAuthentication().getName();
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
    @Transactional(rollbackFor = Exception.class)
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
            username = SecurityContextHolder.getContext().getAuthentication().getName();
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

    @PostMapping("/getProcessStatus")
    public APIResponse<List<ProcessStatusVO>> getProcessStatus(@RequestBody Map<String, Object> requestData) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            int applicationId = (int) requestData.get("id");
            String applicationType = (String) requestData.get("applicationType");
            String status = applicationMapper.findStatus(applicationId);

            // 状态不合法则不允许查看流程状态
            if (status.equals("等待平台审核") || status.equals("平台审核未通过") ||
                    status.equals("等待数据提供方审核") || status.equals("数据提供方审核未通过")) {
                return APIResponse.error(400, "当前申请状态不支持查看流程状态");
            }

            int taskId = taskMapper.findTaskIdByApplicationId(applicationId);
            List<String> userLists;
            List<ProcessStatusVO> processStatusList;

            if ("签名".equals(applicationType)) {
                processStatusList = stuMapper.findUsersByTaskId(taskId);
            } else if ("确权".equals(applicationType)) {
                processStatusList = ctuMapper.findUsersByTaskId(taskId);

            } else if ("仲裁".equals(applicationType)) {
                processStatusList = atuMapper.findUsersByTaskId(taskId);

            } else {
                return APIResponse.error(400, "不支持的申请类型：" + applicationType);
            }
            userLists = processStatusList.stream()
                    .map(ProcessStatusVO::getUsername)
                    .collect(Collectors.toList());

            if (!userLists.contains(username)) {
                return APIResponse.error(403, "当前用户无权限查看该流程状态");
            }

            return APIResponse.success(processStatusList);

        } catch (Exception e) {
            return APIResponse.error(500, "系统内部错误: " + e.getMessage());
        }
    }
}
