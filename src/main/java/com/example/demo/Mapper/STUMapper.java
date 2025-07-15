package com.example.demo.Mapper;

import com.example.demo.Model.ProcessStatusVO;
import com.example.demo.Model.SignTaskUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface STUMapper {

    // 插入新的 task_user 记录
    @Insert("INSERT INTO signtask_user (task_id, file_name, task_type, user_name, status, B, Y, completed_at, signer_number) " +
            "VALUES (#{taskId}, #{fileName}, #{taskType}, #{userName}, #{status}, #{b}, #{y}, #{completedAt}, #{signerNumber})")
    void insertTaskUser(SignTaskUser taskUser);

    // 根据用户名查找 的所有记录
    @Select("SELECT * FROM signtask_user WHERE user_name = #{userName}")
    List<SignTaskUser> findTasksByUserName(String userName);

    // 根据 taskId 和 userName 更新 status, y, b
    @Update("UPDATE signtask_user SET status = #{status}, y = #{y}, b = #{b} " +
            "WHERE task_id = #{taskId} AND user_name = #{userName}")
    void updateStatusYB(int taskId, String userName, String status, String y, String b);

    // 根据 taskId 和 userName 更新 status
    @Update("UPDATE signtask_user SET status = #{status} WHERE task_id = #{taskId} AND user_name = #{userName}")
    void updateStatus(int taskId, String userName, String status);

    // 根据 taskId 和 signerNumber 查找用户
    @Select("SELECT * FROM SIGNTASK_USER WHERE task_id = #{taskId} AND signer_number = #{signerNumber}")
    SignTaskUser findNextSigner(int taskId, int signerNumber);

    // 根据 taskId 和 username 查找 signerNumber
    @Select("SELECT signer_number FROM SIGNTASK_USER WHERE task_id = #{taskId} AND user_name = #{username}")
    int findSignerNumber(int taskId, String username);

    // 根据 taskId 查找所有的任务
    @Select("SELECT * FROM signtask_user WHERE task_id = #{taskId} ORDER BY SIGNER_NUMBER")
    List<SignTaskUser> findTaskByTaskId(int taskId);

    // 根据 taskId 和 username 查找用户
    @Select("SELECT status FROM SIGNTASK_USER WHERE task_id = #{taskId} AND user_name = #{username}")
    String findSigner(int taskId, String username);

    @Select("SELECT s.user_name, s.status, u.role, s.signer_number as sort FROM signtask_user s join users u" +
            " on s.user_name = u.username WHERE task_id = #{taskId}")
    List<ProcessStatusVO> findUsersByTaskId(int taskId);

}

