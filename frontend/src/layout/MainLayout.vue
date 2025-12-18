<template>
  <div class="app-wrapper">
    <!-- 侧边栏容器 -->
    <div class="sidebar-container">
      <Sidebar />
    </div>

    <!-- 主体容器 -->
    <div class="main-container">
      <!-- 顶栏 -->
      <div class="header-container">
        <Navbar />
      </div>

      <!-- 内容区 -->
      <section class="app-main">
        <!-- 使用 Transition 和 KeepAlive 增强体验 -->
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </section>
    </div>
  </div>
</template>

<script setup>
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
</script>

<style scoped>
.app-wrapper {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.sidebar-container {
  width: 220px;
  height: 100%;
  background-color: #304156;
  transition: width 0.28s;
  flex-shrink: 0; /* 防止被挤压 */
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f0f2f5;
  position: relative;
}

.header-container {
  height: 60px;
  width: 100%;
  position: relative;
  z-index: 9;
}

.app-main {
  flex: 1;
  overflow-y: auto; /* 内容区独立滚动 */
  padding: 20px;
  position: relative;
}

/* 页面切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>