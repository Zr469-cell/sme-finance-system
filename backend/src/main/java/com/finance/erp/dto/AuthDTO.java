package com.finance.erp.dto;

import lombok.Data;

public class AuthDTO {

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
    }
    
    @Data
    public static class LoginResponse {
        private Long id;
        private String username;
        private String token;
        
        public LoginResponse(Long id, String username, String token) {
            this.id = id;
            this.username = username;
            this.token = token;
        }
    }

    // [新增] 重置密码请求
    @Data
    public static class ResetPasswordRequest {
        private String phone;
        private String code;
        private String newPassword;
    }
}