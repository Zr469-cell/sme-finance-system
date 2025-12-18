package com.finance.erp.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 订单审核请求参数
 */
@Data
public class ApproveRequest {
    // 实际执行/入账日期 (允许补录或指定日期)
    private LocalDate executeDate;
    
    // 是否使用"在途物资"科目冲销 (用于解决跨期采购问题)
    private boolean useTransit;
}