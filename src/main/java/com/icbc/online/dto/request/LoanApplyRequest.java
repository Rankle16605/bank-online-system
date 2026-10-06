package com.icbc.online.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplyRequest {
    @NotBlank(message = "信用账户不能为空")
    private String accountNo;

    @NotNull(message = "借款金额不能为空")
    @DecimalMin(value = "1000.00", message = "借款金额最低1000元")
    private BigDecimal loanAmount;

    @NotNull(message = "借款期限不能为空")
    @Min(value = 3, message = "借款期限最低3个月")
    private Integer loanTerm;

    private String remark;
}
