package com.icbc.online.controller;

import com.icbc.online.dto.ApiResponse;
import com.icbc.online.dto.response.BillDTO;
import com.icbc.online.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账单控制器
 */
@Tag(name = "账单服务", description = "账单查询与生成")
@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @Operation(summary = "获取账单列表")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BillDTO>>> getBills() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<BillDTO> bills = billService.getBills(username);
        return ResponseEntity.ok(ApiResponse.success(bills));
    }

    @Operation(summary = "获取账单详情")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillDTO>> getBillDetail(@PathVariable Long id) {
        BillDTO bill = billService.getBillDetail(id);
        return ResponseEntity.ok(ApiResponse.success(bill));
    }
}
