package com.icbc.online.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    // 概览统计
    private Long totalUsers;
    private Long totalAccounts;
    private Long totalTransactions;
    private BigDecimal totalBalance;
    private Long totalLoans;
    private BigDecimal totalLoanAmount;
    private Long activeUsersToday;
    private Long transactionsToday;
    private BigDecimal amountToday;

    // 图表数据
    private List<Map<String, Object>> transactionTrend;    // 近7天交易趋势
    private List<Map<String, Object>> accountTypeDistribution; // 账户类型分布
    private List<Map<String, Object>> loanStatusDistribution;  // 借款状态分布
    private List<Map<String, Object>> topUsers;                // 资产TOP用户
    private List<Map<String, Object>> recentTransactions;      // 最近交易
    private List<Map<String, Object>> recentLoans;             // 最近借款
}
