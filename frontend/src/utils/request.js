import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 对应 vite.config.js 中的 proxy
  timeout: 15000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

// === 请求拦截器 ===
service.interceptors.request.use(
  config => {
    // 从 localStorage 获取 Token
    const token = localStorage.getItem('token')
    if (token) {
      // 这里的 Header Key 要跟后端 Security 配置一致，通常是 Authorization
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// === 响应拦截器 ===
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 后端约定的成功状态码是 200
    if (res.code === 200) {
      return res.data // 直接返回核心数据，剥离 code 和 msg
    } else {
      // 业务逻辑错误 (如余额不足、参数校验失败)
      ElMessage.error(res.msg || '系统业务异常')
      
      // 401 未登录处理
      if (res.code === 401) {
        localStorage.clear()
        window.location.href = '/' // 或者跳转到 /login
      }
      return Promise.reject(new Error(res.msg || 'Error'))
    }
  },
  error => {
    console.error('Response Error:', error)
    let message = error.message || '请求失败'
    
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 400: message = '请求参数错误'; break
        case 401: message = '未授权，请登录'; break
        case 403: message = '拒绝访问'; break
        case 404: message = '请求地址出错'; break
        case 500: message = '服务器内部错误'; break
        default: message = `连接错误 ${status}`
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时'
    } else if (error.message.includes('Network Error')) {
      message = '网络连接失败'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 关键修复：必须默认导出 service 实例
export default service