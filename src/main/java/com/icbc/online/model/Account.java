package com.icbc.online.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_account")
public class Account {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("account_no")
    private String accountNo;

    @TableField("user_id")
    private Long userId;

    @TableField("account_type")
    private Integer accountType;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @TableField("frozen_amount")
    @Builder.Default
    private BigDecimal frozenAmount = BigDecimal.ZERO;

    @TableField("credit_limit")
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @TableField("available_credit")
    @Builder.Default
    private BigDecimal availableCredit = BigDecimal.ZERO;

    @TableField("billing_day")
    @Builder.Default
    private Integer billingDay = 1;

    @TableField("due_day")
    @Builder.Default
    private Integer dueDay = 15;

    @Builder.Default
    private Integer status = 1;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
