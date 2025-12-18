package com.finance.erp.entity;

import com.finance.erp.common.enums.AccountType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "accounts")
@Data
@EqualsAndHashCode(callSuper = true)
public class Account extends BaseEntity {

    private Long bookId;
    
    private Long parentId; // 父科目ID

    private String name;
    
    private String code; // 科目代码 (1001)

    @Enumerated(EnumType.STRING)
    private AccountType accountType; // ASSET, LIABILITY...

    private boolean isPlaceholder; // 是否为分类占位符

    // 存储扩展属性，如 UI 颜色、图标等
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> attributes;
}