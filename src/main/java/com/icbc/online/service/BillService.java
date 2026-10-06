package com.icbc.online.service;

import com.icbc.online.dto.response.BillDTO;
import com.icbc.online.model.Bill;

import java.util.List;

/**
 * 账单服务接口
 */
public interface BillService {

    void generateMonthlyBill(Long userId, Long accountId, String billMonth);

    List<BillDTO> getBills(String username);

    BillDTO getBillDetail(Long billId);
}
