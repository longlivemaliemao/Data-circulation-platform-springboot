package com.example.demo.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetCompletedDataDTO {
    int taskId;
    String fileName;
    String creatorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp begin;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp end;
}
