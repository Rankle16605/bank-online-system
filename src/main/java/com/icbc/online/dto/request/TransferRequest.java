package com.icbc.online.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 转账请求
 */
@Data
public class TransferRequest {

    @NotBlank(message = "转出账户不能为空")
    private String fromAccount;

    @NotBlank(message = "转入账户不能为空")
    private String toAccount;

    @NotNull(message = "转账金额不能为空")
    @Positive(message = "转账金额必须大于0")
    private BigDecimal amount;

    private String remark;
}
