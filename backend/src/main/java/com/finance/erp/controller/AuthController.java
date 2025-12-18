package com.finance.erp.controller;

import com.finance.erp.common.Result;
import com.finance.erp.dto.AuthDTO;
import com.finance.erp.service.impl.UserServiceImpl; // 注意：为了方便调用新增方法，这里直接用 Impl 或需更新 Interface
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImpl userService; // 注入实现类以调用新方法

    @PostMapping("/login")
    public Result<AuthDTO.LoginResponse> login(@RequestBody AuthDTO.LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody AuthDTO.RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    // [新增] 发送验证码
    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestParam String phone) {
        userService.sendVerifyCode(phone);
        return Result.success();
    }

    // [新增] 重置密码
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody AuthDTO.ResetPasswordRequest request) {
        userService.resetPassword(request);
        return Result.success();
    }
}