package com.finance.erp.service;

import com.finance.erp.dto.TransactionDTO;
import java.math.BigDecimal;

public interface BookkeepingService {

    Long createTransaction(TransactionDTO dto);

    // Long -> long
    Long voidTransaction(long originalTransactionId);

    // Long -> long
    BigDecimal getAccountBalance(long accountId);
}