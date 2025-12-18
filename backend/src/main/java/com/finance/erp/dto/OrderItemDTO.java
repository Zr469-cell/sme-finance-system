package com.finance.erp.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单明细行传输对象
 */
@Data
public class OrderItemDTO {
    
    private Long productId;      // 商品ID
    
    private BigDecimal quantity; // 数量
    
    private BigDecimal unitPrice; // 单价 (采购价 或 售价)
    
    private String remark;       // 备注
}