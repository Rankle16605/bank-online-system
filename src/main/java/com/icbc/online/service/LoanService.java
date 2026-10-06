package com.icbc.online.service;

import com.icbc.online.dto.request.LoanApplyRequest;
import com.icbc.online.dto.response.LoanDTO;
import com.icbc.online.dto.response.LoanRepaymentDTO;

import java.util.List;

public interface LoanService {

    LoanDTO applyLoan(String username, LoanApplyRequest request);

    List<LoanDTO> getMyLoans(String username);

    List<LoanDTO> getLoansByAccount(String accountNo);

    LoanDTO getLoanDetail(String loanNo);

    List<LoanRepaymentDTO> getRepayments(String loanNo);

    LoanDTO approveLoan(String loanNo);

    LoanDTO rejectLoan(String loanNo, String reason);

    LoanRepaymentDTO repay(Long repaymentId);
}
