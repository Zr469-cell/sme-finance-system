<template>
  <div class="app-container">
    <!-- 顶部操作栏 -->
    <div class="filter-container">
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增商品</el-button>
      <el-input 
        v-model="searchKeyword" 
        placeholder="搜索商品名称 / SKU / 编码" 
        style="width: 250px; margin-left: 10px;" 
        clearable
        :prefix-icon="Search"
      />
      <el-button :icon="Refresh" circle style="margin-left: 10px" @click="fetchData" />
    </div>

    <!-- 商品表格 -->
    <el-table 
      :data="filteredList" 
      border 
      stripe 
      style="width: 100%" 
      v-loading="loading"
    >
      <el-table-column prop="sku" label="SKU/编码" width="120" sortable />
      
      <el-table-column prop="name" label="商品名称" min-width="200">
        <template #default="{ row }">
          <span class="product-name">{{ row.name }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="type" label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getTypeTag(row.type)" effect="plain">{{ typeMap[row.type] || row.type }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="unit" label="单位" width="70" align="center" />

      <!-- 【新增】当前库存列 -->
      <el-table-column label="当前库存" width="120" align="right" sortable prop="currentStock">
        <template #default="{ row }">
          <span :class="getStockClass(row)">
            {{ row.currentStock || 0 }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="参考进价" align="right" width="120">
        <template #default="{ row }">{{ formatMoney(row.purchaseCost) }}</template>
      </el-table-column>

      <el-table-column label="标准售价" align="right" width="120">
        <template #default="{ row }">{{ formatMoney(row.salePrice) }}</template>
      </el-table-column>

      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit">编辑</el-button>
          <el-popconfirm title="确定删除该商品吗? 将无法恢复。" @confirm="handleDelete(row)">
            <template #reference>
              <el-button link type="danger" :icon="Delete">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑商品弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增商品/物料" width="500px" destroy-on-close>
      <el-form :model="form" ref="formRef" :rules="rules" label-width="80px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：特级面粉" />
        </el-form-item>
        
        <el-form-item label="SKU编码" prop="sku">
          <el-input v-model="form.sku" placeholder="例如：MF-001 (留空自动生成)" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" style="width: 100%">
                <el-option label="成品 (可销售)" value="FINISHED"/>
                <el-option label="原料 (采购用)" value="RAW"/>
                <el-option label="半成品" value="WIP"/>
                <el-option label="服务 (无库存)" value="SERVICE"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计量单位" prop="unit">
              <el-input v-model="form.unit" placeholder="个/箱/kg" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">价格设置</el-divider>

        <el-form-item label="参考进价">
          <el-input-number 
            v-model="form.purchaseCost" 
            :min="0" 
            :precision="2" 
            style="width: 100%" 
            controls-position="right"
          >
            <template #prefix>￥</template>
          </el-input-number>
        </el-form-item>

        <el-form-item label="标准售价">
          <el-input-number 
            v-model="form.salePrice" 
            :min="0" 
            :precision="2" 
            style="width: 100%"
            controls-position="right"
          >
            <template #prefix>￥</template>
          </el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreate" :loading="submitting">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Delete, Edit, Search, Refresh } from '@element-plus/icons-vue'
import { useBookStore } from '@/stores/bookStore'
import { getProductList, createProduct, deleteProduct } from '@/api/product'
import { formatMoney } from '@/utils/format'
import { ElMessage } from 'element-plus'

const bookStore = useBookStore()
const list = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = ref({})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const typeMap = {
  'FINISHED': '成品',
  'RAW': '原料',
  'WIP': '半成品',
  'SERVICE': '服务'
}

// 前端搜索过滤
const filteredList = computed(() => {
  if (!searchKeyword.value) return list.value
  const kw = searchKeyword.value.toLowerCase()
  return list.value.filter(item => 
    (item.name && item.name.toLowerCase().includes(kw)) || 
    (item.sku && item.sku.toLowerCase().includes(kw))
  )
})

const getTypeTag = (type) => {
  const map = { 'FINISHED': 'success', 'RAW': 'info', 'WIP': 'warning', 'SERVICE': '' }
  return map[type] || 'info'
}

const getStockClass = (row) => {
  if (row.type === 'SERVICE') return 'text-gray'
  const stock = Number(row.currentStock || 0)
  if (stock <= 0) return 'text-red bold' // 缺货警告
  return 'bold'
}

const fetchData = async () => {
  loading.value = true
  try {
    list.value = await getProductList(bookStore.bookId)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.value = { 
    bookId: bookStore.bookId, 
    type: 'FINISHED', 
    unit: '个', 
    purchaseCost: 0, 
    salePrice: 0,
    currentStock: 0 // 新建默认为0
  }
  dialogVisible.value = true
}

const submitCreate = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await createProduct(form.value)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        fetchData()
      } catch(e) {
        console.error(e)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = async (row) => {
  try {
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch(e) {
    console.error(e)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; display: flex; align-items: center; }
.product-name { font-weight: 500; color: #303133; }
.text-red { color: #F56C6C; }
.text-gray { color: #909399; }
.bold { font-weight: bold; }
</style>