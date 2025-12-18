package com.finance.erp.controller;

import com.finance.erp.common.Result;
import com.finance.erp.entity.Product;
import com.finance.erp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull; // 1. 引入 NonNull
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @PostMapping
    public Result<Product> createProduct(@RequestBody @NonNull Product product) { // 2. 添加 @NonNull 注解
        Product saved = productRepository.save(product);
        return Result.success(saved);
    }

    @GetMapping
    public Result<List<Product>> listProducts(@RequestParam("bookId") long bookId) { // Long -> long
        List<Product> list = productRepository.findByBookId(bookId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable("id") long id) { // Long -> long
        return productRepository.findById(id)
                .map(Result::success)
                .orElse(Result.error("商品不存在"));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable("id") long id) { // Long -> long
        try {
            productRepository.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除失败，该商品已被引用");
        }
    }
}