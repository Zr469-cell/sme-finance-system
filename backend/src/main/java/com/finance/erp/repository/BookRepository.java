package com.finance.erp.repository;

import com.finance.erp.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // 查询某用户的所有账套
    List<Book> findByUserId(Long userId);
}