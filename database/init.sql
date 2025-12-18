-- --------------------------------------------------------
-- 0. 环境初始化
-- --------------------------------------------------------
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清理旧表 (顺序很重要)
DROP TABLE IF EXISTS inventory_logs;
DROP TABLE IF EXISTS product_boms;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS contacts;
DROP TABLE IF EXISTS scheduled_transactions;
DROP TABLE IF EXISTS splits;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

-- --------------------------------------------------------
-- 1. 基础架构 (多租户 & 财务核心)
-- --------------------------------------------------------

-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 账套表 (工厂实体)
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    currency_code VARCHAR(10) DEFAULT 'CNY',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 往来单位表 (CRM/SRM 核心)
-- 工厂必须管理: 供应商(卖原料的), 客户(买产品的), 员工(领工资的)
CREATE TABLE contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL COMMENT '公司名或人名',
    -- CUSTOMER, VENDOR, EMPLOYEE, OTHER
    type VARCHAR(20) NOT NULL,    
    tax_id VARCHAR(50) COMMENT '税号/身份证',
    phone VARCHAR(50),
    address VARCHAR(512),
    -- 辅助字段
    bank_name VARCHAR(100),
    bank_account VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='往来单位';

-- 科目表
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    account_type VARCHAR(20) NOT NULL, -- ASSET, LIABILITY...
    is_placeholder BOOLEAN DEFAULT FALSE,
    attributes JSON NULL,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES accounts(id),
    INDEX idx_book_code (book_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- 2. 财务记账引擎 (Financial Core)
-- --------------------------------------------------------

-- 交易头
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    post_date DATE NOT NULL,
    enter_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(1024) NOT NULL,
    entry_code VARCHAR(100), -- 凭证号
    FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分录表 (升级版：加入 Contact 关联)
CREATE TABLE splits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    -- 核心关联：这笔钱是哪个客户/供应商的？
    contact_id BIGINT NULL COMMENT '往来挂账 (应收/应付/预收/预付)',
    amount DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    memo VARCHAR(1024),
    reconcile_state CHAR(1) DEFAULT 'n',
    reconcile_date DATETIME NULL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (contact_id) REFERENCES contacts(id),
    INDEX idx_contact_acc (contact_id, account_id) -- 查某客户的应收账款明细
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- 3. 供应链与生产核心 (SCM & Manufacturing)
-- --------------------------------------------------------

-- 产品/物料表
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) COMMENT 'SKU/料号/条码',
    -- 工厂核心分类: RAW(原料), WIP(半成品), FINISHED(成品), SERVICE(服务)
    type VARCHAR(20) NOT NULL DEFAULT 'FINISHED', 
    unit VARCHAR(20) DEFAULT '个',
    -- 价格策略
    sale_price DECIMAL(19, 4) DEFAULT 0.0000 COMMENT '标准售价',
    purchase_cost DECIMAL(19, 4) DEFAULT 0.0000 COMMENT '参考进价',
    attributes JSON NULL COMMENT '规格/材质/图纸链接',
    FOREIGN KEY (book_id) REFERENCES books(id),
    UNIQUE KEY uk_book_sku (book_id, sku)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料主数据';

-- 物料清单 (BOM) - 工厂 ERP 的灵魂
-- 描述 "造1个父件需要多少子件"
CREATE TABLE product_boms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_product_id BIGINT NOT NULL COMMENT '成品/半成品',
    child_product_id BIGINT NOT NULL COMMENT '原料/子件',
    quantity DECIMAL(19, 4) NOT NULL COMMENT '单位用量',
    wastage_rate DECIMAL(5, 4) DEFAULT 0.0000 COMMENT '损耗率 (如 0.05 代表 5%)',
    FOREIGN KEY (parent_product_id) REFERENCES products(id),
    FOREIGN KEY (child_product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BOM结构表';

-- 业务单据 (订单)
-- 涵盖: PO(采购), SO(销售), WO(生产工单)
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    contact_id BIGINT NULL COMMENT '供应商/客户 (生产单可为空)',
    order_no VARCHAR(50) NOT NULL COMMENT '单号',
    -- PURCHASE, SALE, PRODUCTION, ADJUSTMENT
    order_type VARCHAR(20) NOT NULL, 
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT, APPROVED, COMPLETED, CANCELLED',
    order_date DATE NOT NULL,
    due_date DATE NULL COMMENT '交货/付款期限',
    total_amount DECIMAL(19, 4) DEFAULT 0.0000,
    transaction_id BIGINT NULL COMMENT '关联生成的财务凭证',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (contact_id) REFERENCES contacts(id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务单据主表';

-- 订单明细
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DECIMAL(19, 4) NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL, -- 采购价/售价/成本价
    subtotal DECIMAL(19, 4) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    remark VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- 4. 库存中心 (Inventory Hub)
-- --------------------------------------------------------

-- 库存变动日志 (实物账)
-- 所有的入库、出库、生产领料，必须记入此表
CREATE TABLE inventory_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    -- 关联业务
    order_id BIGINT NULL COMMENT '关联单据',
    -- 变动方向
    -- PURCHASE_IN (采购入库), SALE_OUT (销售出库)
    -- PRODUCTION_IN (完工入库), PRODUCTION_OUT (生产领料)
    change_type VARCHAR(30) NOT NULL,
    change_qty DECIMAL(19, 4) NOT NULL COMMENT '正数入库，负数出库',
    -- 成本核算 (简单加权平均法需要记录每次入库成本)
    cost_price DECIMAL(19, 4) DEFAULT 0.0000 COMMENT '入库时的单价成本',
    log_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水账';

-- --------------------------------------------------------
-- 5. 计划交易 (保留)
-- --------------------------------------------------------
CREATE TABLE scheduled_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    next_run_date DATE NOT NULL,
    last_run_date DATE NULL,
    enabled BOOLEAN DEFAULT TRUE,
    auto_create BOOLEAN DEFAULT FALSE,
    template_data JSON NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- --------------------------------------------------------
-- 6. 初始化示例数据 (让系统跑起来)
-- --------------------------------------------------------
-- 创建管理员
INSERT INTO users (username, password_hash, email) VALUES ('admin', 'hash_placeholder', 'admin@factory.com');