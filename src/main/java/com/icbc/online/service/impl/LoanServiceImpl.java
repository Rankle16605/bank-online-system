package com.icbc.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.icbc.online.dto.request.LoanApplyRequest;
import com.icbc.online.dto.response.LoanDTO;
import com.icbc.online.dto.response.LoanRepaymentDTO;
import com.icbc.online.exception.BusinessException;
import com.icbc.online.mapper.*;
import com.icbc.online.model.*;
import com.icbc.online.service.LoanService;
import com.icbc.online.util.BizNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanMapper loanMapper;
    private final LoanRepaymentMapper repaymentMapper;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;
    private final AccountFlowMapper accountFlowMapper;

    @Override
    @Transactional
    public LoanDTO applyLoan(String username, LoanApplyRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getAccountNo, request.getAccountNo()));
        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        if (!account.getUserId().equals(user.getId())) {
            throw new BusinessException("该账户不属于您");
        }
        if (account.getAccountType() != 2) {
            throw new BusinessException("仅信用卡账户可以申请借款");
        }
        if (account.getStatus() != 1) {
            throw new BusinessException("账户状态异常");
        }

        // 利率根据期限确定
        BigDecimal rate;
        if (request.getLoanTerm() <= 6) rate = new BigDecimal("8.000");
        else if (request.getLoanTerm() <= 12) rate = new BigDecimal("10.000");
        else if (request.getLoanTerm() <= 24) rate = new BigDecimal("12.000");
        else rate = new BigDecimal("15.000");

        // 等额本息计算
        BigDecimal monthlyRate = rate.divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        BigDecimal amount = request.getLoanAmount();
        int periods = request.getLoanTerm();
        BigDecimal monthlyPayment = calculateMonthlyPayment(amount, monthlyRate, periods);
        BigDecimal totalRepay = monthlyPayment.multiply(new BigDecimal(periods));

        String loanNo = BizNoGenerator.generateLoanNo();
        Loan loan = Loan.builder()
                .loanNo(loanNo)
                .userId(user.getId())
                .accountId(account.getId())
                .loanAmount(amount)
                .approvedAmount(amount)
                .interestRate(rate)
                .loanTerm(periods)
                .monthlyPayment(monthlyPayment)
                .totalRepay(totalRepay)
                .repaidAmount(BigDecimal.ZERO)
                .remainingPeriods(periods)
                .status(1)
                .applyDate(LocalDateTime.now())
                .remark(request.getRemark())
                .build();

        loanMapper.insert(loan);
        log.info("借款申请成功: {} 金额: {} 期限: {}月", loanNo, amount, periods);
        return convertToDTO(loan, user, account);
    }

    @Override
    public List<LoanDTO> getMyLoans(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return loanMapper.selectList(
                        new LambdaQueryWrapper<Loan>()
                                .eq(Loan::getUserId, user.getId())
                                .orderByDesc(Loan::getApplyDate))
                .stream()
                .map(l -> convertToDTO(l, user, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanDTO> getLoansByAccount(String accountNo) {
        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getAccountNo, accountNo));
        if (account == null) {
            throw new BusinessException("账户不存在");
        }
        User user = userMapper.selectById(account.getUserId());
        return loanMapper.selectList(
                        new LambdaQueryWrapper<Loan>()
                                .eq(Loan::getAccountId, account.getId())
                                .orderByDesc(Loan::getApplyDate))
                .stream()
                .map(l -> convertToDTO(l, user, account))
                .collect(Collectors.toList());
    }

    @Override
    public LoanDTO getLoanDetail(String loanNo) {
        Loan loan = loanMapper.selectOne(
                new LambdaQueryWrapper<Loan>().eq(Loan::getLoanNo, loanNo));
        if (loan == null) {
            throw new BusinessException("借款不存在");
        }
        User user = userMapper.selectById(loan.getUserId());
        Account account = accountMapper.selectById(loan.getAccountId());
        return convertToDTO(loan, user, account);
    }

    @Override
    public List<LoanRepaymentDTO> getRepayments(String loanNo) {
        Loan loan = loanMapper.selectOne(
                new LambdaQueryWrapper<Loan>().eq(Loan::getLoanNo, loanNo));
        if (loan == null) {
            throw new BusinessException("借款不存在");
        }
        return repaymentMapper.selectList(
                        new LambdaQueryWrapper<LoanRepayment>()
                                .eq(LoanRepayment::getLoanId, loan.getId())
                                .orderByAsc(LoanRepayment::getPeriodNo))
                .stream()
                .map(this::convertRepaymentToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LoanDTO approveLoan(String loanNo) {
        Loan loan = loanMapper.selectOne(
                new LambdaQueryWrapper<Loan>().eq(Loan::getLoanNo, loanNo));
        if (loan == null) {
            throw new BusinessException("借款不存在");
        }
        if (loan.getStatus() != 1) {
            throw new BusinessException("当前状态不允许审批");
        }

        loan.setStatus(2);
        loan.setApproveDate(LocalDateTime.now());
        loan.setStartDate(LocalDateTime.now());
        loan.setDueDate(LocalDateTime.now().plusMonths(loan.getLoanTerm()));
        loanMapper.updateById(loan);

        // 生成还款计划
        generateRepaymentSchedule(loan);
        log.info("借款审批通过: {}", loanNo);
        return convertToDTO(loan, null, null);
    }

    @Override
    @Transactional
    public LoanDTO rejectLoan(String loanNo, String reason) {
        Loan loan = loanMapper.selectOne(
                new LambdaQueryWrapper<Loan>().eq(Loan::getLoanNo, loanNo));
        if (loan == null) {
            throw new BusinessException("借款不存在");
        }
        if (loan.getStatus() != 1) {
            throw new BusinessException("当前状态不允许拒绝");
        }
        loan.setStatus(0);
        loan.setRemark(reason);
        loanMapper.updateById(loan);
        log.info("借款已拒绝: {}", loanNo);
        return convertToDTO(loan, null, null);
    }

    @Override
    @Transactional
    public LoanRepaymentDTO repay(Long repaymentId) {
        LoanRepayment repayment = repaymentMapper.selectById(repaymentId);
        if (repayment == null) {
            throw new BusinessException("还款记录不存在");
        }
        if (repayment.getStatus() != 0) {
            throw new BusinessException("该期已还款");
        }

        repayment.setStatus(1);
        repayment.setActualAmount(repayment.getAmount());
        repayment.setActualDate(LocalDateTime.now());
        repaymentMapper.updateById(repayment);

        // 更新借款记录
        Loan loan = loanMapper.selectById(repayment.getLoanId());
        if (loan == null) {
            throw new BusinessException("借款不存在");
        }
        loan.setRepaidAmount(loan.getRepaidAmount().add(repayment.getAmount()));
        loan.setRemainingPeriods(Math.max(0, loan.getRemainingPeriods() - 1));

        long unpaidCount = repaymentMapper.selectCount(
                new LambdaQueryWrapper<LoanRepayment>()
                        .eq(LoanRepayment::getLoanId, loan.getId())
                        .eq(LoanRepayment::getStatus, 0));
        if (unpaidCount == 0) {
            loan.setStatus(4); // 已结清
        } else if (loan.getStatus() != 3) {
            loan.setStatus(3); // 还款中
        }
        loanMapper.updateById(loan);

        log.info("还款成功: repaymentId={} amount={}", repaymentId, repayment.getAmount());
        return convertRepaymentToDTO(repayment);
    }

    private void generateRepaymentSchedule(Loan loan) {
        List<LoanRepayment> schedule = new ArrayList<>();
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        BigDecimal remaining = loan.getApprovedAmount();

        for (int i = 1; i <= loan.getLoanTerm(); i++) {
            BigDecimal interest = remaining.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principal = loan.getMonthlyPayment().subtract(interest);
            if (i == loan.getLoanTerm()) {
                principal = remaining;
            }
            remaining = remaining.subtract(principal);

            LocalDateTime scheduledDate = loan.getStartDate() != null
                    ? loan.getStartDate().plusMonths(i)
                    : LocalDateTime.now().plusMonths(i);

            LoanRepayment rp = LoanRepayment.builder()
                    .repaymentNo(BizNoGenerator.generateRepaymentNo())
                    .loanId(loan.getId())
                    .periodNo(i)
                    .amount(loan.getMonthlyPayment())
                    .principal(principal)
                    .interest(interest)
                    .actualAmount(BigDecimal.ZERO)
                    .scheduledDate(scheduledDate)
                    .status(0)
                    .build();
            schedule.add(rp);
        }
        // MyBatis-Plus 逐条插入
        for (LoanRepayment rp : schedule) {
            repaymentMapper.insert(rp);
        }
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int periods) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(new BigDecimal(periods), 2, RoundingMode.HALF_UP);
        }
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal pow = onePlusR.pow(periods);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(pow);
        BigDecimal denominator = pow.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private LoanDTO convertToDTO(Loan loan, User user, Account account) {
        String statusText = switch (loan.getStatus()) {
            case 0 -> "已拒绝";
            case 1 -> "审核中";
            case 2 -> "已批准";
            case 3 -> "还款中";
            case 4 -> "已结清";
            case 5 -> "已逾期";
            default -> "未知";
        };

        return LoanDTO.builder()
                .id(loan.getId())
                .loanNo(loan.getLoanNo())
                .userId(loan.getUserId())
                .username(user != null ? user.getUsername() : null)
                .accountId(loan.getAccountId())
                .accountNo(account != null ? account.getAccountNo() : null)
                .loanAmount(loan.getLoanAmount())
                .approvedAmount(loan.getApprovedAmount())
                .interestRate(loan.getInterestRate())
                .loanTerm(loan.getLoanTerm())
                .monthlyPayment(loan.getMonthlyPayment())
                .totalRepay(loan.getTotalRepay())
                .repaidAmount(loan.getRepaidAmount())
                .remainingAmount(loan.getTotalRepay().subtract(loan.getRepaidAmount()))
                .remainingPeriods(loan.getRemainingPeriods())
                .status(loan.getStatus())
                .statusText(statusText)
                .applyDate(loan.getApplyDate())
                .approveDate(loan.getApproveDate())
                .startDate(loan.getStartDate())
                .dueDate(loan.getDueDate())
                .remark(loan.getRemark())
                .createdAt(loan.getCreatedAt())
                .build();
    }

    private LoanRepaymentDTO convertRepaymentToDTO(LoanRepayment rp) {
        String statusText = switch (rp.getStatus()) {
            case 0 -> "待还";
            case 1 -> "已还";
            case 2 -> "逾期";
            case 3 -> "减免";
            default -> "未知";
        };
        return LoanRepaymentDTO.builder()
                .id(rp.getId())
                .repaymentNo(rp.getRepaymentNo())
                .loanId(rp.getLoanId())
                .periodNo(rp.getPeriodNo())
                .amount(rp.getAmount())
                .principal(rp.getPrincipal())
                .interest(rp.getInterest())
                .actualAmount(rp.getActualAmount())
                .scheduledDate(rp.getScheduledDate())
                .actualDate(rp.getActualDate())
                .status(rp.getStatus())
                .statusText(statusText)
                .build();
    }
}
