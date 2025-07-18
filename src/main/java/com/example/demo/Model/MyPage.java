package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyPage<T> {
    Long pageNum;
    Long pageSize;
    List<T> result;
    Long total;
}
