package com.example.demo.Controller;

import com.example.demo.Mapper.*;
import com.example.demo.Model.*;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskMapper taskMapper;  // 注入 TaskMapper，用于任务数据操作

    @Autowired
    private STUMapper stuMapper;  // 注入 STUMapper，用于签名任务用户数据操作

    @Autowired
    private CTUMapper ctuMapper;  // 注入 CTUMapper，用于确权任务用户数据操作

    @Autowired
    private ATUMapper atuMapper;  // 注入 ATUMapper，用于确权任务用户数据操作

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private UserMapper userMapper;

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);  // 日志记录器

    // 大整数 p 和 g 用于后续的加密计算
    BigInteger p = new BigInteger("132165373947571709001890899559578394061572732290158236845675979056783176833192189640519330577968623712019753279011546461561086378291703395170828826203868040544703192493236905634659492348075654172349595065574318562378095706622284475060330389667603958501055142626804746804365447731489915179943331725842802927799");
    BigInteger g = new BigInteger("436921");

    @GetMapping("/find_task")
    public APIResponse<String> findTask(@RequestParam("taskId") int taskId) {
        if(taskMapper.findTaskById(taskId) != null)
            return APIResponse.success("找到对应流转任务");
        else
            return APIResponse.error(500, "没有找到对应流转任务");
    }

    @PostMapping("/testTime")
    public APIResponse<String> testTime(
            @RequestParam("username") String username,
            @RequestParam("taskId") String taskId,
            @RequestParam("executionTime") String executionTime
            ) {
        try {

            // 1. 转换执行时间为数值
            double execTime;
            execTime = Double.parseDouble(executionTime);

            // 2. 记录不同级别的日志
            logger.info("接收到性能数据 - 用户: {}, 任务ID: {}, 耗时: {}ms", username, taskId, execTime);

            // 耗时过长警告
            if (execTime > 1000) {
                logger.warn("耗时过长警告 - 用户: {}, 任务ID: {}, 耗时: {}ms", username, taskId, execTime);
            }

            // 3. 返回成功响应
            return APIResponse.success("性能数据记录成功");

        } catch (Exception e) {
            logger.error("记录性能数据异常 - 用户: {}, 任务ID: {}", username, taskId, e);
            return APIResponse.error(500,"服务器内部错误");
        }

    }

    /**
     * 处理前端的 POST 请求，创建新任务并返回操作结果
     *
     * @param createTaskRequest 包含任务信息的请求数据传输对象
     * @return 返回包含操作结果的 APIResponse 对象
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('数据提供方') OR hasAuthority('审批员')")
    public APIResponse<String> createTask(@RequestBody CreateTaskRequest createTaskRequest) {
        try {
            // 从 CreateTaskRequestDTO 中获取数据并创建 Task 对象
            Task task = new Task();
            task.setTaskType(createTaskRequest.getTaskType()); // 设置任务类型
            task.setStatus("in_progress");  // 任务初始状态
            task.setCreatedAt(new Timestamp(System.currentTimeMillis()));  // 当前时间作为创建时间
            task.setUsername(createTaskRequest.getUsername());
            task.setApplicationId(createTaskRequest.getApplicationId());
            task.setUsagePolicy("");
            // 创建 SecureRandom 实例
            SecureRandom random = new SecureRandom();
            // 生成 1024 位随机数
            BigInteger e1 = new BigInteger(1024, random);
            BigInteger e2 = new BigInteger(1024, random);
            BigInteger f1 = "仲裁".equals(task.getTaskType()) ? new BigInteger(1024, random) : new BigInteger("0");
            BigInteger f2 = "仲裁".equals(task.getTaskType()) ? new BigInteger(1024, random) : new BigInteger("0");

            if (Objects.equals(task.getTaskType(), "签名")) {
                task.setFileName(createTaskRequest.getSelectFile()); // 设置文件 ID
                task.setConfirmId("");
                task.setB("");
                task.setY("");
                task.setX("");
                task.setE1("");
                task.setE2("");
                task.setF1("");
                task.setF2("");
                task.setUsagePolicy(createTaskRequest.getUsagePolicy());
                task.setSignApplicationId(-1);
                applicationMapper.updateAuthEndTime(createTaskRequest.getApplicationId(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(createTaskRequest.getAuthEndTime()));
            } else  {
                task.setConfirmId(createTaskRequest.getConfirmId());
                Task task1 = taskMapper.findTaskById(Integer.parseInt(task.getConfirmId()));
                task.setFileName(task1.getFileName());
                task.setB(task1.getB());
                task.setY(task1.getY());
                task.setX(task1.getX());
                task.setSignApplicationId(task1.getApplicationId());
                task.setE1(String.valueOf(e1));
                task.setE2(String.valueOf(e2));
                task.setF1(String.valueOf(f1));
                task.setF2(String.valueOf(f2));
            }

//            System.out.println(task);

            // 将任务插入数据库，并获取返回结果
            int result = taskMapper.insert(task);

            int taskId = task.getTaskId(); // 获取新任务的 ID

            if (result > 0) {
                // 根据任务类型创建相应的用户
                if (Objects.equals(task.getTaskType(), "签名")) {

                    createSignUser(createTaskRequest, taskId); // 创建签名用户
                } else if (Objects.equals(task.getTaskType(), "确权")) {
                    createConfirmUser(Integer.parseInt(createTaskRequest.getConfirmId()), taskId, e1, e2);
                } else {
                    createArbitrationUser(Integer.parseInt(createTaskRequest.getConfirmId()), taskId, e1, e2);
                }

                return APIResponse.success("任务创建成功"); // 返回成功响应
            } else {
                return APIResponse.error(500, "任务创建失败"); // 返回失败响应
            }
        } catch (Exception e) {
            return APIResponse.error(500, "发生错误: " + e.getMessage()); // 捕获异常并返回错误信息
        }
    }

    /**
     * 处理前端的 GET 请求，根据用户名查找所有任务
     *
     * @param userName 要查找的用户名
     * @return 返回包含任务的 APIResponse 对象
     */
    @GetMapping("/getTask")
    public APIResponse<List<Handle>> getTasksByUserName(@RequestParam String userName) {
        try {
            userName = SecurityContextHolder.getContext().getAuthentication().getName();
            List<SignTaskUser> signTaskUsers = stuMapper.findTasksByUserName(userName); // 查找任务
            List<ConfirmTaskUser> confirmTaskUsers = ctuMapper.findTasksByUserName(userName);
            List<ArbitrationTaskUser> arbitrationTaskUsers = atuMapper.findInProgressTasksByUserName(userName);

            List<Handle> handles = new ArrayList<>(); // 创建一个空的 Handle 列表

            // 将 signTaskUsers 转换为 Handle
            for (SignTaskUser signTaskUser : signTaskUsers) {
                Handle handle = new Handle();
                handle.setTaskId(signTaskUser.getTaskId());
                handle.setFileName(signTaskUser.getFileName());
                handle.setTaskType(signTaskUser.getTaskType());
                handle.setStatus(signTaskUser.getStatus());
                handle.setCompletedAt(signTaskUser.getCompletedAt());
                handle.setB(signTaskUser.getB());
                handle.setY(signTaskUser.getY());
                handles.add(handle);
            }

            // 将 confirmTaskUsers 转换为 Handle
            for (ConfirmTaskUser confirmTaskUser : confirmTaskUsers) {
                Handle handle = new Handle();
                handle.setTaskId(confirmTaskUser.getTaskId());
                Task task = taskMapper.findTaskById(confirmTaskUser.getTaskId());

                handle.setFileName(task.getFileName());
                handle.setTaskType(confirmTaskUser.getTaskType());
                handle.setStatus(confirmTaskUser.getStatus());
                handle.setCompletedAt(confirmTaskUser.getCompletedAt());
                handle.setD(confirmTaskUser.getD());
                handles.add(handle);

            }

            // 将 arbitrationTaskUsers 转换为 Handle
            for (ArbitrationTaskUser arbitrationTaskUser : arbitrationTaskUsers) {
                Handle handle = new Handle();
                handle.setTaskId(arbitrationTaskUser.getTaskId());
                Task task = taskMapper.findTaskById(arbitrationTaskUser.getTaskId());
                System.out.println(task.getFileName());
                handle.setFileName(task.getFileName());
                handle.setTaskType(arbitrationTaskUser.getTaskType());
                handle.setStatus(arbitrationTaskUser.getStatus());
                handle.setCompletedAt(arbitrationTaskUser.getCompletedAt());
                handle.setD(arbitrationTaskUser.getD());
                handle.setD1(arbitrationTaskUser.getD1());
                handle.setCh(arbitrationTaskUser.getCh());
                handle.setR(arbitrationTaskUser.getR());
                handle.setNum(arbitrationTaskUser.getNum());
                handles.add(handle);
//                System.out.println(handle);
            }
            handles = handles.stream()
                    .filter(app -> "in_progress".equalsIgnoreCase(app.getStatus()) || "私钥无效，请重新提交".equalsIgnoreCase(app.getStatus()))
                    .collect(Collectors.toList());
            handles.sort((a1, a2) -> a2.getCompletedAt().compareTo(a1.getCompletedAt()));
            return APIResponse.success(handles); // 返回成功响应
        } catch (Exception e) {
            return APIResponse.error(500, "发生错误: " + e.getMessage()); // 捕获异常并返回错误信息
        }
    }

    /**
     * 处理前端的 POST 请求，更新签名任务状态并返回操作结果
     *
     * @param request 包含更新信息的请求数据传输对象
     * @return 返回包含操作结果的 APIResponse 对象
     */
    @PostMapping("/signUpdate")
    public APIResponse<String> signUpdateTask(@RequestBody TaskRequest request) {
        try {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName(); // 获取用户名
            String y = request.getY(); // 获取 y 值
            String b = request.getB(); // 获取 b 值
            String B1 = request.getB1(); // 待验证公钥
            String B = userMapper.KeyStatus(userName); // 正确的公钥
            System.out.println(B1);
            System.out.println(B);
            int taskId = request.getTaskId(); // 获取任务 ID
            Task task = taskMapper.findTaskById(taskId);
            String applicationId = String.valueOf(task.getApplicationId());
            if(!B1.equals(B)){
                if(stuMapper.findSigner(taskId, userName).equals("私钥无效，请重新提交")){
                    applicationMapper.updateApplication(applicationId, "签名失败", userName+"使用非法私钥签名");
                    List<SignTaskUser> STUser = stuMapper.findTaskByTaskId(taskId);
                    Optional<SignTaskUser> targetUserOptional = STUser.stream()
                            .filter(user -> user.getUserName().equals(userName))
                            .findFirst();
                    if (targetUserOptional.isPresent()) {
                        stuMapper.updateStatus(taskId, userName, "error");
                        for (SignTaskUser user : STUser) {
                            if ("pending".equalsIgnoreCase(user.getStatus())) {
                                stuMapper.updateStatus(taskId, user.getUserName(), "stop");
                            }
                        }
                    }
                    taskMapper.updateTaskFields(taskId, "fail", y, b);
                    return APIResponse.error(400, "第二次使用非法私钥计算");
                }
                stuMapper.updateStatus(taskId, userName, "私钥无效，请重新提交");
                return APIResponse.error(400, "第一次使用非法私钥计算");
            }

            // 更新当前用户的状态
            stuMapper.updateStatusYB(taskId, userName, "completed", y, b);

            // 查找当前用户的 signerNumber
            int currentSignerNumber = stuMapper.findSignerNumber(taskId, userName);

            SignTaskUser nextUser = stuMapper.findNextSigner(taskId, currentSignerNumber + 1);

            if (nextUser != null) {
                stuMapper.updateStatusYB(taskId, nextUser.getUserName(), "in_progress", y, b);
            } else {
                // 如果没有下一个用户，标记任务为完成

                applicationMapper.updateApplication(applicationId, "签名已完成", "已允许下载该数据");
                taskMapper.updateTaskFields(taskId, "completed", y, b);
            }

            return APIResponse.success("Task updated successfully"); // 返回成功响应

        } catch (NumberFormatException e) {
            // 处理数字格式转换失败
            System.err.println("Error parsing number: " + e.getMessage());
            return APIResponse.error(500, "Invalid number format for y or b");

        } catch (Exception e) {
            // 捕获其他未知错误
            System.err.println("An unexpected error occurred: " + e.getMessage());
            return APIResponse.error(500, "An error occurred while updating the task");
        }
    }
    /**
     * 处理前端的 POST 请求，更新确权任务状态并返回操作结果
     *
     * @param request 包含更新信息的请求数据传输对象
     * @return 返回包含操作结果的 APIResponse 对象
     */
    @PostMapping("/confirmUpdate")
    public APIResponse<String> confirmUpdateTask(@RequestBody TaskRequest request) {
        try{
            String d = request.getD(); // 获取 d 值
            int taskId = request.getTaskId(); // 获取任务 ID
            String userName = SecurityContextHolder.getContext().getAuthentication().getName(); // 获取用户名

            String B1 = request.getB1(); // 待验证公钥
            String B = userMapper.KeyStatus(userName); // 正确的公钥

            Task task = taskMapper.findTaskById(taskId);
            String applicationId = String.valueOf(task.getApplicationId());
            if(!B1.equals(B)){
                if(ctuMapper.findConfirm(taskId, userName).equals("私钥无效，请重新提交")){
                    applicationMapper.updateApplication(applicationId, "确权失败", userName+"使用非法私钥确权");
                    List<ConfirmTaskUser> CTUser = ctuMapper.findTaskByTaskId(taskId);
                    Optional<ConfirmTaskUser> targetUserOptional = CTUser.stream()
                            .filter(user -> user.getUserName().equals(userName))
                            .findFirst();
                    if (targetUserOptional.isPresent()) {
                        ctuMapper.updateStatus(taskId, userName, "error");
                        for (ConfirmTaskUser user : CTUser) {
                            if ("pending".equalsIgnoreCase(user.getStatus())) {
                                ctuMapper.updateStatus(taskId, user.getUserName(), "stop");
                            }
                        }
                    }
                    taskMapper.updateTaskFields(taskId, "fail", task.getY(), task.getB());
                    return APIResponse.error(400, "第二次使用非法私钥计算");
                }
                ctuMapper.updateStatus(taskId, userName, "私钥无效，请重新提交");
                return APIResponse.error(400, "第一次使用非法私钥计算");
            }

            // 更新当前用户的状态
            String t = ctuMapper.findTask(taskId, userName);
            ctuMapper.updateStatusD(taskId, userName, "completed", t);

            // 查找当前用户的 ConfirmNumber
            int currentConfirmNumber = ctuMapper.findConfirmNumber(taskId, userName);

            ConfirmTaskUser nextUser = ctuMapper.findNextConfirm(taskId, currentConfirmNumber + 1);

            if (nextUser != null) {
                ctuMapper.updateStatusD(taskId, nextUser.getUserName(), "in_progress", d);
                return APIResponse.success("Task updated successfully"); // 返回成功响应
            } else {
                // 如果没有下一个用户，标记任务为完成
                BigInteger x = new BigInteger(task.getX());
                BigInteger e1 = new BigInteger(task.getE1());
                BigInteger e2 = new BigInteger(task.getE2());
                BigInteger D = new BigInteger(d);
                BigInteger k = x.modPow(e1, p).multiply(g.modPow(e2, p)).mod(p);
                Integer id = task.getApplicationId();

                if(k.equals(D)){
                    applicationMapper.updateApplication(String.valueOf(id), "确权验证成功" ,"");
                    taskMapper.updateTaskFields(taskId, "completed", task.getY(), task.getB());
                    return APIResponse.success("Task updated successfully"); // 返回成功响应
                }

                applicationMapper.updateApplication(String.valueOf(id), "确权验证失败" ,"");
                applicationMapper.updateApplication(String.valueOf(task.getSignApplicationId()), "该签名任务确权验证失败" ,"不允许下载该数据");
                taskMapper.updateTaskFields(taskId, "fail", task.getY(), task.getB());
                return APIResponse.success("确权验证失败");
            }
        } catch (NumberFormatException e){
            System.err.println("Error parsing number: " + e.getMessage());
            return APIResponse.error(500, "Invalid number format for y or b");
        } catch (Exception e){
            System.err.println("An unexpected error occurred: " + e.getMessage());
            return APIResponse.error(500, "An error occurred while updating the task");
        }
    }

    /**
     * 处理前端的 POST 请求，更新仲裁任务状态并返回操作结果
     *
     * @param request 包含更新信息的请求数据传输对象
     * @return 返回包含操作结果的 APIResponse 对象
     */
    @PostMapping("/arbitrationUpdate")
    public APIResponse<String> arbitrationUpdateTask(@RequestBody TaskRequest request) {
        try {
            // 从请求中获取相关参数
            String num = request.getNum();
            int taskId = request.getTaskId(); // 获取任务 ID
            Task task = taskMapper.findTaskById(taskId); // 查找当前任务
            Integer id = task.getApplicationId(); // 获取申请 ID
            // 获取任务相关的参数
            BigInteger x = new BigInteger(task.getX());
            String userName = request.getUsername();
            userName = SecurityContextHolder.getContext().getAuthentication().getName();

            // 查找当前用户的仲裁编号
            int currentArbitrationNumber = atuMapper.findArbitrationNumber(taskId, userName);

            // 查找下一个需要处理的用户
            ArbitrationTaskUser nextUser = atuMapper.findNextArbitration(taskId, currentArbitrationNumber + 1);

            if(num.equals("1")) {
                //第一轮验证
                String d = request.getD();     // 获取参数 d
                String r = request.getR();
                String delta = request.getDelta();
                String t = request.getT();      // 获取参数 t
                String t1 = request.getT1();    // 获取参数 t1
                String t2 = request.getT2();    // 获取参数 t2
                if (nextUser != null){
                    // 本轮还没结束
                    atuMapper.updateStatus1(taskId, userName, "pending", d, t, t1, t2,r, delta); // 更新当前用户状态为“待处理”
                    atuMapper.updateStatus1(taskId, nextUser.getUserName(), "in_progress", d, "0", "0", "0", "0", "0"); // 更新下一个用户状态为“进行中”
                    return APIResponse.success("参数更新成功"); // 返回成功响应
                } else {
                    // 本轮已结束，进行验证

                    atuMapper.updateStatus1(taskId, userName, "pending", d, t, t1, t2,r, delta); // 更新当前用户状态为“待处理”

                    BigInteger e1 = new BigInteger(task.getE1());
                    BigInteger e2 = new BigInteger(task.getE2());
                    BigInteger k = x.modPow(e1, p).multiply(g.modPow(e2, p)).mod(p); // 计算 k 的值
                    if (String.valueOf(k).equals(d)){
                        //验证成功
                        System.out.println("第一轮验证成功");
                        // 更新申请状态为“仲裁验证无误”
                        applicationMapper.updateApplication(String.valueOf(id), "仲裁验证完成", "签名值没有问题");
                        // 更新任务状态为“已完成”
                        taskMapper.updateTaskFields(taskId, "completed", "", "");
                        // 更新每个用户的状态为完成验证
                        atuMapper.updateStatus3(taskId, "completed", "1");

                        return APIResponse.success("第一轮验证成功"); // 返回成功响应
                    } else {
                        // 验证失败
                        System.out.println("第一轮验证失败");
//                        System.out.println(k);
//                        System.out.println(d);
                        // 更新每个用户的到第二轮验证
                        atuMapper.updateStatus3(taskId, "pending", "2");
                        applicationMapper.updateApplication(String.valueOf(id), "第一次验证失败，进行下一步验证", "在处理界面添加私钥计算");

                        //计算第二轮的c给第一个用户
                        BigInteger y = new BigInteger(task.getY());
                        BigInteger b = new BigInteger(task.getB());
                        BigInteger f1 = new BigInteger(task.getF1());
                        BigInteger f2 = new BigInteger(task.getF2());
                        BigInteger Y = y.modPow(f1, p);
                        BigInteger B = b.modPow(f2, p);
                        BigInteger c = Y.multiply(B).mod(p);

                        ArbitrationTaskUser firstUser = atuMapper.findNextArbitration(taskId, 1);

                        //把c存到d1里
                        atuMapper.updateStatus2(taskId, firstUser.getUserName(), "in_progress", String.valueOf(c));

                        return APIResponse.success("第一次验证失败，进行下一步验证"); // 返回失败响应
                    }
                }

            } else if (num.equals("2")) {
                // 第二轮验证
                String d1 = request.getD1();     // 获取参数 d
//                System.out.println(d1);
                if (nextUser != null){

                    atuMapper.updateStatus2(taskId, userName, "completed", d1); // 更新当前用户状态为“已完成”
                    atuMapper.updateStatus2(taskId, nextUser.getUserName(), "in_progress", d1); // 更新下一个用户状态为“进行中”
                    return APIResponse.success("参数更新成功"); // 返回成功响应
                } else {
                    // 本轮已结束，进行验证
                    atuMapper.updateStatus2(taskId, userName, "pending", d1); // 更新当前用户状态为“已完成”

                    BigInteger f1 = new BigInteger(task.getF1());
                    BigInteger f2 = new BigInteger(task.getF2());
                    BigInteger k = x.modPow(f1, p).multiply(g.modPow(f2, p)).mod(p); // 计算 k 的值

                    if (String.valueOf(k).equals(d1)){
                        // 验证成功
                        System.out.println("第二轮验证成功");
                        // 更新申请状态为“仲裁验证无误”
                        applicationMapper.updateApplication(String.valueOf(id), "仲裁验证完成", "签名值没有问题");
                        // 更新任务状态为“已完成”
                        taskMapper.updateTaskFields(taskId, "completed", "", "");
                        // 更新每个用户的状态为完成验证
                        atuMapper.updateStatus3(taskId, "completed", "2");

                        return APIResponse.success("第二轮验证成功"); // 返回成功响应
                    } else {
                        // 验证失败
                        System.out.println("第二轮验证失败");
//                        System.out.println(k);
//                        System.out.println(d1);

                        atuMapper.updateStatus3(taskId, "in_progress", "3");
                        applicationMapper.updateApplication(String.valueOf(id), "第二次验证失败，进行下一步验证", "在处理界面添加私钥计算");

                        // 验证t2
                        List<ArbitrationTaskUser> ATU = atuMapper.findAll(taskId);
                        for(ArbitrationTaskUser atu : ATU){
                            BigInteger t2 = new BigInteger(atu.getT2());
                            BigInteger delta = new BigInteger(atu.getDelta());
                            BigInteger ans = g.modPow(delta, p);
                            if(!t2.equals(ans)){
                                return APIResponse.success(atu.getUserName()+"的t2值验证错误");
                            }
                        }

                        ArbitrationTaskUser U2 = atuMapper.findNextArbitration(taskId, 2);
                        BigInteger d = new BigInteger(U2.getD());
                        BigInteger e1 = new BigInteger(task.getE1());
                        BigInteger e2 = new BigInteger(task.getE2());
                        BigInteger ans1 = d.multiply(g.modPow(e2.modInverse(p), p)).mod(p).modPow(f1, p);
                        BigInteger ans2 = new BigInteger(d1).multiply(g.modPow(f2.modInverse(p), p)).mod(p).modPow(e1, p);

                        if(ans1.equals(ans2)){
                            System.out.println("签名并非联合签名人的联合签名");
//                            System.out.println("ans1 = " + ans1);
//                            System.out.println("ans2 = " + ans2);

                            applicationMapper.updateApplication(String.valueOf(id), "仲裁验证完成", "签名并非联合签名人的联合签名");
                            taskMapper.updateTaskStatus(taskId, "completed");
                            atuMapper.updateStatus3(taskId, "completed", "2");
                            return APIResponse.success("签名并非联合签名人的联合签名");
                        } else {
                            // 至少有一方未诚实执行以上过程
                            // 生成知识证明挑战
                            SecureRandom random = new SecureRandom();
                            BigInteger omega = new BigInteger(1024, random);
                            BigInteger ch = omega;
                            List<String> deltas = atuMapper.findAllDelta(taskId);
                            for (String delta : deltas){
                                BigInteger p = new BigInteger(delta);
                                ch = ch.add(p);
                            }
                            atuMapper.updateStatus4(taskId, String.valueOf(ch));
                            return APIResponse.success("检查不诚实执行者");
                        }
                    }
                }
            } else {
                // 前两轮验证均失败，第三轮验证欺骗者
//                System.out.println(request);
                String S = request.getS();
                atuMapper.updateStatus5(taskId, userName, S);
                atuMapper.updateStatus0(taskId, userName, "pending");
                if (atuMapper.countEmptySByTaskId(taskId) == 0) {
                    System.out.println("验证欺骗者");
                    // 所有用户的 s 都计算完了，开始验证欺骗者
                    Task task1 = taskMapper.findTaskById(taskId);

                    StringBuilder resultBuilder = new StringBuilder();
                    int index = 1;
                    int i = 1;
                    BigInteger d1, d2 = new BigInteger("0");
                    // 遍历任务中的每个用户
                    while (true) {
                        ArbitrationTaskUser user = atuMapper.findNextArbitration(taskId, index++);
                        if (user == null) {
                            break;
                        }

                        // 验证用户诚实性
                        d1 = i == 1 ? new BigInteger(user.getC()) : d2;
                        d2 = new BigInteger(user.getD());
                        String result = verifyHonesty(user, p, g, d1, d2);
                        i = 0;
                        resultBuilder.append(result);
                    }

                    String finalResult = resultBuilder.toString();
//                    System.out.println(finalResult);

                    // 更新数据库状态
                    applicationMapper.updateApplication(String.valueOf(id), "仲裁验证完成", finalResult);
                    taskMapper.updateTaskStatus(taskId, "completed");
                    atuMapper.updateStatus3(taskId, "completed", "3");

                    return APIResponse.success(finalResult);
                }
                return APIResponse.success("等待所有用户提交私钥");
            }
        } catch (NumberFormatException e) {
            // 捕获数字格式异常，记录错误信息
            System.err.println("Error parsing number: " + e.getMessage());
            return APIResponse.error(500, "Invalid number format for y or b"); // 返回错误响应
        } catch (Exception e) {
            // 捕获其他异常，记录错误信息
            System.err.println("An unexpected error occurred: " + e.getMessage());
            return APIResponse.error(500, "An error occurred while updating the task"); // 返回错误响应
        }
    }

    // 验证用户的诚实性的方法
    private String verifyHonesty(ArbitrationTaskUser user, BigInteger p, BigInteger g, BigInteger d1, BigInteger d2) {
        BigInteger t = new BigInteger(user.getT());
        BigInteger t1 = new BigInteger(user.getT1());
        BigInteger b = new BigInteger(userMapper.KeyStatus(user.getUserName()));
        BigInteger s = new BigInteger(user.getS());
        BigInteger ch = new BigInteger(user.getCh());

        System.out.println("b = " + b);

        BigInteger ans = b.modPow(ch, p).multiply(g.modPow(s, p)).mod(p);
        BigInteger ans1 = d1.modPow(ch, p).multiply(d2.modPow(s, p)).mod(p);

//        System.out.println("b = " + b);
//        System.out.println("ans = " + ans);
//
//        System.out.println("d1 = " + d1);
//        System.out.println("ch = " + ch);
//        System.out.println("p = " + p);
//        System.out.println("d2 = " + d2);
//        System.out.println("s = " + s);
//        System.out.println("ans1 = " + ans1);

        // 返回用户的诚信状态
        return (t.equals(ans) && t1.equals(ans1)) ? user.getUserName() + "诚实" : user.getUserName() + "欺骗";
    }

    /**
     * 创建签名用户
     *
     * @param createTaskRequest 包含用户信息的请求数据传输对象
     * @param taskId 任务 ID
     */
    public void createSignUser(CreateTaskRequest createTaskRequest, int taskId) {
        try {
            String FileId = fileMapper.findFileIdByFileName(createTaskRequest.getSelectFile()); // 获取文件名
            String Outline = fileMapper.findFileOutlineByFileName(createTaskRequest.getSelectFile());
            String usagePolicy = createTaskRequest.getUsagePolicy();
            List<CreateTaskRequest.SignerMember> members = createTaskRequest.getSigner().getMembers(); // 提取成员列表

            // 拼接 members 的用户名和文件 ID和授权细则
            StringBuilder m = new StringBuilder(FileId);
            m.append(Outline);
            m.append(usagePolicy);
            for (CreateTaskRequest.SignerMember member : members) {
                m.append(member.getUsername()); // 拼接每个成员的用户名
            }

            // 对拼接结果进行哈希
            byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(m.toString().getBytes());
            BigInteger hashValue = new BigInteger(1, hashBytes); // 转换为正的 BigInteger

            // 计算哈希值的平方并对 p 取模
            BigInteger x = hashValue.multiply(hashValue).mod(p);

            taskMapper.updateTaskField(taskId, String.valueOf(x));

            // 设置第一个成员的 B 和 Y 为 g 和 x，其他成员为 0
            for (int i = 0; i < members.size(); i++) {
                CreateTaskRequest.SignerMember member = members.get(i);
                SignTaskUser signTaskUser = new SignTaskUser();

                signTaskUser.setTaskId(taskId);
                signTaskUser.setStatus(i == 0 ? "in_progress" : "pending"); // 第一个成员进行中，其他为待处理
                signTaskUser.setTaskType(createTaskRequest.getTaskType()); // 设置任务类型
                signTaskUser.setFileName(createTaskRequest.getSelectFile()); // 设置文件名
                signTaskUser.setCompletedAt(new Timestamp(System.currentTimeMillis())); // 当前时间作为完成时间
                signTaskUser.setB1("0");
                // 对第一个成员设置 B 和 Y 为 g 和 x，其他成员设置为 0
                signTaskUser.setB(i == 0 ? String.valueOf(g) : "0"); // 设置 B
                signTaskUser.setY(i == 0 ? String.valueOf(x) : "0"); // 设置 Y

                // 设置编号
                signTaskUser.setSignerNumber(i + 1); // 设置编号，从 1 开始

                signTaskUser.setUserName(member.getUsername()); // 设置用户名

//                System.out.println(signTaskUser);

                stuMapper.insertTaskUser(signTaskUser); // 插入签名用户信息
            }

        } catch (NoSuchAlgorithmException e) {
            logger.error("哈希算法异常", e); // 记录哈希算法异常
        } catch (Exception e) {
            logger.error("任务处理发生异常", e); // 记录其他异常
        }
    }


    /**
     * 创建确权用户
     *
     * @param confirmId 确权的任务ID
     * @param taskId 任务 ID
     */
    public void createConfirmUser(int confirmId, int taskId, BigInteger e1, BigInteger e2) {
        try {
            List<SignTaskUser> confirmUser = stuMapper.findTaskByTaskId(confirmId);
            Task task = taskMapper.findTaskById(taskId);
            BigInteger y = new BigInteger(task.getY());
            BigInteger b = new BigInteger(task.getB());
            int i = -1;

            // 模幂运算
            BigInteger Y = y.modPow(e1, p);
            BigInteger B = b.modPow(e2, p);
            BigInteger c = Y.multiply(B).mod(p);
            ConfirmTaskUser confirm = new ConfirmTaskUser();
            confirm.setTaskType("确权");
            if (confirmUser != null) {
                for (SignTaskUser user : confirmUser) {
                    i++;

                    confirm.setTaskId(taskId);
                    confirm.setUserName(user.getUserName());
                    confirm.setConfirmNumber(user.getSignerNumber());
                    confirm.setCompletedAt(new Timestamp(System.currentTimeMillis()));

                    // 第一个用户设置 c，其他用户设置 0
                    confirm.setD(user.getSignerNumber() == 1 ? String.valueOf(c) : "0");
                    confirm.setStatus(user.getSignerNumber() == 1 ? "in_progress" : "pending");


                    // 插入记录到数据库
//                    System.out.println(confirm);
                    ctuMapper.insertTaskUser(confirm);
                }
            }
        } catch (Exception e) {
            // 捕获所有异常并输出错误信息
            System.err.println("An error occurred while creating confirm users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建仲裁用户
     *
     * @param confirmId 确权的任务ID
     * @param taskId 任务 ID
     */
    public void createArbitrationUser(int confirmId, int taskId, BigInteger e1, BigInteger e2) {
        try {
            List<SignTaskUser> arbitrationUser = stuMapper.findTaskByTaskId(confirmId);
            Task task = taskMapper.findTaskById(taskId);
            BigInteger y = new BigInteger(task.getY());
            BigInteger b = new BigInteger(task.getB());
            int i = -1;

            // 模幂运算
            BigInteger Y = y.modPow(e1, p);
            BigInteger B = b.modPow(e2, p);
            BigInteger c = Y.multiply(B).mod(p);

            ArbitrationTaskUser arbitration = new ArbitrationTaskUser();
            arbitration.setTaskType("仲裁");
            arbitration.setT("");
            arbitration.setT1("");
            arbitration.setT2("");
            arbitration.setD1("0");
            arbitration.setCh("");
            arbitration.setDelta("");
            arbitration.setNum("1");
            arbitration.setR("");
            arbitration.setS("");
            arbitration.setC(String.valueOf(c));
            if (arbitrationUser != null) {
                for (SignTaskUser user : arbitrationUser) {
                    i++;

                    arbitration.setTaskId(taskId);
                    arbitration.setUserName(user.getUserName());
                    arbitration.setArbitrationNumber(user.getSignerNumber());
                    arbitration.setCompletedAt(new Timestamp(System.currentTimeMillis()));

                    // 第一个用户设置 c，其他用户设置 0
                    arbitration.setD(user.getSignerNumber() == 1 ? String.valueOf(c) : "0");
                    arbitration.setStatus(user.getSignerNumber() == 1 ? "in_progress" : "pending");


                    // 插入记录到数据库
//                    System.out.println(arbitration);
                    atuMapper.insertTaskUser(arbitration);
                }
            }
        } catch (Exception e) {
            // 捕获所有异常并输出错误信息
            System.err.println("An error occurred while creating confirm users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * 返回完成流转的数据参数
     *
     * @return APIResponse 包装的 DataRequset 列表
     */
    @GetMapping("/getCompletedData")
    public APIResponse<List<DataRequset>> getCompletedData() {
        try {
            // 查询状态为 "completed" 的任务
            List<Task> tasks = taskMapper.findCompletedDataTasks();
            List<DataRequset> dataRequsetList = new ArrayList<>();
            // 遍历每个任务，组装 DataRequset 对象
            for (Task task : tasks) {
                DataRequset data = new DataRequset();

                // 根据文件名查询文件信息
                File file = fileMapper.findFileByFileName(task.getFileName());
//                System.out.println(file);
                // 如果文件不存在，跳过当前任务
                if (file == null) {
                    System.err.println("File not found for fileId: " + task.getFileName());
                    continue;
                }

                // 设置 DataRequset 对象的属性
                data.setTaskId(task.getTaskId());
                data.setTime(task.getCreatedAt());
                data.setB(task.getB());
                data.setY(task.getY());
                data.setFileName(task.getFileName());
                data.setDataId(file.getFileId());
                data.setCreator(file.getCreatorName());
                data.setOutline(file.getFileOutline());
                data.setUsagePolicy(task.getUsagePolicy());

                dataRequsetList.add(data);
            }

            // 返回成功响应
            return APIResponse.success(dataRequsetList);
        } catch (Exception e) {
            // 打印异常日志并返回错误响应
            e.printStackTrace();
            return APIResponse.error(400, "获取完成流转的数据失败: " + e.getMessage());
        }
    }

}
