package com.finance.erp.service;

import com.finance.erp.dto.OrderDTO;
import java.time.LocalDate;

/**
 * 供应链服务接口
 * 负责订单流转、库存变动
 */
public interface ScmService {

    /**
     * 创建订单
     * @param dto 订单传输对象
     * @return 订单ID
     */
    Long createOrder(OrderDTO dto);

    /**
     * 审核并执行订单 (支持指定日期和冲销选项)
     * @param orderId 订单ID
     * @param executeDate 执行/入账日期
     * @param useTransit 是否冲销在途物资 (用于跨期采购)
     */
    void approveAndExecuteOrder(long orderId, LocalDate executeDate, boolean useTransit);

    /**
     * 生产完工
     * @param productionOrderId 生产工单ID
     */
    void completeProduction(long productionOrderId);
}