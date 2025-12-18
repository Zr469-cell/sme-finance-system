package com.finance.erp.dto;

import com.fasterxml.jackson.annotation.JsonFormat; // 引入注解
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 财务凭证传输对象
 */
@Data
public class TransactionDTO {
    
    private Long bookId;
    
    // 【关键修复】显式指定日期格式，防止 "Cannot deserialize..." 错误
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate postDate; 
    
    private String description;
    
    private String entryCode;
    
    private List<SplitDTO> splits;
}