package com.icbc.online.service;

import com.icbc.online.dto.response.AdminDashboardDTO;
import com.icbc.online.dto.response.AccountDTO;
import com.icbc.online.dto.response.UserDTO;
import com.icbc.online.dto.response.LoanDTO;
import com.icbc.online.dto.response.TransactionDTO;
import com.icbc.online.dto.request.RechargeRequest;

import java.math.BigDecimal;
import java.util.List;

public interface AdminService {

    AdminDashboardDTO getDashboard();

    List<UserDTO> getAllUsers();

    List<AccountDTO> getUserAccounts(Long userId);

    List<AccountDTO> getAllAccounts();

    AccountDTO recharge(RechargeRequest request);

    List<TransactionDTO> getAllTransactions(int page, int size);

    List<LoanDTO> getAllLoans();

    Long getTotalUsers();

    Long getTotalAccounts();

    BigDecimal getTotalBalance();
}
