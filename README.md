# SME Finance System (基于复式记账的中小企业财务系统)

> 一个融合了**资深财务智慧**与**全栈工程架构**的企业级财务管理系统。
> A professional Double-Entry Bookkeeping System for SMEs.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-green.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.0-4FC08D.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)

## 📖 项目简介 (Introduction)

本项目是一款专为中小企业设计的财务管理系统（ERP 核心模块）。区别于普通的 CRUD 系统，本项目严格遵循**《企业会计准则》**，核心基于**“有借必有贷，借贷必相等”**的复式记账逻辑设计。

系统采用前后端分离架构，致力于提供流畅的用户体验和精确的财务数据处理能力。

### 🌟 核心亮点
* **双重专业背景**：由资深全栈工程师兼资深财务人员设计，业务逻辑严密，杜绝“外行”设计的财务漏洞。
* **严谨的会计引擎**：内置科目树（Chart of Accounts）、凭证自动平衡校验、自动过账与结转。
* **高性能架构**：后端采用 Java Spring Boot 保证事务的一致性（ACID），前端采用 Vue 3 提供丝滑的交互体验。

## 🛠 技术栈 (Tech Stack)

### 后端 (Backend)
* **核心框架**：Java 17+, Spring Boot 3.x
* **数据库**：MySQL 8.0 (InnoDB引擎)
* **ORM框架**：MyBatis Plus / Spring Data JPA (根据实际选择)
* **安全鉴权**：Spring Security + JWT
* **工具库**：Lombok, Hutool, EasyExcel

### 前端 (Frontend)
* **核心框架**：Vue 3 (Composition API)
* **构建工具**：Vite
* **UI 组件库**：Element Plus / Ant Design Vue
* **状态管理**：Pinia
* **网络请求**：Axios

## 🧩 核心功能模块 (Features)

1.  **基础设置 (Settings)**
    * 会计科目管理 (支持无限层级)
    * 多币种设置
    * 期初余额初始化
2.  **账务处理 (G/L)**
    * **凭证录入**：仿 Excel 的快捷录入界面，支持自动平衡校验。
    * **凭证审核**：严谨的制单、审核分离机制。
    * **期末结转**：自动生成损益结转凭证。
3.  **财务报表 (Reports)**
    * 科目余额表 / 明细账
    * 资产负债表 (Balance Sheet) - 自动生成
    * 利润表 (Income Statement) - 自动生成

## 🚀 快速开始 (Getting Started)

### 1. 环境准备
* JDK 17+
* Node.js 16+
* MySQL 8.0

### 2. 数据库配置
1.  在 MySQL 中创建数据库 `finance_sys`。
2.  导入 `sql/init.sql` 脚本以初始化表结构和基础数据（科目表模板）。
3.  修改 `backend/src/main/resources/application.yml` 中的数据库账号密码。

### 3. 后端启动
```bash
cd backend
mvn clean install
mvn spring-boot:run
