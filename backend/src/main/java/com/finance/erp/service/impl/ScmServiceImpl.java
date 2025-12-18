package com.finance.erp.service.impl;

import com.finance.erp.common.enums.OrderStatus;
import com.finance.erp.common.enums.OrderType;
import com.finance.erp.common.enums.ProductType;
import com.finance.erp.common.exception.BusinessException;
import com.finance.erp.dto.OrderDTO;
import com.finance.erp.dto.OrderItemDTO;
import com.finance.erp.dto.SplitDTO;
import com.finance.erp.dto.TransactionDTO;
import com.finance.erp.entity.*;
import com.finance.erp.repository.*;
import com.finance.erp.service.BookkeepingService;
import com.finance.erp.service.ScmService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ScmServiceImpl implements ScmService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final AccountRepository accountRepository;
    private final BookkeepingService bookkeepingService;
    
    // === 标准会计科目代码 (Standard Codes) ===
    // 对应 AccountController 初始化生成的科目代码
    private static final String CODE_INVENTORY = "1405"; // 库存商品
    private static final String CODE_TRANSIT = "1401";   // 在途物资
    private static final String CODE_AP = "2202";        // 应付账款
    private static final String CODE_AR = "1122";        // 应收账款
    private static final String CODE_INCOME = "6001";    // 主营业务收入

    @Override
    @Transactional
    public Long createOrder(OrderDTO dto) {
        Order order = new Order();
        order.setBookId(dto.getBookId());
        order.setContactId(dto.getContactId());
        order.setOrderType(dto.getOrderType());
        order.setOrderDate(dto.getOrderDate());
        order.setDueDate(dto.getDueDate());
        order.setStatus(OrderStatus.DRAFT);
        
        // 生成业务单号：PO-时间戳 (采购) / SO-时间戳 (销售)
        String prefix = dto.getOrderType() == OrderType.PURCHASE ? "PO-" : "SO-";
        order.setOrderNo(prefix + System.currentTimeMillis());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (dto.getItems() != null) {
            for (OrderItemDTO itemDto : dto.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                // 校验商品是否存在
                item.setProduct(productRepository.getReferenceById(
                    Objects.requireNonNull(itemDto.getProductId(), "Product ID is null")
                ));
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setRemark(itemDto.getRemark());
                
                // subtotal 是只读列，不设置，但在内存中计算用于统计 totalAmount
                BigDecimal sub = itemDto.getQuantity().multiply(itemDto.getUnitPrice());
                items.add(item);
                total = total.add(sub);
            }
        }
        order.setItems(items);
        order.setTotalAmount(total);

        return orderRepository.save(order).getId();
    }

    @Override
    @Transactional
    public void approveAndExecuteOrder(long orderId, LocalDate executeDate, boolean useTransit) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException("订单不存在"));
            
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("只有草稿状态的订单可以审核");
        }

        // 1. 根据类型执行不同的业务逻辑 (库存 + 财务)
        if (order.getOrderType() == OrderType.PURCHASE) {
            executePurchase(order, executeDate, useTransit);
        } else if (order.getOrderType() == OrderType.SALE) {
            executeSale(order, executeDate);
        }

        // 2. 更新单据状态
        order.setStatus(OrderStatus.APPROVED);
        orderRepository.save(order);
    }

    private void executePurchase(Order order, LocalDate executeDate, boolean useTransit) {
        Long bookId = order.getBookId();

        // A. 库存处理
        for (OrderItem item : order.getItems()) {
            // 记录库存变动日志
            InventoryLog log = new InventoryLog();
            log.setBookId(bookId);
            log.setProductId(item.getProduct().getId());
            log.setOrderId(order.getId());
            log.setChangeType("PURCHASE_IN");
            log.setChangeQty(item.getQuantity()); // 正数入库
            log.setCostPrice(item.getUnitPrice());
            inventoryLogRepository.save(log);

            // 更新商品当前库存快照
            Product product = item.getProduct();
            BigDecimal current = product.getCurrentStock() == null ? BigDecimal.ZERO : product.getCurrentStock();
            product.setCurrentStock(current.add(item.getQuantity()));
            productRepository.save(product);
        }

        // B. 财务处理：生成凭证
        TransactionDTO txn = new TransactionDTO();
        txn.setBookId(bookId);
        txn.setPostDate(executeDate); // 使用指定的入账日期
        txn.setDescription("采购入库单: " + order.getOrderNo());
        // 使用订单号作为凭证号的前缀或关联信息，这里让系统自动生成 entryCode
        txn.setEntryCode(null); 
        
        List<SplitDTO> splits = new ArrayList<>();
        
        // Split 1: 借 库存商品 (Asset +)
        SplitDTO dr = new SplitDTO();
        dr.setAccountId(getAccountIdByCode(bookId, CODE_INVENTORY, "库存商品(1405)")); 
        dr.setAmount(order.getTotalAmount()); 
        dr.setMemo("采购入库");
        splits.add(dr);

        // Split 2: 贷方逻辑
        SplitDTO cr = new SplitDTO();
        if (useTransit) {
            // 冲销在途物资
            // 借：库存商品 / 贷：在途物资
            cr.setAccountId(getAccountIdByCode(bookId, CODE_TRANSIT, "在途物资(1401)"));
            cr.setMemo("冲销在途物资");
        } else {
            // 标准应付账款
            // 借：库存商品 / 贷：应付账款
            cr.setAccountId(getAccountIdByCode(bookId, CODE_AP, "应付账款(2202)"));
            cr.setMemo("应付货款");
        }
        cr.setContactId(order.getContactId()); // 关联供应商
        cr.setAmount(order.getTotalAmount().negate()); // 贷方为负
        splits.add(cr);

        txn.setSplits(splits);
        bookkeepingService.createTransaction(txn);
    }

    private void executeSale(Order order, LocalDate executeDate) {
        Long bookId = order.getBookId();

        // A. 库存处理
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            
            // 校验库存是否充足 (服务类型除外)
            if (product.getType() != ProductType.SERVICE) {
                BigDecimal current = product.getCurrentStock() == null ? BigDecimal.ZERO : product.getCurrentStock();
                if (current.compareTo(item.getQuantity()) < 0) {
                    throw new BusinessException("库存不足: " + product.getName() + " (当前: " + current + ")");
                }
                // 扣减库存
                product.setCurrentStock(current.subtract(item.getQuantity()));
                productRepository.save(product);
            }

            // 记录日志
            InventoryLog log = new InventoryLog();
            log.setBookId(bookId);
            log.setProductId(product.getId());
            log.setOrderId(order.getId());
            log.setChangeType("SALE_OUT");
            log.setChangeQty(item.getQuantity().negate()); // 负数出库
            inventoryLogRepository.save(log);
        }

        // B. 财务处理：生成凭证 (借：应收账款，贷：主营业务收入)
        TransactionDTO txn = new TransactionDTO();
        txn.setBookId(bookId);
        txn.setPostDate(executeDate); // 使用指定的日期
        txn.setDescription("销售出库单: " + order.getOrderNo());
        txn.setEntryCode(null); // 让财务系统生成标准凭证号

        List<SplitDTO> splits = new ArrayList<>();
        
        // Split 1: 借 应收账款 (1122)
        SplitDTO dr = new SplitDTO();
        dr.setAccountId(getAccountIdByCode(bookId, CODE_AR, "应收账款(1122)"));
        dr.setContactId(order.getContactId()); // 关联客户
        dr.setAmount(order.getTotalAmount()); 
        dr.setMemo("销售应收");
        splits.add(dr);
        
        // Split 2: 贷 主营业务收入 (6001)
        SplitDTO cr = new SplitDTO();
        cr.setAccountId(getAccountIdByCode(bookId, CODE_INCOME, "主营业务收入(6001)"));
        cr.setAmount(order.getTotalAmount().negate());
        cr.setMemo("销售收入");
        splits.add(cr);
        
        txn.setSplits(splits);
        bookkeepingService.createTransaction(txn);
    }

    @Override
    public void completeProduction(long productionOrderId) {
        // 留空，待二期开发生产模块
    }

    /**
     * 辅助方法：根据科目代码查找 ID
     * 如果找不到，抛出友好的错误提示
     */
    private Long getAccountIdByCode(Long bookId, String code, String nameHint) {
        return accountRepository.findByBookIdAndCode(bookId, code)
                .map(Account::getId)
                .orElseThrow(() -> new BusinessException("自动记账失败: 未找到科目 [" + code + " " + nameHint + "]。请先在科目管理中初始化标准科目。"));
    }
}