<template>
  <div class="book-setup-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>选择您的财务账套</span>
          <el-button type="primary" size="small" @click="dialogVisible = true">新建账套</el-button>
        </div>
      </template>

      <div v-if="loading" class="loading-box">加载中...</div>
      
      <div v-else-if="books.length === 0" class="empty-box">
        <el-empty description="暂无账套，请先创建" />
      </div>

      <div v-else class="book-list">
        <div 
          v-for="book in books" 
          :key="book.id" 
          class="book-item"
        >
          <!-- 点击区域 -->
          <div class="book-content" @click="handleSelectBook(book)">
            <div class="book-icon">
              <el-icon :size="32" color="#409EFF"><Wallet /></el-icon>
            </div>
            <div class="book-info">
              <h3>{{ book.name }}</h3>
              <p>
                <el-tag size="small" :type="getBookTypeTag(book.type)">{{ book.type }}</el-tag>
                <span class="currency">{{ book.currencyCode }}</span>
              </p>
            </div>
          </div>
          
          <!-- 删除按钮区 -->
          <div class="actions">
             <el-popconfirm 
                title="确定删除该账套吗？所有相关数据将不可恢复！" 
                confirm-button-type="danger"
                @confirm="handleDelete(book)"
              >
              <template #reference>
                <el-button link type="danger" :icon="Delete" class="delete-btn"></el-button>
              </template>
            </el-popconfirm>
            <el-icon class="arrow" @click="handleSelectBook(book)"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 新建账套弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建账套" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="账套名称" required>
          <el-input v-model="form.name" placeholder="例如：我的面馆 / 某某工厂" />
        </el-form-item>
        <el-form-item label="账套类型">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="个人/家庭" value="PERSONAL" />
            <el-option label="商贸店铺 (进销存)" value="BUSINESS_SIMPLE" />
            <el-option label="制造工厂 (ERP)" value="MANUFACTURING" />
          </el-select>
        </el-form-item>
        <el-form-item label="本位币">
          <el-select v-model="form.currencyCode" style="width: 100%">
            <el-option label="CNY - 人民币" value="CNY" />
            <el-option label="USD - 美元" value="USD" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="autoInit">自动生成标准会计科目树 (推荐)</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreate" :loading="createLoading">确定创建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Wallet, ArrowRight, Delete } from '@element-plus/icons-vue'
import { getMyBooks, createBook, deleteBook } from '@/api/book'
import { initializeAccounts } from '@/api/account'
import { useBookStore } from '@/stores/bookStore'
import { useUserStore } from '@/stores/userStore' // [新增] 引入用户 Store
import { ElMessage } from 'element-plus'

const router = useRouter()
const bookStore = useBookStore()
const userStore = useUserStore() // [新增] 获取当前登录用户

const books = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const createLoading = ref(false)
const autoInit = ref(true)

const form = ref({
  name: '',
  type: 'BUSINESS_SIMPLE',
  currencyCode: 'CNY',
  userId: null // [修改] 初始化为 null，创建时动态赋值
})

onMounted(() => {
  fetchBooks()
})

const fetchBooks = async () => {
  loading.value = true
  try {
    // [关键修改] 传入当前登录用户的 ID，而不是使用默认值
    const userId = userStore.userId
    if (!userId) {
      ElMessage.error('用户信息获取失败，请重新登录')
      router.push('/login')
      return
    }
    const res = await getMyBooks(userId)
    books.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if(!form.value.name) return ElMessage.warning('请输入名称')
  
  createLoading.value = true
  try {
    // [关键修改] 将账套绑定到当前登录用户
    form.value.userId = userStore.userId
    
    const newBook = await createBook(form.value)
    
    if (autoInit.value && newBook && newBook.id) {
      await initializeAccounts(newBook.id)
      ElMessage.success('账套创建并初始化成功')
    } else {
      ElMessage.success('账套创建成功')
    }

    dialogVisible.value = false
    form.value.name = ''
    fetchBooks()
  } catch (e) {
    console.error(e)
  } finally {
    createLoading.value = false
  }
}

// 删除账套逻辑
const handleDelete = async (book) => {
  try {
    await deleteBook(book.id)
    ElMessage.success('账套已删除')
    if (bookStore.bookId === book.id) {
      bookStore.clearBook()
    }
    fetchBooks()
  } catch (e) {
    console.error(e)
  }
}

const handleSelectBook = (book) => {
  bookStore.setBook(book)
  ElMessage.success(`已进入账套：${book.name}`)
  router.push('/finance/accounts') 
}

const getBookTypeTag = (type) => {
  const map = {
    'PERSONAL': 'info',
    'BUSINESS_SIMPLE': 'warning',
    'MANUFACTURING': 'danger'
  }
  return map[type] || ''
}
</script>

<style scoped>
.book-setup-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%239C92AC' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}
.box-card {
  width: 500px;
  border-radius: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}
.book-list {
  margin-top: 10px;
  max-height: 400px;
  overflow-y: auto;
}
.book-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 12px;
  transition: all 0.2s;
  background-color: #fff;
}
.book-item:hover {
  background-color: #ecf5ff;
  border-color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}
.book-content {
  display: flex;
  align-items: center;
  flex: 1;
  cursor: pointer;
}
.book-icon {
  margin-right: 15px;
  background-color: #ecf5ff;
  padding: 8px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.book-info h3 {
  margin: 0 0 6px 0;
  font-size: 16px;
  color: #303133;
}
.currency {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
  background-color: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
}
.actions {
  display: flex;
  align-items: center;
}
.delete-btn {
  margin-right: 10px;
  padding: 0 5px;
}
.arrow {
  cursor: pointer;
}
.loading-box, .empty-box {
  padding: 40px 0;
  text-align: center;
  color: #909399;
}
</style>