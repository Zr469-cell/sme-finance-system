<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑财务凭证' : '录入财务凭证'"
    width="980px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-form :model="form" ref="formRef" label-width="80px">
      <!-- 抬头信息 -->
      <div class="header-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="记账日期" required>
              <el-date-picker 
                v-model="form.postDate" 
                type="date" 
                value-format="YYYY-MM-DD" 
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="凭证字号">
              <el-input v-model="form.entryCode" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="业务摘要" required>
              <el-input 
                v-model="form.description" 
                placeholder="例如：提现 / 支付房租 / 收到货款" 
                @keydown.enter.prevent
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- 核心：复式记账分录表 -->
      <el-table :data="form.splits" border stripe size="small" class="split-table">
        <el-table-column label="#" width="50" align="center">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>

        <el-table-column label="会计科目 (Account)" min-width="240">
          <template #default="{ row }">
            <el-select 
              v-model="row.accountId" 
              filterable 
              placeholder="请选择科目" 
              style="width: 100%"
            >
              <el-option 
                v-for="acc in accountList" 
                :key="acc.id" 
                :label="acc.code ? `[${acc.code}] ${acc.name}` : acc.name" 
                :value="acc.id" 
              />
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column label="分录摘要" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.memo" placeholder="可选" />
          </template>
        </el-table-column>

        <!-- 借方金额输入 -->
        <el-table-column label="借方金额 (Debit)" width="160" align="right">
          <template #header>
            <span style="color: #409EFF">借方金额 (Debit)</span>
          </template>
          <template #default="{ row }">
            <el-input-number 
              v-model="row.debit" 
              :min="0" 
              :precision="2" 
              :controls="false" 
              style="width: 100%"
              class="text-right-input"
              @input="onDebitChange(row)"
            />
          </template>
        </el-table-column>

        <!-- 贷方金额输入 -->
        <el-table-column label="贷方金额 (Credit)" width="160" align="right">
          <template #header>
            <span style="color: #F56C6C">贷方金额 (Credit)</span>
          </template>
          <template #default="{ row }">
            <el-input-number 
              v-model="row.credit" 
              :min="0" 
              :precision="2" 
              :controls="false" 
              style="width: 100%"
              class="text-right-input"
              @input="onCreditChange(row)"
            />
          </template>
        </el-table-column>

        <el-table-column width="60" align="center">
          <template #default="{ $index }">
            <el-button 
              type="danger" 
              link 
              :icon="Delete" 
              @click="removeSplit($index)" 
              v-if="form.splits.length > 2"
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部试算平衡栏 -->
      <div class="table-footer">
        <el-button link type="primary" :icon="Plus" @click="addSplit">添加分录</el-button>
        
        <div class="balance-check">
          <span class="label">借方总计:</span>
          <span class="amount">{{ totalDebit }}</span>
          
          <span class="divider">|</span>
          
          <span class="label">贷方总计:</span>
          <span class="amount">{{ totalCredit }}</span>
          
          <span class="divider">|</span>

          <!-- 状态显示 -->
          <span v-if="isBalanced" class="status success">
            <el-icon><CircleCheckFilled /></el-icon> 平衡
          </span>
          <span v-else class="status error">
            <el-icon><WarningFilled /></el-icon> 差额: {{ balanceDiff }}
            <el-button type="warning" link size="small" @click="autoBalance" style="margin-left: 5px">
              自动配平
            </el-button>
          </span>
        </div>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :disabled="!isBalanced" :loading="submitting">
        保存凭证
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Plus, Delete, CircleCheckFilled, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createTransaction } from '@/api/transaction'
import { getAccountList } from '@/api/account'

const props = defineProps(['bookId'])
const emit = defineEmits(['success'])

const visible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const accountList = ref([])

// 表单数据结构：注意这里是 splits (分录)，不是 items
const form = ref({
  bookId: null,
  postDate: '',
  description: '',
  entryCode: '',
  splits: [] 
})

