package com.finance.erp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.finance.erp.common.enums.BookType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Data
@EqualsAndHashCode(callSuper = true)
public class Book extends BaseEntity {
    
    private Long userId;
    
    // 强制指定 JSON 字段名为 "name"，确保前端传过来的 name 能注入进去
    @JsonProperty("name")
    @Column(nullable = false)
    private String name;
    
    @Column(length = 10)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookType type;

    // === 手动添加 Getter/Setter 作为 Lombok 的双重保险 ===
    // 这解决了 "The method getName() is undefined" 错误
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BookType getType() {
        return type;
    }

    public void setType(BookType type) {
        this.type = type;
    }
}