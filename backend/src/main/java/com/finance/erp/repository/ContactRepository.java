package com.finance.erp.repository;

import com.finance.erp.common.enums.ContactType;
import com.finance.erp.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    List<Contact> findByBookId(Long bookId);

    List<Contact> findByBookIdAndType(Long bookId, ContactType type);
    
    List<Contact> findByBookIdAndNameContaining(Long bookId, String name);

    /**
     * 【新增】根据账套ID删除所有往来单位
     * Spring Data JPA 会自动根据方法名生成类似 "DELETE FROM Contact WHERE bookId = ?1" 的 SQL
     */
    void deleteByBookId(Long bookId);
}