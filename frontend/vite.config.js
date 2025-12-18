import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 指向后端端口
        changeOrigin: true,
        // 关键修改：删除了 rewrite 行
        // 这样前端发 /api/books，后端收到的也是 /api/books，就能匹配上了
      }
    }
  }
})