package com.icbc.online.controller;

import com.icbc.online.dto.ApiResponse;
import com.icbc.online.dto.request.LoginRequest;
import com.icbc.online.dto.request.UserRegisterRequest;
import com.icbc.online.dto.response.LoginResponse;
import com.icbc.online.dto.response.UserDTO;
import com.icbc.online.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息管理")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@RequestBody @Valid UserRegisterRequest request) {
        UserDTO user = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功", user));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO user = userService.getCurrentUser(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @Operation(summary = "获取指定用户信息")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
