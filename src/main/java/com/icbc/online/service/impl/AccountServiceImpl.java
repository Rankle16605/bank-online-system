package com.icbc.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.icbc.online.dto.response.AccountDTO;
import com.icbc.online.exception.BusinessException;
import com.icbc.online.mapper.AccountMapper;
import com.icbc.online.mapper.UserMapper;
import com.icbc.online.model.Account;
import com.icbc.online.model.User;
import com.icbc.online.service.AccountService;
import com.icbc.online.util.BizNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账户服务实现 (MyBatis-Plus)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AccountDTO createAccount(String username, Integer accountType) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String accountNo = BizNoGenerator.generateAccountNo();

        Account account = Account.builder()
                .accountNo(accountNo)
                .userId(user.getId())
                .accountType(accountType != null ? accountType : 1)
                .balance(BigDecimal.ZERO)
                .frozenAmount(BigDecimal.ZERO)
                .status(1)
                .build();

        accountMapper.insert(account);
        log.info("账户创建成功: {}", account.getAccountNo());

        return convertToDTO(account);
    }

    @Override
    public List<AccountDTO> getAccounts(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return accountMapper.selectList(
                        new LambdaQueryWrapper<Account>().eq(Account::getUserId, user.getId()))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDTO getAccountDetail(String accountNo) {
        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getAccountNo, accountNo));
        if (account == null) {
            throw new BusinessException("账户不存在");
        }
        return convertToDTO(account);
    }

    @Override
    public AccountDTO getAccountBalance(String accountNo) {
        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getAccountNo, accountNo));
        if (account == null) {
            throw new BusinessException("账户不存在");
        }
        return convertToDTO(account);
    }

    private AccountDTO convertToDTO(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .accountNo(account.getAccountNo())
                .userId(account.getUserId())
                .accountType(account.getAccountType())
                .accountTypeName(getAccountTypeName(account.getAccountType()))
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

    private String getAccountTypeName(Integer type) {
        return switch (type != null ? type : 1) {
            case 1 -> "储蓄账户";
            case 2 -> "信用账户";
            default -> "未知类型";
        };
    }
}
