# SME Finance System (基于复式记账的中小企业财务系统)

> 一个融合了**资深财务智慧**与**全栈工程架构**的企业级财务管理系统。
> A professional Double-Entry Bookkeeping System for SMEs.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-green.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.0-4FC08D.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)

## 📖 项目简介 (Introduction)

本项目是一款专为中小企业设计的财务管理系统。本项目严格遵循《企业会计准则》**，核心基于**“有借必有贷，借贷必相等”的复式记账逻辑设计。帮助企业和个人更加便捷的实现财务管理和溯源，提供管理效率，降低管理成本。

系统采用前后端分离架构，致力于提供流畅的用户体验和精确的财务数据处理能力。

### 🌟 核心亮点
* **贴近实际的需求**：专业的会计系统上手难度高，维护困难，使用成本较大，该系统针对个人和中小微企业的实际需求开发，技能满足简单的记账需求，也能适应企业的进存销凭证生成核销和简单的财务报表生成，便于决策
* **严谨的会计引擎**：内置科目树（Chart of Accounts）、凭证自动平衡校验、自动过账与结转。
* **高性能架构**：后端采用 Java Spring Boot 保证事务的一致性（ACID），前端采用 Vue 3 提供良好的交互体验。

## 🛠 技术栈 (Tech Stack)

### 后端 (Backend)
* **核心框架**：Java 17+, Spring Boot 3.x
* **数据库**：MySQL 8.0 
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
    * 会计科目管理
    * <img width="2556" height="728" alt="image" src="https://github.com/user-attachments/assets/2886284b-7927-42c3-af06-d30fbec7bba7" />

    * 凭证借贷金额自动校验
    * <img width="1610" height="732" alt="image" src="https://github.com/user-attachments/assets/1b6f33f8-2c4a-41eb-a851-fb1dd53360f7" />

    * 序时账随时查询历史凭证记录
    * <img width="2113" height="391" alt="image" src="https://github.com/user-attachments/assets/41b632cf-869b-4130-a8a4-2101da6b92a5" />

    * 红字冲销法删除错误凭证
    * <img width="2157" height="570" alt="image" src="https://github.com/user-attachments/assets/cb1b0a43-05b7-469d-a574-19e85ecb4281" />

    * 企业进存销业务和凭证核销功能
    * <img width="2559" height="891" alt="image" src="https://github.com/user-attachments/assets/4344ca8e-9ed1-4f38-8722-629de32dadf8" />

    * 期末财务报表自动生成
    * <img width="1744" height="733" alt="image" src="https://github.com/user-attachments/assets/10bff86b-7513-4f84-a9fd-52dbfd14bb6d" />
    * <img width="2108" height="705" alt="image" src="https://github.com/user-attachments/assets/0a9caba2-bc29-47e3-9b0a-ca9971f49f62" />
    
    *使用手册帮助用户更快上手
    <img width="2559" height="1417" alt="image" src="https://github.com/user-attachments/assets/2c9aebc2-9c5a-4e72-8419-061d3ae3647b" />

3.  **账务处理 (G/L)**
    * **凭证录入**：支持自动平衡校验。
    * **凭证审核**：严谨的制单、审核分离机制。
    * **期末结转**：自动生成损益结转凭证。
4.  **财务报表 (Reports)**
    * 科目余额表 / 明细账
    * 资产负债表  - 自动生成
    * 利润表 - 自动生成

## 🚀 快速开始 (Getting Started)
    * 浏览器访问 打开您的网址：👉 http://47.242.153.172（2026.1.14前有效）
    
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
