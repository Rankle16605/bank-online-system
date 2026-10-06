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
@TableName("tb_loan_repayment")
public class LoanRepayment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("repayment_no")
    private String repaymentNo;

    @TableField("loan_id")
    private Long loanId;

    @TableField("period_no")
    private Integer periodNo;

    private BigDecimal amount;

    private BigDecimal principal;

    private BigDecimal interest;

    @TableField("actual_amount")
    @Builder.Default
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @TableField("scheduled_date")
    private LocalDateTime scheduledDate;

    @TableField("actual_date")
    private LocalDateTime actualDate;

    @Builder.Default
    private Integer status = 0;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
