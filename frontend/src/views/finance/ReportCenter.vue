<template>
  <div class="report-container">
    <div class="filter-header">
      <h2>财务报表中心</h2>
      <div class="controls">
        <el-date-picker
          v-model="dateRange"
          type="monthrange"
          range-separator="至"
          start-placeholder="开始月份"
          end-placeholder="结束月份"
          value-format="YYYY-MM-DD"
          @change="calculateReports"
          :clearable="false"
        />
        <el-button type="primary" :icon="Printer" @click="printReport" style="margin-left: 10px">打印/导出</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card" class="report-tabs">
      <!-- 1. 资产负债表 -->
      <el-tab-pane label="资产负债表 (Balance Sheet)" name="balance">
        <div class="report-paper" id="print-area-balance">
          <div class="report-title">
            <h3>资产负债表</h3>
            <p>截止日期：{{ endDateDisplay }}</p>
            <p>单位：人民币 (元)</p>
          </div>
          
          <el-row :gutter="40">
            <!-- 左侧：资产 -->
            <el-col :span="12">
              <table class="report-table">
                <thead><tr><th>资产 (Assets)</th><th class="text-right">金额</th></tr></thead>
                <tbody>
                  <tr v-for="item in assetRows" :key="item.id" :class="{ 'total-row': !item.parentId }">
                    <td :style="{ paddingLeft: item.level * 15 + 'px' }">{{ item.name }}</td>
                    <td class="text-right">{{ formatMoney(item.balance) }}</td>
                  </tr>
                  <tr class="grand-total">
                    <td>资产总计</td>
                    <td class="text-right">{{ formatMoney(totalAssets) }}</td>
                  </tr>
                </tbody>
              </table>
            </el-col>

            <!-- 右侧：负债及权益 -->
            <el-col :span="12">
              <table class="report-table">
                <thead><tr><th>负债及所有者权益 (Liabilities & Equity)</th><th class="text-right">金额</th></tr></thead>
                <tbody>
                  <!-- 负债部分 -->
                  <tr v-for="item in liabilityRows" :key="item.id" :class="{ 'total-row': !item.parentId }">
                    <td :style="{ paddingLeft: item.level * 15 + 'px' }">{{ item.name }}</td>
                    <td class="text-right">{{ formatMoney(Math.abs(item.balance)) }}</td>
                  </tr>
                  <tr class="sub-total">
                    <td>负债合计</td>
                    <td class="text-right">{{ formatMoney(Math.abs(totalLiabilities)) }}</td>
                  </tr>

                  <!-- 权益部分 -->
                  <tr v-for="item in equityRows" :key="item.id" :class="{ 'total-row': !item.parentId }">
                    <td :style="{ paddingLeft: item.level * 15 + 'px' }">{{ item.name }}</td>
                    <td class="text-right">{{ formatMoney(Math.abs(item.balance)) }}</td>
                  </tr>
                  <!-- 动态计算：本期净利润 -->
                  <tr class="highlight-row">
                    <td>未分配利润 (本期净利)</td>
                    <td class="text-right">{{ formatMoney(Math.abs(netIncome)) }}</td>
                  </tr>
                  <tr class="sub-total">
                    <td>所有者权益合计</td>
                    <td class="text-right">{{ formatMoney(Math.abs(totalEquity + netIncome)) }}</td>
                  </tr>

                  <tr class="grand-total">
                    <td>负债及权益总计</td>
                    <td class="text-right">{{ formatMoney(Math.abs(totalLiabilities + totalEquity + netIncome)) }}</td>
                  </tr>
                </tbody>
              </table>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>

      <!-- 2. 利润表 -->
      <el-tab-pane label="利润表 (Income Statement)" name="income">
        <div class="report-paper" id="print-area-income">
          <div class="report-title">
            <h3>利润表</h3>
            <p>会计期间：{{ startDateDisplay }} 至 {{ endDateDisplay }}</p>
            <p>单位：人民币 (元)</p>
          </div>

          <table class="report-table single-column">
            <thead><tr><th>项目</th><th class="text-right">金额</th></tr></thead>
            <tbody>
              <tr class="section-header"><td>一、营业收入</td><td class="text-right">{{ formatMoney(Math.abs(totalIncome)) }}</td></tr>
              <tr v-for="item in incomeRows" :key="item.id">
                <td class="indent">{{ item.name }}</td>
                <td class="text-right">{{ formatMoney(Math.abs(item.balance)) }}</td>
              </tr>

              <tr class="section-header"><td>减：营业成本与费用</td><td class="text-right">{{ formatMoney(totalExpenses) }}</td></tr>
              <tr v-for="item in expenseRows" :key="item.id">
                <td class="indent">{{ item.name }}</td>
                <td class="text-right">{{ formatMoney(item.balance) }}</td>
              </tr>

              <tr class="grand-total result">
                <td>三、净利润 (Net Income)</td>
                <td class="text-right">{{ formatMoney(netIncome) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Printer, Refresh } from '@element-plus/icons-vue'
import { useBookStore } from '@/stores/bookStore'
import { getAccountList } from '@/api/account'
import { getTransactionList } from '@/api/transaction'
import { formatMoney } from '@/utils/format'
import { ElMessage } from 'element-plus'

const bookStore = useBookStore()
const activeTab = ref('balance')

// 默认日期：本月
const now = new Date()
const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0]
const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0]
const dateRange = ref([startOfMonth, endOfMonth])

