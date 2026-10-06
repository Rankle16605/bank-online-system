package com.icbc.online.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long id;
    private String transactionNo;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private Integer transactionType;
    private String transactionTypeName;
    private Integer status;
    private String statusName;
    private String remark;
    private LocalDateTime createdAt;
}
