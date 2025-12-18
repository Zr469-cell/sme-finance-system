package com.finance.erp.repository;

import com.finance.erp.common.enums.OrderStatus;
import com.finance.erp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByBookId(Long bookId);

    List<Order> findByBookIdAndStatus(Long bookId, OrderStatus status);

    // 【新增】根据账套ID删除所有订单 (JPA会自动级联删除 order_items)
    void deleteByBookId(Long bookId);
}