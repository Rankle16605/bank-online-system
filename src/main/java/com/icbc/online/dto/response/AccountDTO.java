package com.icbc.online.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private Long id;
    private String accountNo;
    private Long userId;
    private String username;
    private Integer accountType;
    private String accountTypeName;
    private BigDecimal balance;
    private BigDecimal frozenAmount;
    private BigDecimal availableBalance;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private Integer billingDay;
    private Integer dueDay;
    private Integer status;
    private LocalDateTime createdAt;
}
