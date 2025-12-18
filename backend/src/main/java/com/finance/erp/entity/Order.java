package com.finance.erp.entity;

import com.finance.erp.common.enums.OrderStatus;
import com.finance.erp.common.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {

    private Long bookId;
    
    private Long contactId; // 供应商/客户

    private String orderNo; // 单号

    @Enumerated(EnumType.STRING)
    private OrderType orderType; // PURCHASE, SALE...

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // DRAFT, APPROVED...

    private LocalDate orderDate;
    
    private LocalDate dueDate;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalAmount;

    // 关联生成的财务凭证
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    @ToString.Exclude
    private Transaction transaction;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;
}