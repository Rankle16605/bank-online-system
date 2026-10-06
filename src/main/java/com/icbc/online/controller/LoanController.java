package com.icbc.online.controller;

import com.icbc.online.dto.ApiResponse;
import com.icbc.online.dto.request.LoanApplyRequest;
import com.icbc.online.dto.response.LoanDTO;
import com.icbc.online.dto.response.LoanRepaymentDTO;
import com.icbc.online.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "借款管理", description = "信用卡借款申请、还款")
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Operation(summary = "申请借款")
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanDTO>> applyLoan(@RequestBody @Valid LoanApplyRequest request) {
        LoanDTO loan = loanService.applyLoan(getCurrentUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("借款申请已提交", loan));
    }

    @Operation(summary = "获取我的借款列表")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<LoanDTO>>> getMyLoans() {
        List<LoanDTO> loans = loanService.getMyLoans(getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @Operation(summary = "获取借款详情")
    @GetMapping("/{loanNo}")
    public ResponseEntity<ApiResponse<LoanDTO>> getLoanDetail(@PathVariable String loanNo) {
        LoanDTO loan = loanService.getLoanDetail(loanNo);
        return ResponseEntity.ok(ApiResponse.success(loan));
    }

    @Operation(summary = "获取还款计划")
    @GetMapping("/{loanNo}/repayments")
    public ResponseEntity<ApiResponse<List<LoanRepaymentDTO>>> getRepayments(@PathVariable String loanNo) {
        List<LoanRepaymentDTO> repayments = loanService.getRepayments(loanNo);
        return ResponseEntity.ok(ApiResponse.success(repayments));
    }

    @Operation(summary = "执行还款")
    @PostMapping("/repay/{repaymentId}")
    public ResponseEntity<ApiResponse<LoanRepaymentDTO>> repay(@PathVariable Long repaymentId) {
        LoanRepaymentDTO result = loanService.repay(repaymentId);
        return ResponseEntity.ok(ApiResponse.success("还款成功", result));
    }
}
