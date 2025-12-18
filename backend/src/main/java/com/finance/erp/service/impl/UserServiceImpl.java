package com.finance.erp.service.impl;

import com.finance.erp.common.exception.BusinessException;
import com.finance.erp.dto.AuthDTO;
import com.finance.erp.entity.User;
import com.finance.erp.repository.UserRepository;
import com.finance.erp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!user.getPasswordHash().equals(request.getPassword())) {
            throw new BusinessException("密码错误");
        }

        String token = UUID.randomUUID().toString();
        return new AuthDTO.LoginResponse(user.getId(), user.getUsername(), token);
    }

    @Override
    @Transactional
    public void register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRole("USER");

        userRepository.save(user);
    }

    // [新增] 模拟发送验证码
    public void sendVerifyCode(String phone) {
        // 实际场景：调用阿里云/腾讯云短信接口
        // 这里仅做模拟：假设验证码是 123456
        System.out.println("向手机 " + phone + " 发送验证码: 123456");
    }

    // [新增] 重置密码
    @Transactional
    public void resetPassword(AuthDTO.ResetPasswordRequest request) {
        // 1. 校验验证码 (模拟)
        if (!"123456".equals(request.getCode())) {
            throw new BusinessException("验证码错误");
        }

        // 2. 查找用户
        // 注意：之前 User 表没设计 phone 字段，这里为了演示，假设前端传的 phone 其实是用户名
        // 或者您可以在 User 实体加一个 phone 字段。这里暂时用 username 匹配。
        User user = userRepository.findByUsername(request.getPhone()) 
                .orElseThrow(() -> new BusinessException("用户不存在 (请确认输入的账号/手机号)"));

        // 3. 更新密码
        user.setPasswordHash(request.getNewPassword());
        userRepository.save(user);
    }
}