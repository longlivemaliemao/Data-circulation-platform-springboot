package com.example.demo.Mapper;

import com.example.demo.Model.ArbitrationTaskUser;
import com.example.demo.Model.ConfirmTaskUser;
import com.example.demo.Model.Task;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ATUMapper {
    @Insert("INSERT INTO ARBITRATION_USER (task_id, arbitration_number, d, t, t1, t2, user_name, status, task_type, completed_at, d1, delta, ch, num, r, s, c) " +
            "VALUES (#{taskId}, #{arbitrationNumber}, #{d}, #{t}, #{t1}, #{t2}, #{userName}, #{status}, #{taskType}, #{completedAt}, #{d1}, #{delta}, #{ch}, #{num}, #{r}, #{s}, #{c})")
    void insertTaskUser(ArbitrationTaskUser arbitrationTaskUser);

    // 根据用户名查找状态为 in_progress 的所有记录
    @Select("SELECT * FROM ARBITRATION_USER WHERE user_name = #{userName}")
    List<ArbitrationTaskUser> findInProgressTasksByUserName(String userName);

    // 根据 taskId 和 userName 更新 status,d1,num
    @Update("UPDATE ARBITRATION_USER SET status = #{status} WHERE task_id = #{taskId} AND user_name = #{userName}")
    void updateStatus0(int taskId, String userName, String status);

    // 根据 taskId 和 userName 更新 status,t,t1,t2,d,r,delta
    @Update("UPDATE ARBITRATION_USER SET status = #{status}, d= #{d}, t=#{t}, t1=#{t1}, t2=#{t2}, r=#{r}, delta=#{delta} WHERE task_id = #{taskId} AND user_name = #{userName}")
    void updateStatus1(int taskId, String userName, String status, String d, String t, String t1, String t2, String r, String delta);

    // 根据 taskId 和 userName 更新 status,d1
    @Update("UPDATE ARBITRATION_USER SET status = #{status}, d1= #{d1} WHERE task_id = #{taskId} AND user_name = #{userName}")
    void updateStatus2(int taskId, String userName, String status, String d1);

    // 根据 taskId 和 userName 更新 status num
    @Update("UPDATE ARBITRATION_USER SET status = #{status}, num = #{num} WHERE task_id = #{taskId}")
    void updateStatus3(int taskId, String status, String num);

    // 根据 taskId 更新 ch
    @Update("UPDATE ARBITRATION_USER SET ch = #{ch} WHERE task_id = #{taskId}")
    void updateStatus4(int taskId, String ch);

    // 根据 taskId 和 userName 更新 s
    @Update("UPDATE ARBITRATION_USER SET s = #{s} WHERE task_id = #{taskId} AND user_name = #{userName}")
    void updateStatus5(int taskId, String userName, String s);


    // 根据 taskId 和 arbitrationNumber 查找用户
    @Select("SELECT * FROM ARBITRATION_USER WHERE task_id = #{taskId} AND arbitration_number = #{arbitrationNumber}")
    ArbitrationTaskUser findNextArbitration(int taskId, int arbitrationNumber);

    // 根据 taskId 和 username 查找 arbitrationNumber
    @Select("SELECT arbitration_number FROM ARBITRATION_USER WHERE task_id = #{taskId} AND user_name = #{username}")
    int findArbitrationNumber(int taskId, String username);


    // 根据 taskId 查找 所有 delta
    @Select("SELECT delta FROM ARBITRATION_USER WHERE task_id = #{taskId}")
    List<String> findAllDelta(int taskId);

    // 根据 taskId 查找所有用户的所有参数
    @Select("SELECT * FROM ARBITRATION_USER WHERE task_id = #{taskId}")
    List<ArbitrationTaskUser> findAll(int taskId);

    // 所有的 s 字段都不是空字符串
    @Select("SELECT COUNT(*) FROM ARBITRATION_USER WHERE task_id = #{taskId} AND s IS NULL")
    int countEmptySByTaskId(int taskId);

}
