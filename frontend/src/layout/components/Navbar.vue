<template>
  <div class="navbar">
    <!-- 左侧：面包屑或欢迎语 -->
    <div class="left-panel">
      <div class="current-book">
        <span class="label">当前账套：</span>
        <el-tag effect="dark" :type="bookStore.isFactory ? 'warning' : 'success'">
          {{ bookStore.bookName || '未选择' }}
        </el-tag>
        <el-tag v-if="bookStore.isFactory" type="info" effect="plain" class="mode-tag">
          工厂/ERP模式
        </el-tag>
      </div>
    </div>

    <!-- 右侧：用户操作区 -->
    <div class="right-panel">
      <!-- 用户下拉菜单 -->
      <el-dropdown trigger="click">
        <div class="user-container">
          <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
          <span class="user-name">{{ userStore.username || 'Admin' }}</span>
          <el-icon class="el-icon--right"><CaretBottom /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('/book-setup')">切换账套</el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useBookStore } from '@/stores/bookStore'
import { useUserStore } from '@/stores/userStore' // [新增] 引入用户Store
import { UserFilled, CaretBottom } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const bookStore = useBookStore()
const userStore = useUserStore() // [新增]

const handleLogout = () => {
  // 1. 清除账套状态
  bookStore.clearBook()
  
  // 2. [关键修复] 清除用户登录状态 (Token)
  userStore.logout()
  
  ElMessage.success('已退出登录')
  
  // 3. [关键修复] 跳转到登录页，而不是账套页
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  height: 60px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.left-panel {
  display: flex;
  align-items: center;
}

.current-book .label {
  font-size: 14px;
  color: #606266;
  margin-right: 8px;
}

.mode-tag {
  margin-left: 8px;
  font-size: 12px;
}

.right-panel {
  display: flex;
  align-items: center;
}

.user-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 8px;
  transition: background .3s;
}
.user-container:hover {
  background: #f5f5f5;
  border-radius: 4px;
}

.user-avatar {
  background-color: #409EFF;
}

.user-name {
  margin-left: 8px;
  font-size: 14px;
  color: #303133;
}
</style>