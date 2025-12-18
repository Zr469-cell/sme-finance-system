package com.finance.erp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 启用 JPA 审计功能
 * 作用：自动填充 BaseEntity 中的 @CreatedDate (创建时间) 等字段
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}