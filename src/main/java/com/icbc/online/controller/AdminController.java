package com.icbc.online.controller;

import com.icbc.online.dto.ApiResponse;
import com.icbc.online.dto.request.RechargeRequest;
import com.icbc.online.dto.response.*;
import com.icbc.online.service.AdminService;
import com.icbc.online.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员系统", description = "管理员仪表盘、用户管理、充值")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final LoanService loanService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboard()));
    }

    @Operation(summary = "获取所有用户")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllUsers()));
    }

    @Operation(summary = "查看用户账户")
    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getUserAccounts(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserAccounts(userId)));
    }

    @Operation(summary = "获取所有账户")
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAllAccounts() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllAccounts()));
    }

    @Operation(summary = "给用户充值")
    @PostMapping("/recharge")
    public ResponseEntity<ApiResponse<AccountDTO>> recharge(@RequestBody @Valid RechargeRequest request) {
        AccountDTO result = adminService.recharge(request);
        return ResponseEntity.ok(ApiResponse.success("充值成功", result));
    }

    @Operation(summary = "获取所有交易记录")
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllTransactions(page, size)));
    }

    @Operation(summary = "获取所有借款")
    @GetMapping("/loans")
    public ResponseEntity<ApiResponse<List<LoanDTO>>> getAllLoans() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllLoans()));
    }

    @Operation(summary = "审批借款")
    @PostMapping("/loans/{loanNo}/approve")
    public ResponseEntity<ApiResponse<LoanDTO>> approveLoan(@PathVariable String loanNo) {
        LoanDTO result = loanService.approveLoan(loanNo);
        return ResponseEntity.ok(ApiResponse.success("借款已审批", result));
    }

    @Operation(summary = "拒绝借款")
    @PostMapping("/loans/{loanNo}/reject")
    public ResponseEntity<ApiResponse<LoanDTO>> rejectLoan(
            @PathVariable String loanNo,
            @RequestParam(defaultValue = "不符合贷款条件") String reason) {
        LoanDTO result = loanService.rejectLoan(loanNo, reason);
        return ResponseEntity.ok(ApiResponse.success("借款已拒绝", result));
    }
}
