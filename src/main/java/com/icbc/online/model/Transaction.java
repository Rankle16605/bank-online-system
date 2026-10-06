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
 * 交易记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_transaction")
public class Transaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("transaction_no")
    private String transactionNo;

    @TableField("from_account")
    private String fromAccount;

    @TableField("to_account")
    private String toAccount;

    private BigDecimal amount;

    @TableField("transaction_type")
    private Integer transactionType;

    @Builder.Default
    private Integer status = 1;

    private String remark;

    @TableField("created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
