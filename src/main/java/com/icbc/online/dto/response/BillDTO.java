package com.icbc.online.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {

    private Long id;
    private String billNo;
    private Long userId;
    private Long accountId;
    private String billMonth;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal beginBalance;
    private BigDecimal endBalance;
    private Integer status;
    private String statusName;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
}
