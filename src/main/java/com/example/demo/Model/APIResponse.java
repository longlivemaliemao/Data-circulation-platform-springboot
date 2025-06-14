package com.example.demo.Model;

public class APIResponse<T> {
    private int code;           // 响应状态码
    private String message;      // 响应提示信息
    private T data;              // 响应数据

    public APIResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 快速构造一个成功的响应
    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<>(200, "Success", data);
    }

    // 快速构造一个错误的响应
    public static <T> APIResponse<T> error(int code, String message) {
        return new APIResponse<>(code, message, null);
    }

    // Getter 和 Setter 方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "APIResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}

