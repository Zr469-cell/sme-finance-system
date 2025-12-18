<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button type="primary" :icon="Plus" @click="dialogVisible = true">新建单位</el-button>
    </div>

    <el-table :data="list" border stripe>
      <el-table-column prop="name" label="名称" min-width="200" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="联系电话" width="150" />
      <el-table-column prop="taxId" label="税号" width="180" />
      <el-table-column label="操作" width="100" align="center">
        <template #default="{ row }">
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增往来单位" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称" required><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type">
            <el-option label="客户" value="CUSTOMER"/>
            <el-option label="供应商" value="VENDOR"/>
            <el-option label="员工" value="EMPLOYEE"/>
          </el-select>
        </el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone"/></el-form-item>
        <el-form-item label="税号"><el-input v-model="form.taxId"/></el-form-item>
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
import { Plus } from '@element-plus/icons-vue'
import { useBookStore } from '@/stores/bookStore'
import { getContactList, createContact, deleteContact } from '@/api/contact'
import { ElMessage } from 'element-plus'

const bookStore = useBookStore()
const list = ref([])
const dialogVisible = ref(false)
const form = ref({ bookId: bookStore.bookId, type: 'CUSTOMER' })

const fetchData = async () => {
  list.value = await getContactList({ bookId: bookStore.bookId })
}

const submitCreate = async () => {
  form.value.bookId = bookStore.bookId
  await createContact(form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
  form.value = { bookId: bookStore.bookId, type: 'CUSTOMER' } // reset
}

const handleDelete = async (row) => {
  await deleteContact(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; }
</style>