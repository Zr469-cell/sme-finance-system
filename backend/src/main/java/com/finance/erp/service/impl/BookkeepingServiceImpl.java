package com.finance.erp.service.impl;

import com.finance.erp.common.exception.BusinessException;
import com.finance.erp.dto.SplitDTO;
import com.finance.erp.dto.TransactionDTO;
import com.finance.erp.entity.Split;
import com.finance.erp.entity.Transaction;
import com.finance.erp.repository.TransactionRepository;
import com.finance.erp.service.BookkeepingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookkeepingServiceImpl implements BookkeepingService {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Long createTransaction(TransactionDTO dto) {
        BigDecimal balance = BigDecimal.ZERO;
        if (dto.getSplits() == null || dto.getSplits().isEmpty()) {
            throw new BusinessException("交易分录不能为空");
        }
        for (SplitDTO splitDto : dto.getSplits()) {
            if (splitDto.getAmount() != null) {
                balance = balance.add(splitDto.getAmount());
            }
        }
        if (balance.abs().compareTo(new BigDecimal("0.0001")) > 0) {
            throw new BusinessException("交易不平衡！借贷差额: " + balance);
        }

        Transaction txn = new Transaction();
        txn.setBookId(dto.getBookId());
        txn.setPostDate(dto.getPostDate());
        txn.setEnterDate(LocalDateTime.now());
        txn.setDescription(dto.getDescription());
        
        if (dto.getEntryCode() == null || dto.getEntryCode().trim().isEmpty()) {
            txn.setEntryCode(generateEntryCode(dto.getBookId(), dto.getPostDate()));
        } else {
            txn.setEntryCode(dto.getEntryCode());
        }

        List<Split> splits = new ArrayList<>();
        for (SplitDTO sDto : dto.getSplits()) {
            Split split = new Split();
            split.setTransaction(txn);
            split.setAccountId(sDto.getAccountId());
            split.setContactId(sDto.getContactId());
            split.setAmount(sDto.getAmount());
            split.setMemo(sDto.getMemo());
            split.setReconcileState('n');
            splits.add(split);
        }
        txn.setSplits(splits);

        return transactionRepository.save(txn).getId();
    }

    @Override
    @Transactional
    public Long voidTransaction(long originalTransactionId) { // Long -> long
        Transaction original = transactionRepository.findById(originalTransactionId)
                .orElseThrow(() -> new BusinessException("凭证不存在"));

        Transaction voidTxn = new Transaction();
        voidTxn.setBookId(original.getBookId());
        voidTxn.setPostDate(LocalDate.now());
        voidTxn.setEnterDate(LocalDateTime.now());
        voidTxn.setDescription("红字冲销: " + original.getDescription());
        voidTxn.setEntryCode("V-" + (original.getEntryCode() == null ? "" : original.getEntryCode()));

        List<Split> newSplits = new ArrayList<>();
        for (Split oldSplit : original.getSplits()) {
            Split newSplit = new Split();
            newSplit.setTransaction(voidTxn);
            newSplit.setAccountId(oldSplit.getAccountId());
            newSplit.setContactId(oldSplit.getContactId());
            newSplit.setAmount(oldSplit.getAmount().negate());
            newSplit.setMemo("冲销原分录");
            newSplit.setReconcileState('n');
            newSplits.add(newSplit);
        }
        voidTxn.setSplits(newSplits);

        return transactionRepository.save(voidTxn).getId();
    }

    @Override
    public BigDecimal getAccountBalance(long accountId) { // Long -> long
        return BigDecimal.ZERO; 
    }

    private String generateEntryCode(Long bookId, LocalDate date) {
        LocalDate start = date.withDayOfMonth(1);
        LocalDate end = date.withDayOfMonth(date.lengthOfMonth());
        String maxCode = transactionRepository.findMaxEntryCode(bookId, start, end);
        int sequence = 1;
        String prefix = date.format(DateTimeFormatter.ofPattern("yyyyMM"));
        if (maxCode != null && maxCode.contains("-")) {
            try {
                String[] parts = maxCode.split("-");
                if (parts.length >= 2 && parts[0].equals(prefix)) {
                    sequence = Integer.parseInt(parts[1]) + 1;
                }
            } catch (Exception ignored) {}
        }
        return String.format("%s-%03d", prefix, sequence);
    }
}