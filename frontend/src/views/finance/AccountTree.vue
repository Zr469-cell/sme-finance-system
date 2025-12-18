<template>
  <div class="app-container">
    <div class="header">
      <h3>科目概览 (Chart of Accounts)</h3>
      <div>
        <el-button type="primary" :icon="Plus" @click="dialogVisible = true">新建科目</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <el-table
      :data="treeData"
      row-key="id"
      border
      default-expand-all
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      @row-dblclick="openLedger"
    >
      <el-table-column prop="name" label="科目名称" min-width="300">
        <template #default="{ row }">
          <el-icon v-if="row.isPlaceholder" style="vertical-align: middle; margin-right: 5px"><Folder /></el-icon>
          <el-icon v-else style="vertical-align: middle; margin-right: 5px"><Document /></el-icon>
          <span>{{ row.name }}</span>
          <span v-if="row.code" class="code-tag">{{ row.code }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="accountType" label="类型" width="120" />
      <el-table-column label="余额" align="right" width="150">
        <template #default="{ row }">
          <span :class="{'text-negative': row.balance < 0}" class="money-font">
            {{ formatMoney(row.balance) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openLedger(row)" :disabled="row.isPlaceholder">打开账簿</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建科目弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建科目" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称" required><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="代码"><el-input v-model="form.code"/></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.accountType">
            <el-option label="资产 (ASSET)" value="ASSET"/>
            <el-option label="负债 (LIABILITY)" value="LIABILITY"/>
            <el-option label="权益 (EQUITY)" value="EQUITY"/>
            <el-option label="收入 (INCOME)" value="INCOME"/>
            <el-option label="费用 (EXPENSE)" value="EXPENSE"/>
          </el-select>
        </el-form-item>
        <el-form-item label="父科目">
          <el-tree-select 
            v-model="form.parentId" 
            :data="treeData" 
            check-strictly 
            :render-after-expand="false"
            style="width: 100%"
            placeholder="无 (作为顶级科目)"
            :props="{ label: 'name', value: 'id' }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Refresh, Folder, Document } from '@element-plus/icons-vue'
import { useBookStore } from '@/stores/bookStore'
import { getAccountList, createAccount, deleteAccount } from '@/api/account'
import { formatMoney } from '@/utils/format'
import { ElMessage } from 'element-plus'

const router = useRouter()
const bookStore = useBookStore()
const treeData = ref([])
const dialogVisible = ref(false)
const form = ref({})

// 列表转树形
const listToTree = (list) => {
  const map = {}, roots = []
  list.forEach(item => { map[item.id] = { ...item, children: [] } })
  list.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })
  return roots
}

const fetchData = async () => {
  const res = await getAccountList(bookStore.bookId)
  treeData.value = listToTree(res)
}

const submitCreate = async () => {
  form.value.bookId = bookStore.bookId
  await createAccount(form.value)
  ElMessage.success('创建成功')
  dialogVisible.value = false
  fetchData()
}

const handleDelete = async (row) => {
  try {
    await deleteAccount(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch(e) { console.error(e) }
}

const openLedger = (row) => {
  if (!row.isPlaceholder) router.push(`/finance/ledger/${row.id}`)
}

onMounted(fetchData)
</script>

<style scoped>
.app-container { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.money-font { font-family: Consolas, monospace; }
.text-negative { color: #F56C6C; }
.code-tag { color: #909399; font-size: 12px; margin-left: 5px; background: #f4f4f5; padding: 2px 4px; border-radius: 4px; }
</style>