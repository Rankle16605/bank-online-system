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
public class LoanRepaymentDTO {
    private Long id;
    private String repaymentNo;
    private Long loanId;
    private Integer periodNo;
    private BigDecimal amount;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal actualAmount;
    private LocalDateTime scheduledDate;
    private LocalDateTime actualDate;
    private Integer status;
    private String statusText;
}
