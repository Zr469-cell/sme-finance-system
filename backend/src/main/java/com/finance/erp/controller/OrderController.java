package com.finance.erp.controller;

import com.finance.erp.common.Result;
import com.finance.erp.common.enums.OrderStatus;
import com.finance.erp.dto.ApproveRequest;
import com.finance.erp.dto.OrderDTO;
import com.finance.erp.entity.Order;
import com.finance.erp.repository.OrderRepository;
import com.finance.erp.service.ScmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final ScmService scmService;
    private final OrderRepository orderRepository;

    @PostMapping
    public Result<Long> createOrder(@RequestBody OrderDTO dto) {
        Long id = scmService.createOrder(dto);
        return Result.success(id);
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approveOrder(
            @PathVariable("id") long id, // Long -> long
            @RequestBody(required = false) ApproveRequest request) {
        
        LocalDate date = (request != null && request.getExecuteDate() != null) 
                ? request.getExecuteDate() 
                : LocalDate.now();
        boolean useTransit = (request != null) && request.isUseTransit();
        
        scmService.approveAndExecuteOrder(id, date, useTransit);
        return Result.success();
    }

    @GetMapping
    public Result<List<Order>> listOrders(
            @RequestParam("bookId") long bookId, // Long -> long
            @RequestParam(value = "status", required = false) OrderStatus status) {
        
        if (status != null) {
            return Result.success(orderRepository.findByBookIdAndStatus(bookId, status));
        }
        return Result.success(orderRepository.findByBookId(bookId));
    }
    
    @GetMapping("/{id}")
    public Result<Order> getOrder(@PathVariable("id") long id) { // Long -> long
        return orderRepository.findById(id)
                .map(Result::success)
                .orElse(Result.error("订单不存在"));
    }
}