const startDateDisplay = computed(() => dateRange.value[0])
const endDateDisplay = computed(() => dateRange.value[1])

// 报表数据
const assetRows = ref([])
const liabilityRows = ref([])
const equityRows = ref([])
const incomeRows = ref([])
const expenseRows = ref([])

const totalAssets = ref(0)
const totalLiabilities = ref(0)
const totalEquity = ref(0)
const totalIncome = ref(0)
const totalExpenses = ref(0)
const netIncome = ref(0)

const calculateReports = async () => {
  const loading = ElMessage.info('正在生成报表...')
  try {
    const bookId = bookStore.bookId
    // 1. 获取基础数据
    const [accounts, transactions] = await Promise.all([
      getAccountList(bookId),
      getTransactionList({ bookId }) // 获取全量流水，前端根据日期过滤
    ])

    // 2. 初始化计算容器
    const accMap = {}
    accounts.forEach(acc => {
      accMap[acc.id] = { ...acc, balance: 0, children: [] }
    })

    // 3. 过滤并计算余额
    const start = new Date(dateRange.value[0])
    const end = new Date(dateRange.value[1])
    // 修正：结束日期应该是当天的23:59:59
    end.setHours(23, 59, 59, 999)

    transactions.forEach(txn => {
      const txnDate = new Date(txn.postDate)
      
      // 逻辑：
      // 资产负债表：是"时点数"，统计截止到 End Date 的所有历史余额
      // 利润表：是"时期数"，只统计 Start ~ End 之间的发生额
      
      if (txnDate <= end) {
        txn.splits.forEach(split => {
          const acc = accMap[split.accountId]
          if (!acc) return

          const amt = Number(split.amount)
          
          // 对于损益类(收入/费用)，只有在区间内的才计入利润表
          if (['INCOME', 'EXPENSE'].includes(acc.accountType)) {
            if (txnDate >= start) {
              acc.balance += amt
            }
          } else {
            // 对于资产负债类，统计历史累计余额
            acc.balance += amt
          }
        })
      }
    })

    // 4. 分类汇总 (扁平转树形简化版，只取有余额的)
    const assets = [], liabilities = [], equity = [], income = [], expenses = []
    
    Object.values(accMap).forEach(acc => {
      // 排除占位符根节点，只展示有余额的具体科目，或者展示二级汇总
      // 这里为了报表简洁，我们只展示二级科目及以下的汇总
      if (!acc.isPlaceholder && Math.abs(acc.balance) > 0.001) {
        if (acc.accountType === 'ASSET') assets.push(acc)
        else if (acc.accountType === 'LIABILITY') liabilities.push(acc)
        else if (acc.accountType === 'EQUITY') equity.push(acc)
        else if (acc.accountType === 'INCOME') income.push(acc)
        else if (acc.accountType === 'EXPENSE') expenses.push(acc)
      }
    })

    // 5. 计算总计
    assetRows.value = assets.sort((a,b) => a.code.localeCompare(b.code))
    liabilityRows.value = liabilities.sort((a,b) => a.code.localeCompare(b.code))
    equityRows.value = equity.sort((a,b) => a.code.localeCompare(b.code))
    incomeRows.value = income.sort((a,b) => a.code.localeCompare(b.code))
    expenseRows.value = expenses.sort((a,b) => a.code.localeCompare(b.code))

    totalAssets.value = assets.reduce((sum, item) => sum + item.balance, 0)
    totalLiabilities.value = liabilities.reduce((sum, item) => sum + item.balance, 0)
    totalEquity.value = equity.reduce((sum, item) => sum + item.balance, 0)
    
    totalIncome.value = income.reduce((sum, item) => sum + item.balance, 0) // 通常是负数
    totalExpenses.value = expenses.reduce((sum, item) => sum + item.balance, 0) // 通常是正数

    // 净利润 = 收入(绝对值) - 费用
    // 注意数据库中收入是负数，费用是正数。 根据会计恒等式：
    // 0 = 资产 + 费用 - 负债 - 权益 - 收入
    // 净利润其实就是 (所有损益类科目的代数和 * -1) 或者 (收入绝对值 - 费用)
    netIncome.value = Math.abs(totalIncome.value) - totalExpenses.value

    loading.close()
  } catch (e) {
    console.error(e)
    loading.close()
  }
}

