package com.finance.erp.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 分录行传输对象
 */
@Data
public class SplitDTO {
    
    private Long accountId; // 科目ID
    
    private Long contactId; // (可选) 关联客户/供应商，用于辅助核算
    
    /**
     * 金额
     * 规则：正数代表借方 (Debit) 增加资产/费用
     * 负数代表贷方 (Credit) 增加负债/收入
     */
    private BigDecimal amount; 
    
    private String memo; // 行备注
}