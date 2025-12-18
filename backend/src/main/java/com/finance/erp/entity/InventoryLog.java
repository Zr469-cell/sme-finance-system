package com.finance.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory_logs")
@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryLog extends BaseEntity {

    private Long bookId;
    
    private Long productId;
    
    private Long orderId; // 关联来源单据

    private String changeType; // PURCHASE_IN, SALE_OUT...

    @Column(precision = 19, scale = 4)
    private BigDecimal changeQty; // 正数入库, 负数出库

    @Column(precision = 19, scale = 4)
    private BigDecimal costPrice; // 入库时的成本价
}