package com.icbc.online.controller;

import com.icbc.online.dto.ApiResponse;
import com.icbc.online.dto.request.TransferRequest;
import com.icbc.online.dto.response.TransactionDTO;
import com.icbc.online.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易控制器
 */
@Tag(name = "交易管理", description = "转账、交易记录查询")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "发起转账")
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDTO>> transfer(
            @RequestBody @Valid TransferRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        TransactionDTO result = transactionService.transfer(username, request);
        return ResponseEntity.ok(ApiResponse.success("转账成功", result));
    }

    @Operation(summary = "查询交易记录")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getHistory(
            @RequestParam String accountNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<TransactionDTO> history = transactionService.getTransactionHistory(accountNo, page, size);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @Operation(summary = "查询交易详情")
    @GetMapping("/{transactionNo}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getDetail(@PathVariable String transactionNo) {
        TransactionDTO detail = transactionService.getTransactionDetail(transactionNo);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }
}
