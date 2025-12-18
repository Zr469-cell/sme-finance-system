package com.finance.erp.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_boms")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductBom extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "parent_product_id")
    private Product parentProduct;

    @ManyToOne
    @JoinColumn(name = "child_product_id")
    private Product childProduct;

    @Column(precision = 19, scale = 4)
    private BigDecimal quantity; // 用量

    @Column(precision = 5, scale = 4)
    private BigDecimal wastageRate; // 损耗率
}