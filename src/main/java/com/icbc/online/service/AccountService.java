package com.icbc.online.service;

import com.icbc.online.dto.request.TransferRequest;
import com.icbc.online.dto.response.AccountDTO;

import java.util.List;

/**
 * 账户服务接口
 */
public interface AccountService {

    AccountDTO createAccount(String username, Integer accountType);

    List<AccountDTO> getAccounts(String username);

    AccountDTO getAccountDetail(String accountNo);

    AccountDTO getAccountBalance(String accountNo);
}
