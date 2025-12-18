package com.finance.erp.repository;

import com.finance.erp.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // 按 记账日期倒序 + ID倒序 排列
    List<Transaction> findByBookIdOrderByPostDateDescIdDesc(Long bookId);

    List<Transaction> findByBookIdAndPostDateBetweenOrderByPostDateDescIdDesc(Long bookId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT MAX(t.entryCode) FROM Transaction t WHERE t.bookId = :bookId AND t.postDate BETWEEN :startDate AND :endDate")
    String findMaxEntryCode(@Param("bookId") Long bookId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 【新增】根据账套ID删除所有凭证 (JPA会自动级联删除 splits)
    void deleteByBookId(Long bookId);
}