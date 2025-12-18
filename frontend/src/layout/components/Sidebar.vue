<template>
  <div class="sidebar-container">
    <div class="logo-wrapper">
      <img src="@/assets/logo.svg" alt="Logo" class="logo-img" />
      <h1 class="logo-text">简盈财务系统</h1>
    </div>

    <el-menu
      :default-active="activeMenu"
      class="el-menu-vertical"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
    >
      <!-- 1. 财务核心 -->
      <el-sub-menu index="/finance">
        <template #title>
          <el-icon><Wallet /></el-icon>
          <span>财务核算</span>
        </template>
        
        <el-menu-item index="/finance/summary">
          <el-icon><DataAnalysis /></el-icon> 财务总览（预览）
        </el-menu-item>

        <el-menu-item index="/finance/reports">
          <el-icon><TrendCharts /></el-icon> 财务报表中心
        </el-menu-item>

        <el-menu-item index="/finance/accounts">
          <el-icon><Files /></el-icon> 科目管理（创建）
        </el-menu-item>
      </el-sub-menu>

      <!-- 2. ERP 业务 (仅工厂/商贸模式可见) -->
      <el-sub-menu index="/erp" v-if="bookStore.isFactory">
        <template #title>
          <el-icon><Box /></el-icon>
          <span>进销存管理</span>
        </template>
        <el-menu-item index="/erp/orders">
          <el-icon><List /></el-icon> 订单中心
        </el-menu-item>
        <el-menu-item index="/erp/products">
          <el-icon><Goods /></el-icon> 商品/物料
        </el-menu-item>
        <el-menu-item index="/erp/contacts">
          <el-icon><User /></el-icon> 往来单位
        </el-menu-item>
      </el-sub-menu>

      <!-- 3. 系统设置 -->
      <el-sub-menu index="/settings">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </template>
        <el-menu-item index="/book-setup">
          <el-icon><Switch /></el-icon> 切换/管理账套
        </el-menu-item>
      </el-sub-menu>

      <!-- 4. 帮助与支持 -->
      <el-menu-item index="/help/manual">
        <el-icon><Reading /></el-icon>
        <span>使用手册 </span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useBookStore } from '@/stores/bookStore'
import { 
  Wallet, Files, Box, List, Goods, User, Setting, Switch, DataAnalysis, TrendCharts, Reading
} from '@element-plus/icons-vue'

const route = useRoute()
const bookStore = useBookStore()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar-container {
  height: 100%;
  background-color: #304156;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.logo-wrapper {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3649;
  padding: 0 10px;
}
.logo-img { width: 32px; height: 32px; margin-right: 10px; }
.logo-text { color: #fff; font-weight: 600; font-size: 18px; white-space: nowrap; margin: 0; }
.el-menu-vertical { border-right: none; width: 100%; }
:deep(.el-menu-item:hover) { background-color: #263445 !important; }
</style>