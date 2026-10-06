package com.icbc.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.icbc.online.dto.request.TransferRequest;
import com.icbc.online.dto.response.AccountDTO;
import com.icbc.online.dto.response.TransactionDTO;
import com.icbc.online.exception.BusinessException;
import com.icbc.online.mapper.AccountMapper;
import com.icbc.online.mapper.TransactionMapper;
import com.icbc.online.mapper.UserMapper;
import com.icbc.online.model.Account;
import com.icbc.online.model.Transaction;
import com.icbc.online.model.User;
import com.icbc.online.service.TransactionService;
import com.icbc.online.util.BizNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易服务实现 (MyBatis-Plus)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionMapper transactionMapper;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;

    @Value("${app.transfer.single-limit:50000.00}")
    private BigDecimal singleLimit;

    @Value("${app.transfer.daily-limit:200000.00}")
    private BigDecimal dailyLimit;

    @Override
    @Transactional
    public TransactionDTO transfer(String username, TransferRequest request) {
        // 校验不能自己转自己
        if (request.getFromAccount().equals(request.getToAccount())) {
            throw new BusinessException("不能向自己的账户转账");
        }

        // 校验转账限额
        if (request.getAmount().compareTo(singleLimit) > 0) {
            throw new BusinessException("单笔转账金额不能超过" + singleLimit + "元");
        }

        // 获取转出账户
        Account fromAccount = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getAccountNo, request.getFromAccount()));
        if (fromAccount == null) {
            throw new BusinessException("转出账户不存在");
        }

        // 获取转入账户
        Account toAccount = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getAccountNo, request.getToAccount()));
        if (toAccount == null) {
            throw new BusinessException("转入账户不存在");
        }

        // 验证账户状态
        if (fromAccount.getStatus() != 1) {
            throw new BusinessException("转出账户状态异常");
        }
        if (toAccount.getStatus() != 1) {
            throw new BusinessException("转入账户状态异常");
        }

        // 验证余额
        BigDecimal available = fromAccount.getBalance().subtract(fromAccount.getFrozenAmount());
        if (available.compareTo(request.getAmount()) < 0) {
            throw new BusinessException("余额不足，可用余额：" + available + "元");
        }

        // 执行转账
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountMapper.updateById(fromAccount);
        accountMapper.updateById(toAccount);

        // 创建交易记录
        Transaction transaction = Transaction.builder()
                .transactionNo(BizNoGenerator.generateTransactionNo())
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .amount(request.getAmount())
                .transactionType(1) // 行内转账
                .status(1) // 成功
                .remark(request.getRemark())
                .build();

        transactionMapper.insert(transaction);
        log.info("转账成功: {} -> {}, 金额: {}", request.getFromAccount(), request.getToAccount(), request.getAmount());

        return convertToDTO(transaction);
    }

    @Override
    public List<TransactionDTO> getTransactionHistory(String accountNo, int page, int size) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<Transaction>()
                .and(w -> w.eq(Transaction::getFromAccount, accountNo).or().eq(Transaction::getToAccount, accountNo))
                .orderByDesc(Transaction::getCreatedAt);

        IPage<Transaction> transactionPage = transactionMapper.selectPage(new Page<>(page + 1, size), wrapper);

        return transactionPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionDTO getTransactionDetail(String transactionNo) {
        Transaction transaction = transactionMapper.selectOne(
                new LambdaQueryWrapper<Transaction>().eq(Transaction::getTransactionNo, transactionNo));
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }
        return convertToDTO(transaction);
    }

    private TransactionDTO convertToDTO(Transaction t) {
        return TransactionDTO.builder()
                .id(t.getId())
                .transactionNo(t.getTransactionNo())
                .fromAccount(t.getFromAccount())
                .toAccount(t.getToAccount())
                .amount(t.getAmount())
                .transactionType(t.getTransactionType())
                .transactionTypeName(getTransactionTypeName(t.getTransactionType()))
                .status(t.getStatus())
                .statusName(getStatusName(t.getStatus()))
                .remark(t.getRemark())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private String getTransactionTypeName(Integer type) {
        return switch (type != null ? type : 0) {
            case 1 -> "行内转账";
            case 2 -> "跨行转账";
            case 3 -> "充值";
            case 4 -> "提现";
            default -> "未知类型";
        };
    }

    private String getStatusName(Integer status) {
        return switch (status != null ? status : 0) {
            case 1 -> "交易成功";
            case 2 -> "处理中";
            default -> "交易失败";
        };
    }
}
