package com.finance.erp.controller;

import com.finance.erp.common.Result;
import com.finance.erp.common.enums.ContactType;
import com.finance.erp.entity.Contact;
import com.finance.erp.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull; // 1. 引入 NonNull
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactRepository contactRepository;

    @PostMapping
    public Result<Contact> createContact(@RequestBody @NonNull Contact contact) { // 2. 添加 @NonNull 注解
        // 简单防重校验 (可选)
        // 实际业务中建议增加校验：同一个账套下名称不能重复
        Contact saved = contactRepository.save(contact);
        return Result.success(saved);
    }

    @GetMapping
    public Result<List<Contact>> listContacts(
            @RequestParam("bookId") long bookId,
            @RequestParam(value = "type", required = false) ContactType type,
            @RequestParam(value = "keyword", required = false) String keyword) { // 【新增】支持关键词搜索
        
        // 1. 如果有关键词，优先进行模糊搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            return Result.success(contactRepository.findByBookIdAndNameContaining(bookId, keyword));
        }

        // 2. 如果有类型筛选
        if (type != null) {
            return Result.success(contactRepository.findByBookIdAndType(bookId, type));
        }

        // 3. 默认查询全部
        return Result.success(contactRepository.findByBookId(bookId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteContact(@PathVariable("id") long id) {
        try {
            contactRepository.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除失败，该单位可能已存在业务数据");
        }
    }
}