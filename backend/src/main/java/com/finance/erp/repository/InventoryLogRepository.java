package com.finance.erp.repository;

import com.finance.erp.entity.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
    
    // 查询某个商品的库存变动历史
    List<InventoryLog> findByProductIdOrderByCreatedAtDesc(Long productId);

    // 查询某个订单产生的所有库存变动
    List<InventoryLog> findByOrderId(Long orderId);

    // 【新增】根据账套ID删除所有库存日志 (解决删除账套时的外键冲突)
    void deleteByBookId(Long bookId);
}