// === 核心计算逻辑 ===
const totalDebit = computed(() => {
  return form.value.splits.reduce((sum, s) => sum + (Number(s.debit) || 0), 0).toFixed(2)
})
const totalCredit = computed(() => {
  return form.value.splits.reduce((sum, s) => sum + (Number(s.credit) || 0), 0).toFixed(2)
})
const balanceDiff = computed(() => (Number(totalDebit.value) - Number(totalCredit.value)).toFixed(2))
const isBalanced = computed(() => Number(balanceDiff.value) === 0 && Number(totalDebit.value) > 0)

// === 业务方法 ===

const open = async (txn = null, defaultAccountId = null) => {
  visible.value = true
  // 加载科目列表
  if (accountList.value.length === 0) {
    try {
      accountList.value = await getAccountList(props.bookId)
    } catch (e) {
      console.error(e)
    }
  }
  
  if (txn) {
    isEdit.value = true
    // TODO: 编辑模式回显逻辑
  } else {
    isEdit.value = false
    form.value = {
      bookId: props.bookId,
      postDate: new Date().toISOString().split('T')[0],
      description: '',
      entryCode: '',
      // 默认提供两行：一借一贷
      splits: [
        { accountId: defaultAccountId, debit: 0, credit: 0, memo: '' },
        { accountId: null, debit: 0, credit: 0, memo: '' }
      ]
    }
  }
}

// 借贷互斥：输入借方，清空贷方
const onDebitChange = (row) => {
  if (row.debit > 0) row.credit = 0
}
// 借贷互斥：输入贷方，清空借方
const onCreditChange = (row) => {
  if (row.credit > 0) row.debit = 0
}

const addSplit = () => {
  form.value.splits.push({ accountId: null, debit: 0, credit: 0, memo: '' })
}

const removeSplit = (index) => {
  form.value.splits.splice(index, 1)
}

// 自动配平逻辑：将差额填入空白行
const autoBalance = () => {
  const diff = Number(balanceDiff.value)
  if (diff === 0) return

  // 找一个没填金额的行，或者新增一行
  let targetRow = form.value.splits.find(s => !s.debit && !s.credit && !s.accountId)
  if (!targetRow) {
    addSplit()
    targetRow = form.value.splits[form.value.splits.length - 1]
  }

  if (diff > 0) {
    // 借方 > 贷方，需要在贷方补差额
    targetRow.credit = diff
    targetRow.debit = 0
  } else {
    // 贷方 > 借方，需要在借方补差额
    targetRow.debit = Math.abs(diff)
    targetRow.credit = 0
  }
}

const submit = async () => {
  if (!form.value.description) return ElMessage.warning('请填写摘要')
  if (form.value.splits.some(s => !s.accountId)) return ElMessage.warning('请选择科目')

  submitting.value = true
  try {
    // 构造后端需要的 DTO
    // 后端 splits 只有 amount 字段：借为正，贷为负
    const payload = {
      ...form.value,
      splits: form.value.splits.map(s => ({
        accountId: s.accountId,
        amount: s.debit > 0 ? s.debit : -s.credit, 
        memo: s.memo
      })).filter(s => s.amount !== 0)
    }

    await createTransaction(payload)
    ElMessage.success('凭证保存成功')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.header-section {
  background-color: #f8f9fa;
  padding: 15px 15px 0 15px;
  border-radius: 4px;
  margin-bottom: 15px;
}
.split-table {
  margin-top: 10px;
}
.text-right-input :deep(.el-input__inner) {
  text-align: right;
  font-family: monospace;
}
.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding: 12px 15px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-top: none;
}
.balance-check {
  display: flex;
  align-items: center;
  font-size: 14px;
}
.balance-check .label { color: #606266; margin-right: 5px; }
.balance-check .amount { font-weight: bold; font-family: monospace; font-size: 15px; }
.balance-check .divider { margin: 0 15px; color: #dcdfe6; }
.status.error { color: #F56C6C; font-weight: bold; display: flex; align-items: center; }
.status.success { color: #67C23A; font-weight: bold; display: flex; align-items: center; }
</style>