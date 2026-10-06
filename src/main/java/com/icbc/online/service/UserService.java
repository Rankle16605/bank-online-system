package com.icbc.online.service;

import com.icbc.online.dto.request.LoginRequest;
import com.icbc.online.dto.request.UserRegisterRequest;
import com.icbc.online.dto.response.LoginResponse;
import com.icbc.online.dto.response.UserDTO;

/**
 * 用户服务接口
 */
public interface UserService {

    UserDTO register(UserRegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserDTO getCurrentUser(String username);

    UserDTO getUserById(Long id);

    void updatePassword(Long userId, String oldPassword, String newPassword);
}
