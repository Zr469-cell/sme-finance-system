package com.finance.erp.service;

import com.finance.erp.dto.AuthDTO;

public interface UserService {
    AuthDTO.LoginResponse login(AuthDTO.LoginRequest request);
    void register(AuthDTO.RegisterRequest request);
}