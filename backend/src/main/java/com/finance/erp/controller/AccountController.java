package com.finance.erp.controller;

import com.finance.erp.common.Result;
import com.finance.erp.common.enums.AccountType;
import com.finance.erp.entity.Account;
import com.finance.erp.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 科目管理接口
 * 负责会计科目树的增删改查及初始化
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;

    /**
     * 获取指定账套下的所有科目
     * 前端拿到此列表后，应根据 parentId 组装成树形结构
     */
    @GetMapping
    public Result<List<Account>> listAccounts(@RequestParam("bookId") Long bookId) {
        List<Account> accounts = accountRepository.findByBookId(bookId);
        return Result.success(accounts);
    }

    /**
     * 【核心升级】一键初始化标准会计科目体系 (GnuCash 风格)
     * 构建完整的 资产/负债/权益/收入/费用 树形结构及其常用子科目
     */
    @PostMapping("/initialize")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> initializeBasics(@RequestParam("bookId") Long bookId) {
        // 1. 防止重复初始化
        List<Account> existing = accountRepository.findByBookId(bookId);
        if (!existing.isEmpty()) {
            return Result.error("该账套已有科目，请勿重复初始化");
        }

        // === Level 1: 创建五大根科目 (Roots) ===
        // 资产 (Assets) - 通常以 1 开头
        Account assets = saveAccount(bookId, null, "资产 (Assets)", "1000", AccountType.ASSET, true);
        // 负债 (Liabilities) - 通常以 2 开头
        Account liabilities = saveAccount(bookId, null, "负债 (Liabilities)", "2000", AccountType.LIABILITY, true);
        // 权益 (Equity) - 通常以 4 开头 (部分准则为3)
        Account equity = saveAccount(bookId, null, "所有者权益 (Equity)", "4000", AccountType.EQUITY, true);
        // 收入 (Income) - 通常以 60 开头
        Account income = saveAccount(bookId, null, "收入 (Income)", "6000", AccountType.INCOME, true);
        // 费用 (Expenses) - 通常以 66 开头
        Account expenses = saveAccount(bookId, null, "费用 (Expenses)", "6600", AccountType.EXPENSE, true);

        // === Level 2: 创建常用子科目 (Children) ===
        
        // --- 1. 资产类 ---
        // 流动资产
        saveAccount(bookId, assets.getId(), "库存现金", "1001", AccountType.ASSET, false);
        saveAccount(bookId, assets.getId(), "银行存款", "1002", AccountType.ASSET, false);
        saveAccount(bookId, assets.getId(), "应收账款", "1122", AccountType.ASSET, false);
        saveAccount(bookId, assets.getId(), "预付账款", "1123", AccountType.ASSET, false);
        saveAccount(bookId, assets.getId(), "其他应收款", "1221", AccountType.ASSET, false);
        
        // [关键新增] 在途物资 (解决跨期采购审核报错)
        saveAccount(bookId, assets.getId(), "在途物资", "1401", AccountType.ASSET, false);
        
        // 存货
        saveAccount(bookId, assets.getId(), "原材料", "1403", AccountType.ASSET, false);
        saveAccount(bookId, assets.getId(), "库存商品", "1405", AccountType.ASSET, false);
        // 固定资产
        saveAccount(bookId, assets.getId(), "固定资产", "1601", AccountType.ASSET, false);

        // --- 2. 负债类 ---
        // 流动负债
        saveAccount(bookId, liabilities.getId(), "短期借款", "2001", AccountType.LIABILITY, false);
        saveAccount(bookId, liabilities.getId(), "应付账款", "2202", AccountType.LIABILITY, false);
        saveAccount(bookId, liabilities.getId(), "预收账款", "2203", AccountType.LIABILITY, false);
        saveAccount(bookId, liabilities.getId(), "应付职工薪酬", "2211", AccountType.LIABILITY, false);
        saveAccount(bookId, liabilities.getId(), "应交税费", "2221", AccountType.LIABILITY, false);

        // --- 3. 权益类 (GnuCash 逻辑核心) ---
        saveAccount(bookId, equity.getId(), "实收资本", "4001", AccountType.EQUITY, false);
        saveAccount(bookId, equity.getId(), "本年利润", "4103", AccountType.EQUITY, false);
        // [关键] 期初余额科目：用于配平初始资金。例如：开账时银行里有1万块，记 借：银行 1万，贷：期初余额 1万。
        saveAccount(bookId, equity.getId(), "期初余额 (Opening Balances)", "4999", AccountType.EQUITY, false); 

        // --- 4. 收入类 ---
        saveAccount(bookId, income.getId(), "主营业务收入", "6001", AccountType.INCOME, false);
        saveAccount(bookId, income.getId(), "其他业务收入", "6051", AccountType.INCOME, false);
        saveAccount(bookId, income.getId(), "投资收益", "6111", AccountType.INCOME, false);

        // --- 5. 费用类 ---
        saveAccount(bookId, expenses.getId(), "主营业务成本", "6401", AccountType.EXPENSE, false);
        saveAccount(bookId, expenses.getId(), "销售费用", "6601", AccountType.EXPENSE, false);
        saveAccount(bookId, expenses.getId(), "财务费用", "6603", AccountType.EXPENSE, false);
        
        // 管理费用及其常见明细 (为了方便，这里直接作为 expenses 的子级，或者作为管理费用的子级)
        // 这里演示建立一个"管理费用"父级，下面挂明细
        Account adminFee = saveAccount(bookId, expenses.getId(), "管理费用", "6602", AccountType.EXPENSE, true);
        
        // Level 3: 管理费用细分
        saveAccount(bookId, adminFee.getId(), "办公费", "660201", AccountType.EXPENSE, false);
        saveAccount(bookId, adminFee.getId(), "差旅费", "660202", AccountType.EXPENSE, false);
        saveAccount(bookId, adminFee.getId(), "业务招待费 (餐饮)", "660203", AccountType.EXPENSE, false);
        saveAccount(bookId, adminFee.getId(), "租金支出", "660204", AccountType.EXPENSE, false);
        saveAccount(bookId, adminFee.getId(), "水电费", "660205", AccountType.EXPENSE, false);

        return Result.success("GnuCash 标准科目体系初始化完成");
    }

    /**
     * 内部辅助方法：快速保存科目
     */
    private Account saveAccount(Long bookId, Long parentId, String name, String code, AccountType type, boolean isPlaceholder) {
        Account acc = new Account();
        acc.setBookId(bookId);
        acc.setParentId(parentId);
        acc.setName(name);
        acc.setCode(code);
        acc.setAccountType(type);
        acc.setPlaceholder(isPlaceholder); // 占位符科目通常不直接记账，只做汇总
        return accountRepository.save(acc);
    }

    /**
     * 创建科目
     * 包含自动编号逻辑
     */
    @PostMapping
    public Result<Long> createAccount(@RequestBody Account account) {
        // 简单防呆校验
        if (account.getName() == null || account.getName().trim().isEmpty()) {
            return Result.error("科目名称不能为空");
        }
        if (account.getBookId() == null) {
            return Result.error("必须指定所属账套");
        }

        // --- 自动编号逻辑 ---
        if (account.getCode() == null || account.getCode().trim().isEmpty()) {
            String newCode = autoGenerateCode(account.getBookId(), account.getParentId(), account.getAccountType());
            account.setCode(newCode);
        }
        
        Account saved = accountRepository.save(account);
        return Result.success(saved.getId());
    }

    /**
     * 更新科目信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateAccount(@PathVariable("id") Long id, @RequestBody Account account) {
        if (!accountRepository.existsById(id)) {
            return Result.error("科目不存在");
        }
        account.setId(id); // 确保 ID 一致
        accountRepository.save(account);
        return Result.success();
    }

    /**
     * 删除科目
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAccount(@PathVariable("id") Long id) {
        try {
            accountRepository.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除失败，该科目可能已包含交易数据或存在子科目");
        }
    }

    /**
     * 辅助方法：生成下一个可用科目代码
     */
    private String autoGenerateCode(Long bookId, Long parentId, AccountType type) {
        // 1. 确定前缀
        String prefix;
        if (parentId == null) {
            switch (type) {
                case ASSET: prefix = "1"; break;
                case LIABILITY: prefix = "2"; break;
                case EQUITY: prefix = "4"; break;
                case INCOME: prefix = "60"; break;
                case EXPENSE: prefix = "66"; break;
                default: prefix = "9";
            }
        } else {
            Account parent = accountRepository.findById(parentId).orElse(null);
            if (parent == null || parent.getCode() == null) {
                return String.valueOf(System.currentTimeMillis() % 10000); 
            }
            prefix = parent.getCode();
        }

        // 2. 查找同级最大后缀
        List<Account> siblings = accountRepository.findByBookId(bookId);
        long maxSuffix = 0;
        
        for (Account acc : siblings) {
            boolean isSibling = Objects.equals(acc.getParentId(), parentId);
            if (isSibling && acc.getCode() != null && acc.getCode().startsWith(prefix)) {
                String suffixStr = acc.getCode().substring(prefix.length());
                if (suffixStr.matches("\\d+")) {
                    try {
                        long suffix = Long.parseLong(suffixStr);
                        if (suffix > maxSuffix) maxSuffix = suffix;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // 3. 生成新代码
        long nextSuffix = maxSuffix + 1;
        if (parentId == null) {
            return String.format("%s%03d", prefix, nextSuffix); 
        } else {
            return String.format("%s%02d", prefix, nextSuffix);
        }
    }
}