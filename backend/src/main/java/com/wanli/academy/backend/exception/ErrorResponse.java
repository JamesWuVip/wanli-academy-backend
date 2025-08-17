package com.wanli.academy.backend.exception;

/**
 * 统一错误响应DTO
 * 用于返回标准化的错误信息
 */
public class ErrorResponse {
    private String code;
    private String message;
    private long timestamp;
    
    public ErrorResponse() {
    }
    
    public ErrorResponse(String code, String message, long timestamp) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    // Getter方法
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    // Setter方法
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "ErrorResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}