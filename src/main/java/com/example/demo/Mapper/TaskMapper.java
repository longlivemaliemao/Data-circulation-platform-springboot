package com.example.demo.Mapper;

import com.example.demo.Model.SignTaskUser;
import com.example.demo.Model.Task;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TaskMapper {

    @SelectKey(statement = "SELECT TASK_SEQ.NEXTVAL FROM DUAL", keyProperty = "taskId", before = true, resultType = Integer.class)
    @Insert("INSERT INTO task (task_id, task_type, confirm_id, created_at, status, file_name, y, b, x, e1, e2, username, APPLICATION_ID, f1, f2, SIGNAPPLICATION_ID, USAGEPOLICY) " +
            "VALUES (#{taskId}, #{taskType}, #{confirmId}, #{createdAt}, #{status}, #{fileName}, #{y}, #{b}, #{x}, #{e1}, #{e2}, #{username}, #{applicationId}, #{f1}, #{f2}, #{signApplicationId}, #{usagePolicy})")
    int insert(Task task);

    // 根据 taskId 修改 status y b
    @Update("UPDATE task SET status = #{status}, Y = #{y}, B = #{b} WHERE task_id = #{taskId}")
    void updateTaskFields(int taskId, String status, String y, String b);

    // 根据 taskId 修改 x
    @Update("UPDATE task SET X = #{x} WHERE task_id = #{taskId}")
    void updateTaskField(int taskId, String x);

    // 根据 taskId 修改 status
    @Update("UPDATE task SET status = #{status} WHERE task_id = #{taskId}")
    void updateTaskStatus(int taskId, String status);

    // 根据 taskId 查找任务
    @Select("SELECT * FROM task WHERE task_id = #{taskId}")
    Task findTaskById(int taskId);

    // 根据 taskId 查找对应签名任务申请ID
    @Select("SELECT SIGNAPPLICATION_ID FROM task WHERE task_id = #{taskId}")
    void findSignTaskIDById(int taskId);

    // 根据状态和任务类型查询所有状态为 completed 且任务类型为签名的任务
    @Select("SELECT * FROM task WHERE status = 'completed' AND task_type = '签名'")
    List<Task> findCompletedDataTasks();

}
