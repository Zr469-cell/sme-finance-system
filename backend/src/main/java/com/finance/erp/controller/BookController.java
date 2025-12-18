package com.finance.erp.controller;

import com.finance.erp.common.Result;
import com.finance.erp.entity.Book;
import com.finance.erp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final ProductRepository productRepository; // 必须注入这个
    private final ContactRepository contactRepository;
    private final AccountRepository accountRepository;

    @PostMapping
    public Result<Book> createBook(@RequestBody Book book) {
        if (book.getName() == null || book.getName().trim().isEmpty()) {
            return Result.error("账套名称不能为空");
        }
        if (book.getUserId() == null) {
            book.setUserId(1L);
        }
        Book saved = bookRepository.save(book);
        return Result.success(saved);
    }

    @GetMapping
    public Result<List<Book>> listMyBooks(@RequestParam(value = "userId", defaultValue = "1") long userId) {
        List<Book> books = bookRepository.findByUserId(userId);
        return Result.success(books);
    }

    @GetMapping("/{id}")
    public Result<Book> getBook(@PathVariable("id") long id) {
        return bookRepository.findById(id)
                .map(Result::success)
                .orElse(Result.error("账套不存在"));
    }

    /**
     * 删除账套 (修复版)
     * 手动清理所有关联数据，解决外键冲突
     */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteBook(@PathVariable("id") long id) {
        try {
            // 1. 清理库存日志 (引用了 Order 和 Product，必须最先删)
            inventoryLogRepository.deleteByBookId(id);

            // 2. 清理订单 (引用了 Product, Contact) -> 级联删除 order_items
            orderRepository.deleteByBookId(id);

            // 3. 清理凭证 (引用了 Account, Contact) -> 级联删除 splits
            transactionRepository.deleteByBookId(id);

            // 4. 【关键修复】清理商品 (Product)
            // 必须在删除 OrderItems 和 InventoryLogs 之后，删除 Book 之前执行
            productRepository.deleteByBookId(id);

            // 5. 清理往来单位 (Contact)
            contactRepository.deleteByBookId(id);

            // 6. 清理科目 (Account)
            accountRepository.deleteByBookId(id);

            // 7. 最后删除账套本体 (Book)
            bookRepository.deleteById(id);
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}