<template>
  <div class="summary-container">
    <div class="header-actions">
      <h2>财务总览 / 试算平衡表</h2>
      <div class="right">
        <!-- 【新增】序时账入口 (审计/溯源专用) -->
        <el-tooltip content="查看所有历史凭证流水，用于期末核算与审计溯源" placement="top">
          <el-button type="warning" plain @click="openJournal" style="margin-right: 12px">
            <el-icon style="margin-right: 5px"><List /></el-icon> 序时账 (总流水)
          </el-button>
        </el-tooltip>
        
        <el-button @click="fetchData" :icon="Refresh" :loading="loading">刷新数据</el-button>
      </div>
    </div>

    <!-- 核心：树形余额表 -->
    <el-table
      v-loading="loading"
      :data="treeData"
      row-key="id"
      border
      default-expand-all
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      class="account-table"
      @row-click="handleRowClick"
    >
      <el-table-column prop="name" label="科目名称" min-width="300">
        <template #default="{ row }">
          <span :class="{'root-node': !row.parentId}">
            <el-icon v-if="!row.parentId" class="icon"><OfficeBuilding /></el-icon>
            <el-icon v-else-if="row.isPlaceholder" class="icon"><Folder /></el-icon>
            <el-icon v-else class="icon"><Document /></el-icon>
            
            {{ row.name }}
            <span class="code" v-if="row.code">({{ row.code }})</span>
          </span>
        </template>
      </el-table-column>

      <el-table-column prop="accountType" label="类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="getTypeTag(row.accountType)">{{ row.accountType }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="借方发生额 (Debit)" width="160" align="right">
        <template #default="{ row }">
          <span class="money-font text-gray">{{ formatMoney(row.totalDebit) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="贷方发生额 (Credit)" width="160" align="right">
        <template #default="{ row }">
          <span class="money-font text-gray">{{ formatMoney(Math.abs(row.totalCredit)) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="期末余额 (Balance)" width="180" align="right">
        <template #default="{ row }">
          <span :class="getBalanceClass(row)" class="money-font bold">
            {{ formatDisplayBalance(row) }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button 
            v-if="!row.isPlaceholder" 
            link 
            type="primary" 
            @click.stop="openLedger(row)"
          >
            查看明细
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, Document, Folder, OfficeBuilding, List } from '@element-plus/icons-vue'
import { useBookStore } from '@/stores/bookStore'
import { getAccountList } from '@/api/account'
import { getTransactionList } from '@/api/transaction'
import { formatMoney } from '@/utils/format'

const router = useRouter()
const bookStore = useBookStore()
const loading = ref(false)
const treeData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const bookId = bookStore.bookId
    
    // 1. 并行获取科目表和所有凭证数据
    const [accounts, transactions] = await Promise.all([
      getAccountList(bookId),
      getTransactionList({ bookId })
    ])

    // 2. 初始化科目映射结构
    const accountMap = {}
    accounts.forEach(acc => {
      accountMap[acc.id] = { ...acc, children: [], totalDebit: 0, totalCredit: 0, balance: 0 }
    })

    // 3. 遍历凭证计算发生额和余额
    transactions.forEach(txn => {
      txn.splits.forEach(split => {
        const acc = accountMap[split.accountId]
        if (acc) {
          const amt = Number(split.amount)
          if (amt > 0) acc.totalDebit += amt
          else acc.totalCredit += amt
          acc.balance += amt
        }
      })
    })

    // 4. 构建树形结构
    const roots = []
    accounts.forEach(acc => {
      const node = accountMap[acc.id]
      if (node.parentId && accountMap[node.parentId]) {
        accountMap[node.parentId].children.push(node)
      } else {
        roots.push(node)
      }
    })

    // 5. 递归汇总：将子科目的金额向上累加到父科目
    const calculateRecursive = (node) => {
      if (node.children && node.children.length > 0) {
        node.children.forEach(child => {
          calculateRecursive(child)
          node.totalDebit += child.totalDebit
          node.totalCredit += child.totalCredit
          node.balance += child.balance
        })
      }
    }
    roots.forEach(root => calculateRecursive(root))

    // 6. 排序：按科目代码排序
    const sortTree = (nodes) => {
      nodes.sort((a, b) => (a.code || '').localeCompare(b.code || ''))
      nodes.forEach(n => { if (n.children) sortTree(n.children) })
    }
    sortTree(roots)

    treeData.value = roots

  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const formatDisplayBalance = (row) => {
  const type = row.accountType
  const val = row.balance
  // 负债、权益、收入通常是贷方余额(负数)，显示时取反符合阅读习惯
  if (['LIABILITY', 'EQUITY', 'INCOME'].includes(type)) {
    return formatMoney(val * -1) 
  }
  return formatMoney(val)
}

const getBalanceClass = (row) => {
  const type = row.accountType
  // 异常余额显示红色 (如资产变成负数)
  if (['ASSET', 'EXPENSE'].includes(type) && row.balance < 0) return 'text-negative'
  if (['LIABILITY', 'EQUITY', 'INCOME'].includes(type) && row.balance > 0) return 'text-negative'
  return 'text-normal'
}

const getTypeTag = (type) => {
  const map = { 'ASSET': '', 'LIABILITY': 'warning', 'EQUITY': 'danger', 'INCOME': 'success', 'EXPENSE': 'info' }
  return map[type] || 'info'
}

// 打开单个科目的分类账
const openLedger = (row) => {
  router.push(`/finance/ledger/${row.id}`)
}

// 打开序时账 (General Journal) - 查看所有凭证
const openJournal = () => {
  // 路由约定：ID 为 0 代表查看全量序时账
  router.push(`/finance/ledger/0`)
}

const handleRowClick = (row) => {
  // 点击行可扩展逻辑
}

onMounted(fetchData)
</script>

<style scoped>
.summary-container { padding: 20px; height: 100vh; display: flex; flex-direction: column; }
.header-actions { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.money-font { font-family: Consolas, monospace; }
.text-gray { color: #909399; font-size: 12px; }
.bold { font-weight: bold; color: #303133; }
.text-negative { color: #F56C6C; }
.icon { margin-right: 5px; vertical-align: middle; }
.code { color: #909399; font-size: 12px; margin-left: 5px; }
.root-node { font-weight: bold; font-size: 15px; }
</style>