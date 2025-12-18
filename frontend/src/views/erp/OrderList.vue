<template>
  <div class="app-container">
    <!-- 顶部操作栏 -->
    <div class="filter-container">
      <el-button type="warning" :icon="Plus" @click="openDialog('PURCHASE')">新建采购单</el-button>
      <el-button type="success" :icon="Plus" @click="openDialog('SALE')">新建销售单</el-button>
      
      <div class="filter-right">
        <el-radio-group v-model="statusFilter" @change="fetchData">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="DRAFT">草稿</el-radio-button>
          <el-radio-button label="APPROVED">已审核</el-radio-button>
        </el-radio-group>
        <el-button :icon="Refresh" circle style="margin-left: 10px" @click="fetchData" />
      </div>
    </div>

    <!-- 订单表格 -->
    <el-table :data="list" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="orderDate" label="单据日期" width="120" sortable />
      <el-table-column prop="orderNo" label="单号" width="180" show-overflow-tooltip />
      
      <el-table-column prop="orderType" label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.orderType === 'PURCHASE' ? 'warning' : 'success'" effect="dark">
            {{ row.orderType === 'PURCHASE' ? '采购' : '销售' }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="往来单位" min-width="180">
        <template #default="{ row }">
          {{ getContactName(row.contactId) }}
        </template>
      </el-table-column>
      
      <el-table-column label="总金额" align="right" width="150">
        <template #default="{ row }">
          <span class="money-font">{{ formatMoney(row.totalAmount) }}</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag effect="plain" :type="row.status === 'APPROVED' ? 'success' : 'info'">
            {{ row.status === 'APPROVED' ? '已审核' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button 
            v-if="row.status === 'DRAFT'" 
            type="primary" 
            link 
            @click="openApproveDialog(row)"
          >
            审核执行
          </el-button>
          <span v-else class="text-gray">已归档</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/详情弹窗组件 -->
    <OrderDetailDialog ref="detailDialogRef" :book-id="bookStore.bookId" @success="fetchData" />

    <!-- 审核确认弹窗 (处理跨期与在途物资) -->
    <el-dialog v-model="approveVisible" title="审核执行订单" width="450px" destroy-on-close>
      <div class="approve-tip">
        <el-alert 
          title="审核操作将不可撤销" 
          type="warning" 
          description="系统将自动扣减/增加库存，并生成相应的财务凭证。" 
          show-icon 
          :closable="false"
        />
      </div>
      
      <el-form :model="approveForm" label-width="110px" style="margin-top: 20px">
        <el-form-item label="入库/记账日期">
          <el-date-picker 
            v-model="approveForm.executeDate" 
            type="date" 
            value-format="YYYY-MM-DD" 
            placeholder="默认为今天"
            style="width: 100%"
          />
        </el-form-item>
        
        <!-- 仅采购单显示此选项 -->
        <el-form-item label="特殊处理" v-if="currentOrder?.orderType === 'PURCHASE'">
          <el-checkbox v-model="approveForm.useTransit">
            冲销“在途物资”
            <el-tooltip content="如果该订单已在期末通过【1401 在途物资】科目入账，请勾选此项，贷方将记录为在途物资，避免重复记录应付账款。" placement="top">
              <el-icon class="help-icon"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-checkbox>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmApprove" :loading="approving">
          确认审核
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Refresh, QuestionFilled } from '@element-plus/icons-vue'
import { useBookStore } from '@/stores/bookStore'
import { getOrderList, approveOrder } from '@/api/order' // 需确保 api/order.js 支持传参
import { getContactList } from '@/api/contact'
import { formatMoney } from '@/utils/format'
import OrderDetailDialog from '@/components/erp/OrderDetailDialog.vue'
import { ElMessage } from 'element-plus'

const bookStore = useBookStore()
const list = ref([])
const loading = ref(false)
const approving = ref(false)
const statusFilter = ref('') // 默认显示全部
const detailDialogRef = ref(null)
const contactMap = ref({})

// 审核弹窗相关状态
const approveVisible = ref(false)
const currentOrder = ref(null)
const approveForm = ref({
  executeDate: '',
  useTransit: false
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = { bookId: bookStore.bookId }
    if (statusFilter.value) params.status = statusFilter.value
    // 注意：后端需要支持按 updateTime 或 id 倒序，这里假设后端已处理，或前端排序
    const res = await getOrderList(params)
    // 前端简单倒序，让最新的在最上面
    list.value = res.sort((a, b) => b.id - a.id)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 预加载往来单位名称
const loadContacts = async () => {
  try {
    const res = await getContactList({ bookId: bookStore.bookId })
    res.forEach(c => contactMap.value[c.id] = c.name)
  } catch (e) {
    console.error(e)
  }
}

const getContactName = (id) => contactMap.value[id] || `ID:${id}`

const openDialog = (type) => {
  detailDialogRef.value.open(type)
}

// 打开审核确认框
const openApproveDialog = (row) => {
  currentOrder.value = row
  // 默认日期为今天
  approveForm.value.executeDate = new Date().toISOString().split('T')[0]
  approveForm.value.useTransit = false
  approveVisible.value = true
}

// 执行审核
const confirmApprove = async () => {
  if (!currentOrder.value) return
  
  approving.value = true
  try {
    // 调用更新后的 API，传递 DTO 对象
    // 后端接收: @RequestBody ApproveRequest request
    await approveOrder(currentOrder.value.id, {
      executeDate: approveForm.value.executeDate,
      useTransit: approveForm.value.useTransit
    })
    
    ElMessage.success('审核成功，凭证已生成')
    approveVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  } finally {
    approving.value = false
  }
}

onMounted(() => {
  loadContacts()
  fetchData()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { 
  margin-bottom: 20px; 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
}
.filter-right { display: flex; align-items: center; }
.money-font { font-family: Consolas, monospace; font-weight: bold; }
.text-gray { color: #909399; font-size: 12px; }
.help-icon { margin-left: 4px; color: #909399; cursor: pointer; }
.approve-tip { margin-bottom: 10px; }
</style>