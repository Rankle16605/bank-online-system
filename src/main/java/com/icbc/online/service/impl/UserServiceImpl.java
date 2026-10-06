package com.icbc.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.icbc.online.dto.request.LoginRequest;
import com.icbc.online.dto.request.UserRegisterRequest;
import com.icbc.online.dto.response.LoginResponse;
import com.icbc.online.dto.response.UserDTO;
import com.icbc.online.exception.BusinessException;
import com.icbc.online.mapper.UserMapper;
import com.icbc.online.model.User;
import com.icbc.online.service.UserService;
import com.icbc.online.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现 (MyBatis-Plus)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserDTO register(UserRegisterRequest request) {
        // 检查用户名是否存在
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()))) {
            throw new BusinessException("用户名已存在");
        }
        // 检查手机号是否存在
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()))) {
            throw new BusinessException("手机号已注册");
        }
        // 检查身份证号是否存在
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getIdCard, request.getIdCard()))) {
            throw new BusinessException("身份证号已注册");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .idCard(request.getIdCard())
                .phone(request.getPhone())
                .email(request.getEmail())
                .role("USER")
                .status(1)
                .build();

        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());

        return convertToDTO(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用");
        }

        // 生成JWT Token
        String role = user.getRole() != null ? user.getRole() : "USER";
        String token = jwtUtil.generateToken(user.getUsername(), role);
        log.info("用户登录成功: {}, 角色: {}", user.getUsername(), role);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(convertToDTO(user))
                .build();
    }

    @Override
    public UserDTO getCurrentUser(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        log.info("用户密码修改成功: {}", user.getUsername());
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
