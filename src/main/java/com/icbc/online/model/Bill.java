package com.icbc.online.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_bill")
public class Bill {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("bill_no")
    private String billNo;

    @TableField("user_id")
    private Long userId;

    @TableField("account_id")
    private Long accountId;

    @TableField("bill_month")
    private String billMonth;

    @TableField("total_income")
    @Builder.Default
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @TableField("total_expense")
    @Builder.Default
    private BigDecimal totalExpense = BigDecimal.ZERO;

    @TableField("begin_balance")
    private BigDecimal beginBalance;

    @TableField("end_balance")
    private BigDecimal endBalance;

    @Builder.Default
    private Integer status = 0;

    @TableField("generated_at")
    private LocalDateTime generatedAt;

    @TableField("created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
