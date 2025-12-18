package com.finance.erp.entity;

import com.finance.erp.common.enums.ContactType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contacts")
@Data
@EqualsAndHashCode(callSuper = true)
public class Contact extends BaseEntity {

    private Long bookId;

    private String name;

    @Enumerated(EnumType.STRING)
    private ContactType type; // CUSTOMER, VENDOR...

    private String taxId;
    private String phone;
    private String address;
    private String bankName;
    private String bankAccount;
}