package com.finance.erp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference; // 引入这个
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "transactions")
@Data
@EqualsAndHashCode(callSuper = true)
public class Transaction extends BaseEntity {

    private Long bookId;

    @Column(nullable = false)
    private LocalDate postDate;

    private LocalDateTime enterDate;

    @Column(length = 1024)
    private String description;

    private String entryCode;

    // 【关键修改】父级使用 ManagedReference (管理者)
    // 意思：序列化我的时候，请把下面的 splits 也带上
    @JsonManagedReference
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Split> splits;
}