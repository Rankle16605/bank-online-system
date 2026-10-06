package com.icbc.online.service;

import com.icbc.online.dto.request.TransferRequest;
import com.icbc.online.dto.response.AccountDTO;
import com.icbc.online.dto.response.TransactionDTO;

import java.util.List;

/**
 * 交易服务接口
 */
public interface TransactionService {

    TransactionDTO transfer(String username, TransferRequest request);

    List<TransactionDTO> getTransactionHistory(String accountNo, int page, int size);

    TransactionDTO getTransactionDetail(String transactionNo);
}
