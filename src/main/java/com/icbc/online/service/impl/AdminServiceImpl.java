package com.icbc.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.icbc.online.dto.request.RechargeRequest;
import com.icbc.online.dto.response.*;
import com.icbc.online.exception.BusinessException;
import com.icbc.online.mapper.*;
import com.icbc.online.model.*;
import com.icbc.online.service.AdminService;
import com.icbc.online.util.BizNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private final LoanMapper loanMapper;
    private final AccountFlowMapper accountFlowMapper;
    private final OperationLogMapper operationLogMapper;
    private final BillMapper billMapper;

    @Override
    public AdminDashboardDTO getDashboard() {
        LocalDateTime today = LocalDate.now().atStartOfDay();

        // 基础统计
        long totalUsers = userMapper.selectCount(null);
        long totalAccounts = accountMapper.selectCount(null);
        long totalTransactions = transactionMapper.selectCount(null);
        long totalLoans = loanMapper.selectCount(null);

        BigDecimal totalBalance = accountMapper.selectList(null).stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLoanAmount = loanMapper.selectList(null).stream()
                .map(Loan::getApprovedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long transactionsToday = transactionMapper.selectList(null).stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(today))
                .count();

        BigDecimal amountToday = transactionMapper.selectList(null).stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(today) && t.getStatus() == 1)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeUsersToday = operationLogMapper.selectList(null).stream()
                .filter(l -> l.getOperationTime() != null && l.getOperationTime().isAfter(today))
                .map(OperationLog::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        // 近7天交易趋势
        List<Map<String, Object>> transactionTrend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<Transaction> allTransactions = transactionMapper.selectList(null);
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            long count = allTransactions.stream()
                    .filter(t -> t.getCreatedAt() != null
                            && !t.getCreatedAt().isBefore(dayStart)
                            && t.getCreatedAt().isBefore(dayEnd))
                    .count();
            BigDecimal amount = allTransactions.stream()
                    .filter(t -> t.getCreatedAt() != null
                            && !t.getCreatedAt().isBefore(dayStart)
                            && t.getCreatedAt().isBefore(dayEnd)
                            && t.getStatus() == 1)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> day = new HashMap<>();
            day.put("date", date.format(fmt));
            day.put("count", count);
            day.put("amount", amount);
            transactionTrend.add(day);
        }

        // 账户类型分布
        List<Map<String, Object>> accountTypeDistribution = new ArrayList<>();
        Map<Integer, Long> typeGroups = accountMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(Account::getAccountType, Collectors.counting()));
        typeGroups.forEach((type, count) -> {
            Map<String, Object> dist = new HashMap<>();
            dist.put("type", type);
            dist.put("name", type == 1 ? "储蓄卡" : "信用卡");
            dist.put("count", count);
            accountTypeDistribution.add(dist);
        });

        // 借款状态分布
        List<Map<String, Object>> loanStatusDistribution = new ArrayList<>();
        Map<Integer, Long> statusGroups = loanMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(Loan::getStatus, Collectors.counting()));
        String[] statusNames = {"已拒绝", "审核中", "已批准", "还款中", "已结清", "已逾期"};
        statusGroups.forEach((status, count) -> {
            Map<String, Object> dist = new HashMap<>();
            dist.put("status", status);
            dist.put("name", status >= 0 && status < statusNames.length ? statusNames[status] : "未知");
            dist.put("count", count);
            loanStatusDistribution.add(dist);
        });

        // TOP用户
        List<Map<String, Object>> topUsers = accountMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(Account::getUserId,
                        Collectors.reducing(BigDecimal.ZERO, Account::getBalance, BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    User user = userMapper.selectById(e.getKey());
                    Map<String, Object> top = new HashMap<>();
                    top.put("userId", e.getKey());
                    top.put("username", user != null ? user.getUsername() : "未知");
                    top.put("totalBalance", e.getValue());
                    return top;
                })
                .collect(Collectors.toList());

        // 最近交易
        List<Map<String, Object>> recentTransactions = transactionMapper.selectList(
                        new LambdaQueryWrapper<Transaction>().orderByDesc(Transaction::getCreatedAt).last("LIMIT 10"))
                .stream()
                .map(t -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("transactionNo", t.getTransactionNo());
                    m.put("amount", t.getAmount());
                    m.put("type", t.getTransactionType());
                    m.put("createdAt", t.getCreatedAt());
                    return m;
                })
                .collect(Collectors.toList());

        // 最近借款
        List<Map<String, Object>> recentLoans = loanMapper.selectList(
                        new LambdaQueryWrapper<Loan>().orderByDesc(Loan::getApplyDate).last("LIMIT 5"))
                .stream()
                .map(l -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("loanNo", l.getLoanNo());
                    m.put("amount", l.getApprovedAmount());
                    m.put("status", l.getStatus());
                    m.put("applyDate", l.getApplyDate());
                    return m;
                })
                .collect(Collectors.toList());

        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalAccounts(totalAccounts)
                .totalTransactions(totalTransactions)
                .totalBalance(totalBalance)
                .totalLoans(totalLoans)
                .totalLoanAmount(totalLoanAmount)
                .activeUsersToday(activeUsersToday)
                .transactionsToday(transactionsToday)
                .amountToday(amountToday)
                .transactionTrend(transactionTrend)
                .accountTypeDistribution(accountTypeDistribution)
                .loanStatusDistribution(loanStatusDistribution)
                .topUsers(topUsers)
                .recentTransactions(recentTransactions)
                .recentLoans(recentLoans)
                .build();
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userMapper.selectList(null).stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountDTO> getUserAccounts(Long userId) {
        return accountMapper.selectList(
                        new LambdaQueryWrapper<Account>().eq(Account::getUserId, userId))
                .stream()
                .map(a -> {
                    User u = userMapper.selectById(userId);
                    return convertAccountToDTO(a, u);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountDTO> getAllAccounts() {
        return accountMapper.selectList(null).stream()
                .map(a -> {
                    User u = userMapper.selectById(a.getUserId());
                    return convertAccountToDTO(a, u);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountDTO recharge(RechargeRequest request) {
        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getAccountNo, request.getAccountNo()));
        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        BigDecimal before = account.getBalance();
        account.setBalance(before.add(request.getAmount()));
        accountMapper.updateById(account);

        // 记录流水
        AccountFlow flow = AccountFlow.builder()
                .accountId(account.getId())
                .flowType(1)
                .amount(request.getAmount())
                .balanceBefore(before)
                .balanceAfter(account.getBalance())
                .remark(request.getRemark() != null ? request.getRemark() : "管理员充值")
                .build();
        accountFlowMapper.insert(flow);

        // 记录交易
        Transaction tx = Transaction.builder()
                .transactionNo(BizNoGenerator.generateTransactionNo())
                .fromAccount("ADMIN_RECHARGE")
                .toAccount(request.getAccountNo())
                .amount(request.getAmount())
                .transactionType(3) // 充值
                .status(1)
                .remark(request.getRemark() != null ? request.getRemark() : "管理员充值")
                .createdAt(LocalDateTime.now())
                .build();
        transactionMapper.insert(tx);

        log.info("管理员充值: accountNo={} amount={}", request.getAccountNo(), request.getAmount());

        User user = userMapper.selectById(account.getUserId());
        return convertAccountToDTO(account, user);
    }

    @Override
    public List<TransactionDTO> getAllTransactions(int page, int size) {
        return transactionMapper.selectList(
                        new LambdaQueryWrapper<Transaction>()
                                .orderByDesc(Transaction::getCreatedAt)
                                .last("LIMIT " + (page * size) + "," + size))
                .stream()
                .map(t -> TransactionDTO.builder()
                        .id(t.getId())
                        .transactionNo(t.getTransactionNo())
                        .fromAccount(t.getFromAccount())
                        .toAccount(t.getToAccount())
                        .amount(t.getAmount())
                        .transactionType(t.getTransactionType())
                        .status(t.getStatus())
                        .remark(t.getRemark())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanDTO> getAllLoans() {
        return loanMapper.selectList(
                        new LambdaQueryWrapper<Loan>().orderByDesc(Loan::getApplyDate))
                .stream()
                .map(l -> {
                    User user = userMapper.selectById(l.getUserId());
                    Account account = accountMapper.selectById(l.getAccountId());
                    String statusText = switch (l.getStatus()) {
                        case 0 -> "已拒绝";
                        case 1 -> "审核中";
                        case 2 -> "已批准";
                        case 3 -> "还款中";
                        case 4 -> "已结清";
                        case 5 -> "已逾期";
                        default -> "未知";
                    };
                    return LoanDTO.builder()
                            .id(l.getId())
                            .loanNo(l.getLoanNo())
                            .userId(l.getUserId())
                            .username(user != null ? user.getUsername() : null)
                            .accountId(l.getAccountId())
                            .accountNo(account != null ? account.getAccountNo() : null)
                            .loanAmount(l.getLoanAmount())
                            .approvedAmount(l.getApprovedAmount())
                            .interestRate(l.getInterestRate())
                            .loanTerm(l.getLoanTerm())
                            .monthlyPayment(l.getMonthlyPayment())
                            .totalRepay(l.getTotalRepay())
                            .repaidAmount(l.getRepaidAmount())
                            .remainingAmount(l.getTotalRepay().subtract(l.getRepaidAmount()))
                            .remainingPeriods(l.getRemainingPeriods())
                            .status(l.getStatus())
                            .statusText(statusText)
                            .applyDate(l.getApplyDate())
                            .approveDate(l.getApproveDate())
                            .startDate(l.getStartDate())
                            .dueDate(l.getDueDate())
                            .remark(l.getRemark())
                            .createdAt(l.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long getTotalUsers() {
        return userMapper.selectCount(null);
    }

    @Override
    public Long getTotalAccounts() {
        return accountMapper.selectCount(null);
    }

    @Override
    public BigDecimal getTotalBalance() {
        return accountMapper.selectList(null).stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private UserDTO convertUserToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AccountDTO convertAccountToDTO(Account account, User user) {
        return AccountDTO.builder()
                .id(account.getId())
                .accountNo(account.getAccountNo())
                .userId(account.getUserId())
                .username(user != null ? user.getUsername() : null)
                .accountType(account.getAccountType())
                .accountTypeName(account.getAccountType() == 1 ? "储蓄卡" : "信用卡")
                .balance(account.getBalance())
                .frozenAmount(account.getFrozenAmount())
                .availableBalance(account.getBalance().subtract(account.getFrozenAmount()))
                .creditLimit(account.getCreditLimit())
                .availableCredit(account.getAvailableCredit())
                .billingDay(account.getBillingDay())
                .dueDay(account.getDueDay())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
