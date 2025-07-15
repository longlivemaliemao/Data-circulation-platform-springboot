package com.example.demo.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.Model.File;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface FileMapper extends BaseMapper<File> {

    // 插入新的文件记录
    @Insert("INSERT INTO files (file_id, file_name, file_path, usage_time, creator_name, FILE_OUTLINE, TOTAL_CHUNKS, UPLOADED_CHUNKS) " +
            "VALUES (#{fileId}, #{fileName}, #{filePath}, #{usageTime}, #{creatorName}, #{fileOutline}, #{totalChunks}, #{uploadedChunks})")
    int insert(File file);

    // 根据 creator_name 查找所有 file_name
    @Select("SELECT file_name FROM files WHERE creator_name = #{creator_name}")
    List<String> findFileByCreatorName(String creator_name);

    // 根据 creator_name 查找所有 file
    @Select("SELECT file_name, usage_time, FILE_OUTLINE FROM files WHERE creator_name = #{creator_name}")
    List<File> findFilesByCreatorName(String creator_name);

    // 根据 file_name 查找 file
    @Select("SELECT * FROM files WHERE file_name = #{fileName}")
    File findFileByFileName(String fileName);

    // 根据 file_name 查找 file_id
    @Select("SELECT file_id FROM files WHERE file_name = #{fileName}")
    String findFileIdByFileName(String fileName);

    // 根据 file_name 查找 file_id
    @Select("SELECT FILE_OUTLINE FROM files WHERE file_name = #{fileName}")
    String findFileOutlineByFileName(String fileName);

    @Update("UPDATE files SET UPLOADED_CHUNKS = #{uploadedChunks} WHERE file_id = #{fileId}")
    void updateChunksByFileID(int uploadedChunks, String fileId);

    @Select("select UPLOADED_CHUNKS, TOTAL_CHUNKS from FILES where CREATOR_NAME = #{creator_name} AND FILE_NAME = #{fileName}")
    Map<String, String> selectChunksByFileName(String creator_name, String fileName);

}
