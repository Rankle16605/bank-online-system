package com.icbc.online.model;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("tb_loan")
public class Loan {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("loan_no")
    private String loanNo;

    @TableField("user_id")
    private Long userId;

    @TableField("account_id")
    private Long accountId;

    @TableField("loan_amount")
    private BigDecimal loanAmount;

    @TableField("approved_amount")
    private BigDecimal approvedAmount;

    @TableField("interest_rate")
    private BigDecimal interestRate;

    @TableField("loan_term")
    private Integer loanTerm;

    @TableField("monthly_payment")
    private BigDecimal monthlyPayment;

    @TableField("total_repay")
    private BigDecimal totalRepay;

    @TableField("repaid_amount")
    @Builder.Default
    private BigDecimal repaidAmount = BigDecimal.ZERO;

    @TableField("remaining_periods")
    @Builder.Default
    private Integer remainingPeriods = 0;

    @Builder.Default
    private Integer status = 1;

    @TableField("apply_date")
    @Builder.Default
    private LocalDateTime applyDate = LocalDateTime.now();

    @TableField("approve_date")
    private LocalDateTime approveDate;

    @TableField("start_date")
    private LocalDateTime startDate;

    @TableField("due_date")
    private LocalDateTime dueDate;

    private String remark;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
