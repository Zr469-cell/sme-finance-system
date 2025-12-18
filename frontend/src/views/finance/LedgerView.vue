<template>
  <div class="ledger-container">
    <div class="header">
      <div class="left">
        <el-button @click="router.back()">返回</el-button>
        <h2 class="title">
          {{ isJournal ? '序时账 (General Journal)' : '分类账簿 (General Ledger)' }}
        </h2>
      </div>
      <div class="right">
        <!-- 【新增】搜索框：支持搜凭证号、摘要 -->
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索凭证号 / 摘要" 
          :prefix-icon="Search"
          style="width: 250px; margin-right: 10px"
          clearable
        />
        <el-button type="success" :icon="EditPen" @click="openTransactionDialog">记一笔</el-button>
      </div>
    </div>

    <el-table 
      :data="filteredTransactions" 
      border 
      stripe 
      height="calc(100vh - 140px)"
      v-loading="loading"
      :default-sort="{ prop: 'entryCode', order: 'descending' }"
    >
      <el-table-column prop="postDate" label="日期" width="110" sortable />
      
      <!-- 【优化】凭证号列：加粗显示，支持排序 -->
      <el-table-column prop="entryCode" label="凭证号" width="130" sortable>
        <template #default="{ row }">
          <el-tag effect="plain" type="info" size="small" class="code-tag">
            {{ row.entryCode || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="摘要 / 科目信息" min-width="300">
        <template #default="{ row }">
          <div class="desc-cell">
            <span class="main-desc">{{ row.description }}</span>
            <div v-if="isJournal" class="sub-info">
              <!-- 显示科目名称 -->
              <span class="account-badge">
                {{ getAccountName(row.mySplit.accountId) }}
              </span>
              <span class="memo" v-if="row.mySplit.memo">({{ row.mySplit.memo }})</span>
            </div>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column label="借方 (Debit)" width="150" align="right">
        <template #default="{ row }">
          <span class="money-font" :class="{ 'text-red': isNegative(row.mySplit.amount) }">
            {{ getDebit(row) }}
          </span>
        </template>
      </el-table-column>
      
      <el-table-column label="贷方 (Credit)" width="150" align="right">
        <template #default="{ row }">
          <span class="money-font" :class="{ 'text-red': isPositive(row.mySplit.amount) }">
            {{ getCredit(row) }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="余额" width="150" align="right" v-if="!isJournal">
        <template #default="{ row }">
          <span class="money-font bold">{{ formatMoney(row.balance) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-popconfirm 
            title="确定要冲销这笔凭证吗？" 
            confirm-button-type="danger"
            @confirm="handleVoid(row)"
          >
            <template #reference>
              <el-button link type="danger" size="small">冲销</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <TransactionDialog ref="txnDialogRef" :book-id="bookStore.bookId" @success="fetchData" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { EditPen, Search } from '@element-plus/icons-vue'
import { useBookStore } from '@/stores/bookStore'
import { getTransactionList, voidTransaction } from '@/api/transaction'
import { getAccountList } from '@/api/account'
import { formatMoney } from '@/utils/format'
import TransactionDialog from '@/components/finance/TransactionDialog.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const bookStore = useBookStore()

const accountId = computed(() => Number(route.params.accountId))
const isJournal = computed(() => accountId.value === 0)

const rawTransactions = ref([]) // 原始数据
const accountMap = ref({})
const loading = ref(false)
const searchKeyword = ref('') // 搜索关键词
const txnDialogRef = ref(null)

// 前端过滤逻辑
const filteredTransactions = computed(() => {
  if (!searchKeyword.value) return rawTransactions.value
  
  const kw = searchKeyword.value.toLowerCase()
  return rawTransactions.value.filter(t => 
    (t.entryCode && t.entryCode.toLowerCase().includes(kw)) || 
    (t.description && t.description.toLowerCase().includes(kw)) ||
    (t.mySplit.memo && t.mySplit.memo.toLowerCase().includes(kw))
  )
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTransactionList({ bookId: bookStore.bookId })
    
    // 加载科目名称映射
    if (isJournal.value && Object.keys(accountMap.value).length === 0) {
      const accList = await getAccountList(bookStore.bookId)
      accList.forEach(acc => accountMap.value[acc.id] = acc)
    }

    const processed = []
    let runningBalance = 0
    
    // 【核心改进】排序逻辑：优先按日期，日期相同按凭证号 (String localeCompare)
    res.sort((a, b) => {
      const dateDiff = new Date(a.postDate) - new Date(b.postDate)
      if (dateDiff !== 0) return dateDiff
      // 字符串比较：202512-001 < 202512-002
      return (a.entryCode || '').localeCompare(b.entryCode || '')
    })

    res.forEach(txn => {
      if (isJournal.value) {
        // 序时账：展开所有分录
        txn.splits.forEach(split => {
          processed.push({ ...txn, mySplit: split, balance: 0 })
        })
      } else {
        // 单科目账：计算余额
        const split = txn.splits.find(s => s.accountId === accountId.value)
        if (split) {
          runningBalance += Number(split.amount)
          processed.push({ ...txn, mySplit: split, balance: runningBalance })
        }
      }
    })
    
    // 默认倒序显示（最新的在最上面）
    rawTransactions.value = processed.reverse()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const getAccountName = (id) => {
  const acc = accountMap.value[id]
  return acc ? `${acc.code || ''} ${acc.name}` : `${id}`
}

const getDebit = (row) => {
  const amt = Number(row.mySplit.amount)
  if (amt > 0) return formatMoney(amt)
  if (amt < 0 && isRedInk(row)) return formatMoney(amt)
  return ''
}

const getCredit = (row) => {
  const amt = Number(row.mySplit.amount)
  if (amt < 0) return formatMoney(Math.abs(amt))
  if (amt > 0 && isRedInk(row)) return formatMoney(amt)
  return ''
}

const isRedInk = (row) => row.description && row.description.includes('冲销')
const isNegative = (val) => Number(val) < 0
const isPositive = (val) => Number(val) > 0

const openTransactionDialog = () => {
  const defaultAcc = isJournal.value ? null : accountId.value
  txnDialogRef.value.open(null, defaultAcc)
}

const handleVoid = async (row) => {
  try {
    await voidTransaction(row.id)
    ElMessage.success('红字冲销成功')
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.ledger-container { padding: 20px; height: 100vh; display: flex; flex-direction: column; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.header .left { display: flex; align-items: center; gap: 15px; }
.header .right { display: flex; align-items: center; }
.title { margin: 0; }
.money-font { font-family: Consolas, monospace; }
.bold { font-weight: bold; }
.text-red { color: #F56C6C; }

/* 样式优化 */
.desc-cell { display: flex; flex-direction: column; gap: 2px; }
.main-desc { font-weight: 500; color: #303133; }
.sub-info { font-size: 12px; display: flex; align-items: center; gap: 6px; margin-top: 2px; }
.account-badge { background: #f0f2f5; padding: 1px 6px; border-radius: 4px; color: #606266; }
.memo { color: #909399; }
.code-tag { font-family: monospace; letter-spacing: 0.5px; }
</style>