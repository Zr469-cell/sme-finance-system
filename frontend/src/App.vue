<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { ElConfigProvider } from 'element-plus'
// 引入中文语言包，解决日期控件默认英文的问题
import zhCn from 'element-plus/es/locale/lang/zh-cn'
</script>

<style>
/* 确保占满全屏，重置默认边距 */
#app {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>