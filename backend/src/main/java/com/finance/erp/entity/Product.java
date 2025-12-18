package com.finance.erp.entity;

import com.finance.erp.common.enums.ProductType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {

    private Long bookId;

    private String name;
    
    private String sku;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    private String unit;

    @Column(precision = 19, scale = 4)
    private BigDecimal salePrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal purchaseCost;

    /**
     * 【新增】当前实物库存数量
     * 这是一个"快照"字段，每次出入库时更新它。
     * 新建商品时默认为 0。
     */
    @Column(precision = 19, scale = 4)
    private BigDecimal currentStock = BigDecimal.ZERO; // 默认初始化为0

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> attributes;
}