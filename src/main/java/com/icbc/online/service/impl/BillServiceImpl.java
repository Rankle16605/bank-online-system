package com.icbc.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.icbc.online.dto.response.BillDTO;
import com.icbc.online.exception.BusinessException;
import com.icbc.online.mapper.AccountMapper;
import com.icbc.online.mapper.BillMapper;
import com.icbc.online.mapper.UserMapper;
import com.icbc.online.model.Account;
import com.icbc.online.model.Bill;
import com.icbc.online.model.User;
import com.icbc.online.service.BillService;
import com.icbc.online.util.BizNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账单服务实现 (MyBatis-Plus)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillMapper billMapper;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void generateMonthlyBill(Long userId, Long accountId, String billMonth) {
        Account account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        // 检查是否已生成
        Bill existBill = billMapper.selectOne(new LambdaQueryWrapper<Bill>()
                .eq(Bill::getUserId, userId)
                .eq(Bill::getAccountId, accountId)
                .eq(Bill::getBillMonth, billMonth));
        if (existBill != null) {
            throw new BusinessException("该月账单已生成");
        }

        Bill bill = Bill.builder()
                .billNo(BizNoGenerator.generateBillNo(userId, billMonth, accountId))
                .userId(userId)
                .accountId(accountId)
                .billMonth(billMonth)
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(BigDecimal.ZERO)
                .beginBalance(account.getBalance())
                .endBalance(account.getBalance())
                .status(1) // 已出账
                .build();

        billMapper.insert(bill);
        log.info("月度账单生成成功: userId={}, accountId={}, month={}", userId, accountId, billMonth);
    }

    @Override
    public List<BillDTO> getBills(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return billMapper.selectList(
                        new LambdaQueryWrapper<Bill>()
                                .eq(Bill::getUserId, user.getId())
                                .orderByDesc(Bill::getCreatedAt))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BillDTO getBillDetail(Long billId) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        return convertToDTO(bill);
    }

    private BillDTO convertToDTO(Bill bill) {
        return BillDTO.builder()
                .id(bill.getId())
                .billNo(bill.getBillNo())
                .userId(bill.getUserId())
                .accountId(bill.getAccountId())
                .billMonth(bill.getBillMonth())
                .totalIncome(bill.getTotalIncome())
                .totalExpense(bill.getTotalExpense())
                .beginBalance(bill.getBeginBalance())
                .endBalance(bill.getEndBalance())
                .status(bill.getStatus())
                .statusName(getStatusName(bill.getStatus()))
                .generatedAt(bill.getGeneratedAt())
                .createdAt(bill.getCreatedAt())
                .build();
    }

    private String getStatusName(Integer status) {
        return switch (status != null ? status : 0) {
            case 0 -> "未出账";
            case 1 -> "已出账";
            case 2 -> "已发送";
            default -> "未知";
        };
    }
}
