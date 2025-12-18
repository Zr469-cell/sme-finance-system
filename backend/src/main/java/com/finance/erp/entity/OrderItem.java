package com.finance.erp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    @JsonIgnore
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal unitPrice;

    /**
     * 【关键修复】
     * 数据库中定义为 GENERATED ALWAYS AS (quantity * unit_price)
     * 必须添加 insertable = false, updatable = false
     * 告诉 Hibernate：不要试图写入这个字段，它是数据库自己算的。
     */
    @Column(precision = 19, scale = 4, insertable = false, updatable = false)
    private BigDecimal subtotal;
    
    private String remark;
}