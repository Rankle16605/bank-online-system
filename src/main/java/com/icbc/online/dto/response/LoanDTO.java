package com.icbc.online.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {
    private Long id;
    private String loanNo;
    private Long userId;
    private String username;
    private Long accountId;
    private String accountNo;
    private BigDecimal loanAmount;
    private BigDecimal approvedAmount;
    private BigDecimal interestRate;
    private Integer loanTerm;
    private BigDecimal monthlyPayment;
    private BigDecimal totalRepay;
    private BigDecimal repaidAmount;
    private BigDecimal remainingAmount;
    private Integer remainingPeriods;
    private Integer status;
    private String statusText;
    private LocalDateTime applyDate;
    private LocalDateTime approveDate;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private String remark;
    private LocalDateTime createdAt;
}
