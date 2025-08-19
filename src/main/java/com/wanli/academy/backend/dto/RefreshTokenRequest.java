package com.wanli.academy.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新令牌请求DTO
 */
public class RefreshTokenRequest {
    
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
    
    // 默认构造函数
    public RefreshTokenRequest() {}
    
    // 构造函数
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Getter和Setter方法
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    @Override
    public String toString() {
        return "RefreshTokenRequest{" +
                "refreshToken='[PROTECTED]'" +
                '}';
    }
}