const printReport = () => {
  window.print()
}

onMounted(calculateReports)
</script>

<style scoped>
.report-container { padding: 20px; display: flex; flex-direction: column; height: 100vh; }
.filter-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.report-tabs { flex: 1; overflow: hidden; display: flex; flex-direction: column; }
:deep(.el-tabs__content) { overflow-y: auto; flex: 1; background: #525659; padding: 20px; }

/* 仿真A4纸效果 */
.report-paper {
  background: white;
  width: 210mm;
  min-height: 297mm;
  margin: 0 auto;
  padding: 20mm;
  box-shadow: 0 0 10px rgba(0,0,0,0.3);
}

.report-title { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 10px; }
.report-title h3 { font-size: 24px; margin: 0 0 10px 0; }
.report-title p { margin: 0; color: #666; font-size: 14px; }

.report-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.report-table th { border-bottom: 1px solid #000; padding: 8px; text-align: left; font-weight: bold; }
.report-table td { padding: 6px 8px; border-bottom: 1px dotted #ddd; }
.report-table.single-column { width: 80%; margin: 0 auto; }

.text-right { text-align: right; font-family: Consolas, monospace; }
.total-row td { font-weight: bold; border-top: 1px solid #000; border-bottom: none; }
.grand-total td { font-weight: bold; font-size: 16px; border-top: 2px solid #000; padding-top: 10px; }
.sub-total td { font-weight: bold; color: #666; font-style: italic; }
.highlight-row td { background-color: #fdf6ec; font-weight: bold; }
.section-header td { font-weight: bold; background-color: #f5f7fa; padding-top: 15px; }
.indent { padding-left: 20px !important; }

@media print {
  body * { visibility: hidden; }
  .report-paper, .report-paper * { visibility: visible; }
  .report-paper { position: absolute; left: 0; top: 0; box-shadow: none; width: 100%; }
}
</style>