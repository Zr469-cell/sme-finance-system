package com.finance.erp.controller;

import com.finance.erp.common.Result;
import com.finance.erp.dto.TransactionDTO;
import com.finance.erp.entity.Transaction;
import com.finance.erp.repository.TransactionRepository;
import com.finance.erp.service.BookkeepingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final BookkeepingService bookkeepingService;
    private final TransactionRepository transactionRepository;

    @PostMapping
    public Result<Long> createTransaction(@RequestBody TransactionDTO dto) {
        Long id = bookkeepingService.createTransaction(dto);
        return Result.success(id);
    }

    @PostMapping("/{id}/void")
    public Result<Long> voidTransaction(@PathVariable("id") long id) { // Long -> long
        Long newId = bookkeepingService.voidTransaction(id);
        return Result.success(newId);
    }

    @GetMapping
    public Result<List<Transaction>> listTransactions(
            @RequestParam("bookId") long bookId, // Long -> long
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {
        
        if (startDate == null && endDate == null) {
            return Result.success(transactionRepository.findByBookIdOrderByPostDateDescIdDesc(bookId));
        }

        if (startDate == null) startDate = LocalDate.of(2000, 1, 1);
        if (endDate == null) endDate = LocalDate.of(2099, 12, 31);

        List<Transaction> list = transactionRepository.findByBookIdAndPostDateBetweenOrderByPostDateDescIdDesc(bookId, startDate, endDate);
        return Result.success(list);
    }
    
    @GetMapping("/{id}")
    public Result<Transaction> getTransaction(@PathVariable("id") long id) { // Long -> long
        return transactionRepository.findById(id)
                .map(Result::success)
                .orElse(Result.error("凭证不存在"));
    }
}