package com.finance.erp.dto;

import com.finance.erp.common.enums.OrderType;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 业务订单传输对象
 * 涵盖：采购单、销售单、生产工单
 */
@Data
public class OrderDTO {
    
    private Long bookId;
    
    private Long contactId;      // 供应商 或 客户 ID
    
    private OrderType orderType; // PURCHASE, SALE, PRODUCTION
    
    private LocalDate orderDate; // 下单日期
    
    private LocalDate dueDate;   // 交货/付款截止日
    
    // 订单明细列表
    private List<OrderItemDTO> items;
}