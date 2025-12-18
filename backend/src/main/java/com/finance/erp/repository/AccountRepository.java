package com.finance.erp.repository;

import com.finance.erp.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    // 查询账套下的所有科目
    List<Account> findByBookId(Long bookId);
    
    // 根据科目代码查找
    Optional<Account> findByBookIdAndCode(Long bookId, String code);

    /**
     * 【新增】根据账套ID删除所有科目
     * Spring Data JPA 会自动生成 DELETE 语句
     */
    void deleteByBookId(Long bookId);
}