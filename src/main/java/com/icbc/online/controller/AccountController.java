package com.icbc.online.controller;

import com.icbc.online.dto.ApiResponse;
import com.icbc.online.dto.response.AccountDTO;
import com.icbc.online.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账户控制器
 */
@Tag(name = "账户管理", description = "账户查询、开户")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Operation(summary = "获取我的账户列表")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAccounts() {
        List<AccountDTO> accounts = accountService.getAccounts(getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @Operation(summary = "查询账户余额")
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<AccountDTO>> getBalance(@RequestParam String accountNo) {
        AccountDTO account = accountService.getAccountBalance(accountNo);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @Operation(summary = "查询账户详情")
    @GetMapping("/{accountNo}")
    public ResponseEntity<ApiResponse<AccountDTO>> getDetail(@PathVariable String accountNo) {
        AccountDTO account = accountService.getAccountDetail(accountNo);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @Operation(summary = "创建新账户")
    @PostMapping
    public ResponseEntity<ApiResponse<AccountDTO>> createAccount(
            @RequestParam(defaultValue = "1") Integer accountType) {
        AccountDTO account = accountService.createAccount(getCurrentUsername(), accountType);
        return ResponseEntity.ok(ApiResponse.success("开户成功", account));
    }
}
