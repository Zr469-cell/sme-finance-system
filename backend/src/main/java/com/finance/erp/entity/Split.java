package com.finance.erp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference; // 引入这个
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "splits")
@Data
@EqualsAndHashCode(callSuper = true)
public class Split extends BaseEntity {

    // 【关键修改】子级使用 BackReference (反向引用)
    // 意思：序列化我的时候，不要去序列化在这个字段里的 "transaction"，防止死循环
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    @ToString.Exclude
    private Transaction transaction;

    private Long accountId;
    
    private Long contactId; 

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 1024)
    private String memo;

    private Character reconcileState;
    
    private LocalDateTime reconcileDate;
}