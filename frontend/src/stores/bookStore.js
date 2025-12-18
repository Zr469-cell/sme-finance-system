import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useBookStore = defineStore('book', () => {
  // State: 从 localStorage 初始化，防止刷新丢失
  const currentBook = ref(JSON.parse(localStorage.getItem('currentBook')) || null)

  // Getters
  const bookId = computed(() => currentBook.value?.id)
  const bookName = computed(() => currentBook.value?.name)
  const currency = computed(() => currentBook.value?.currencyCode || 'CNY')
  
  // 判断是否开启 ERP 功能
  const isFactory = computed(() => {
    const type = currentBook.value?.type
    return type === 'MANUFACTURING' || type === 'BUSINESS_SIMPLE'
  })

  // Actions
  function setBook(book) {
    currentBook.value = book
    localStorage.setItem('currentBook', JSON.stringify(book))
  }

  function clearBook() {
    currentBook.value = null
    localStorage.removeItem('currentBook')
  }

  return { 
    currentBook, 
    bookId, 
    bookName, 
    currency, 
    isFactory, 
    setBook, 
    clearBook 
  }
})