import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

// 布局组件
import MainLayout from '@/layout/MainLayout.vue'

// 基础页面
import Login from '@/views/Login.vue'
import BookSetup from '@/views/BookSetup.vue'

const routes = [
  // 默认重定向
  {
    path: '/',
    redirect: '/book-setup'
  },
  
  // 登录页
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  
  // 账套选择页
  {
    path: '/book-setup',
    name: 'BookSetup',
    component: BookSetup,
    meta: { title: '选择账套', requiresAuth: true }
  },
  
  // === 财务核心模块 (Finance) ===
  {
    path: '/finance',
    component: MainLayout,
    redirect: '/finance/accounts',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'accounts',
        name: 'AccountTree',
        component: () => import('@/views/finance/AccountTree.vue'),
        meta: { title: '科目概览' }
      },
      {
        path: 'summary',
        name: 'FinancialSummary',
        component: () => import('@/views/finance/FinancialSummary.vue'),
        meta: { title: '财务总览' }
      },
      {
        path: 'reports',
        name: 'ReportCenter',
        component: () => import('@/views/finance/ReportCenter.vue'),
        meta: { title: '财务报表' }
      },
      {
        path: 'ledger/:accountId',
        name: 'LedgerView',
        component: () => import('@/views/finance/LedgerView.vue'),
        meta: { title: '分类账簿' }
      }
    ]
  },

  // === ERP 进销存模块 (ERP) ===
  {
    path: '/erp',
    component: MainLayout,
    redirect: '/erp/orders',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/erp/OrderList.vue'),
        meta: { title: '订单中心' }
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/erp/ProductList.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'contacts',
        name: 'ContactList',
        component: () => import('@/views/erp/ContactList.vue'),
        meta: { title: '往来单位' }
      }
    ]
  },

  // === 【新增】帮助与支持模块 ===
  {
    path: '/help',
    component: MainLayout,
    redirect: '/help/manual',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'manual',
        name: 'HelpManual',
        // 路由懒加载：只有点击时才下载该页面的代码
        component: () => import('@/views/help/HelpManual.vue'),
        meta: { title: '使用手册' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// === 全局路由守卫 ===
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置浏览器标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - Finance ERP`
  }

  // 登录检查
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/book-setup')
  } else {
    next()
  }
})

export default router