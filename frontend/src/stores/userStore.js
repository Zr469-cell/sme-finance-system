import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // === State ===
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')) || null)
  const token = ref(localStorage.getItem('token') || '')

  // === Getters ===
  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => userInfo.value?.id || 1) // 默认 ID 1 用于测试
  const username = computed(() => userInfo.value?.username || 'Guest')

  // === Actions ===
  function login(user, authToken) {
    userInfo.value = user
    token.value = authToken
    
    localStorage.setItem('userInfo', JSON.stringify(user))
    localStorage.setItem('token', authToken)
  }

  function logout() {
    userInfo.value = null
    token.value = ''
    
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
    
    // 登出时通常也需要清除账套选择
    localStorage.removeItem('currentBook')
  }

  return { 
    userInfo, 
    token, 
    isLoggedIn, 
    userId, 
    username, 
    login, 
    logout 
  }
})