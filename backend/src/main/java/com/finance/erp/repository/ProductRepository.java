package com.finance.erp.repository;

import com.finance.erp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByBookId(Long bookId);

    Optional<Product> findByBookIdAndSku(Long bookId, String sku);

    // 【关键】必须要有这个删除方法
    void deleteByBookId(Long bookId);
}