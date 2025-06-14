package com.example.demo.Mapper;

import com.example.demo.Model.ConfirmTaskUser;
import com.example.demo.Model.SignTaskUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CTUMapper {

    // 插入新的 task_user 记录
    @Insert("INSERT INTO CONFIRM_USER (task_id,user_name, status, task_type, D, CONFIRM_NUMBER, completed_at) " +
            "VALUES (#{taskId}, #{userName}, #{status}, #{taskType},  #{d}, #{confirmNumber}, #{completedAt})")
    void insertTaskUser(ConfirmTaskUser taskUser);

    // 根据用户名查找所有记录
    @Select("SELECT * FROM CONFIRM_USER WHERE user_name = #{userName}")
    List<ConfirmTaskUser> findTasksByUserName(String userName);

    // 根据 taskId 查找所有的任务
    @Select("SELECT * FROM CONFIRM_USER WHERE task_id = #{taskId}")
    List<ConfirmTaskUser> findTaskByTaskId(int taskId);

    // 根据 taskId 和 userName 更新 status
    @Update("UPDATE CONFIRM_USER SET status = #{status}, d= #{d} " +
            "WHERE task_id = #{taskId} AND user_name = #{userName}")
    void updateStatusD(int taskId, String userName, String status, String d);

    // 根据 taskId 和 signerNumber 查找用户
    @Select("SELECT * FROM CONFIRM_USER WHERE task_id = #{taskId} AND confirm_number = #{confirmNumber}")
    ConfirmTaskUser findNextConfirm(int taskId, int confirmNumber);

    // 根据 taskId 和 username 查找 signerNumber
    @Select("SELECT confirm_number FROM CONFIRM_USER WHERE task_id = #{taskId} AND user_name = #{username}")
    int findConfirmNumber(int taskId, String username);

    // 根据 taskId 和 username 查找 D
    @Select("SELECT d FROM CONFIRM_USER WHERE task_id = #{taskId} AND user_name = #{username}")
    String findTask(int taskId, String username);

    // 根据 taskId 和 username 查找用户
    @Select("SELECT status FROM CONFIRM_USER WHERE task_id = #{taskId} AND user_name = #{username}")
    String findConfirm(int taskId, String username);


    // 根据 taskId 和 userName 更新 status
    @Update("UPDATE CONFIRM_USER SET status = #{status} WHERE task_id = #{taskId} AND user_name = #{userName}")
    void updateStatus(int taskId, String userName, String status);


}
