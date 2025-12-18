package com.finance.erp.controller;

import com.finance.erp.dto.OrderDTO;

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
     * 审核并执行订单
     * @param orderId 订单ID
     */
    void approveAndExecuteOrder(Long orderId);

    /**
     * 生产完工
     * @param productionOrderId 生产工单ID
     */
    void completeProduction(Long productionOrderId);
}