<template>
  <div class="login-wrapper">
    <!-- 背景装饰：抽象的数据流线条，增加高级感 -->
    <div class="bg-graphic"></div>
    <div class="bg-overlay"></div>

    <div class="login-box">
      <!-- 1. 顶部 Logo 与标题 -->
      <div class="header">
        <div class="logo-container">
          <img src="@/assets/logo.svg" alt="Logo" class="logo" />
        </div>
        <!-- 使用更具商务感的标题 -->
        <h1 class="title">简盈财务系统</h1>
        <p class="subtitle">洞察数据 · 驱动增长</p>
      </div>

      <!-- 2. 核心表单区域 -->
      <el-form 
        :model="form" 
        ref="formRef" 
        :rules="rules" 
        size="large" 
        class="login-form"
        hide-required-asterisk
      >
        <!-- 用户名/手机号 -->
        <el-form-item prop="username">
          <div class="input-label">账号 ID</div>
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名或手机号" 
            class="custom-input"
          />
        </el-form-item>
        
        <!-- 密码 -->
        <el-form-item prop="password">
          <div class="input-label-row">
            <span>访问密码</span>
            <span class="forgot-btn" @click="openForgotDialog" v-if="!isRegister">忘记密码?</span>
          </div>
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
            class="custom-input"
            @keydown.enter="handleSubmit"
          />
        </el-form-item>

        <!-- 确认密码 (仅注册) -->
        <el-form-item prop="confirmPassword" v-if="isRegister">
          <div class="input-label">确认密码</div>
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请再次输入密码" 
            show-password
            class="custom-input"
          />
        </el-form-item>

        <!-- 邮箱 (仅注册) -->
        <el-form-item prop="email" v-if="isRegister">
          <div class="input-label">工作邮箱 (选填)</div>
          <el-input 
            v-model="form.email" 
            placeholder="name@company.com" 
            class="custom-input"
          />
        </el-form-item>

        <!-- 登录/注册按钮 -->
        <el-button 
          type="primary" 
          class="submit-btn" 
          :loading="loading" 
          @click="handleSubmit"
        >
          {{ isRegister ? '立即注册' : '登 录' }}
        </el-button>
      </el-form>

      <!-- 3. 底部切换区 -->
      <div class="footer">
        <span class="divider"></span>
        <div class="switch-area">
          <span class="text">{{ isRegister ? '已有账号?' : '新用户?' }}</span>
          <span class="link-btn" @click="toggleMode">
            {{ isRegister ? '直接登录' : '创建账号' }}
          </span>
        </div>
      </div>
    </div>

    <!-- 找回密码弹窗 -->
    <el-dialog v-model="forgotVisible" title="安全重置" width="420px" align-center class="custom-dialog">
      <el-form :model="resetForm" ref="resetRef" :rules="resetRules" label-position="top">
        <el-form-item label="验证手机" prop="phone">
          <el-input v-model="resetForm.phone" placeholder="请输入注册手机号" />
        </el-form-item>
        
        <el-form-item label="验证码" prop="code">
          <div class="verify-row">
            <el-input v-model="resetForm.code" placeholder="6位数字" />
            <el-button type="primary" plain class="send-btn" :disabled="timer > 0" @click="sendCode">
              {{ timer > 0 ? `${timer}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="resetForm.newPassword" 
            type="password" 
            placeholder="设置新的访问密码" 
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-actions">
          <el-button @click="forgotVisible = false" class="dialog-btn">取消</el-button>
          <el-button type="primary" @click="handleResetPassword" :loading="resetLoading" class="dialog-btn primary">
            提交重置
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { ElMessage } from 'element-plus'
import { login, register, sendVerifyCode, resetPassword } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

// 状态控制
const loading = ref(false)
const isRegister = ref(false)
const formRef = ref(null)

// 表单数据
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: ''
})

// 校验规则
const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = computed(() => {
  const baseRules = {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
  if (isRegister.value) {
    baseRules.confirmPassword = [{ validator: validatePass2, trigger: 'blur', required: true }]
  }
  return baseRules
})

// 切换模式
const toggleMode = () => {
  isRegister.value = !isRegister.value
  formRef.value?.resetFields()
}

// 提交逻辑
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (isRegister.value) {
          await register(form)
          ElMessage.success('注册成功，正在为您登录...')
          await doLogin()
        } else {
          await doLogin()
        }
      } catch (e) {
        console.error(e)
      } finally {
        loading.value = false
      }
    }
  })
}

const doLogin = async () => {
  const res = await login({ username: form.username, password: form.password })
  userStore.login(res, res.token)
  ElMessage.success(`欢迎回来，${res.username}`)
  router.push('/book-setup')
}

// === 找回密码 ===
const forgotVisible = ref(false)
const resetLoading = ref(false)
const timer = ref(0)
const resetRef = ref(null)
const resetForm = reactive({ phone: '', code: '', newPassword: '' })

const resetRules = {
  phone: [{ required: true, message: '必填', trigger: 'blur' }],
  code: [{ required: true, message: '必填', trigger: 'blur' }],
  newPassword: [{ required: true, message: '必填', trigger: 'blur' }]
}

const openForgotDialog = () => {
  forgotVisible.value = true
  resetForm.phone = ''
  resetForm.code = ''
  resetForm.newPassword = ''
}

const sendCode = async () => {
  if (!resetForm.phone) return ElMessage.warning('请先输入手机号')
  try {
    await sendVerifyCode(resetForm.phone)
    ElMessage.success('验证码已发送: 123456')
    timer.value = 60
    const interval = setInterval(() => {
      timer.value--
      if (timer.value <= 0) clearInterval(interval)
    }, 1000)
  } catch (e) { console.error(e) }
}

const handleResetPassword = async () => {
  if (!resetRef.value) return
  await resetRef.value.validate(async (valid) => {
    if (valid) {
      resetLoading.value = true
      try {
        await resetPassword(resetForm)
        ElMessage.success('重置成功，请登录')
        forgotVisible.value = false
      } catch (e) { console.error(e) } finally { resetLoading.value = false }
    }
  })
}
</script>

<style scoped>
/* 1. 沉浸式高级背景 */
.login-wrapper {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  /* 麦肯锡风格深蓝底色 */
  background-color: #051c2c; 
  overflow: hidden;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
}

/* 2. 背景装饰：抽象数据流线条 */
.bg-graphic {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 10% 20%, rgba(0, 100, 200, 0.2) 0%, transparent 40%),
    radial-gradient(circle at 90% 80%, rgba(64, 158, 255, 0.1) 0%, transparent 40%);
  z-index: 1;
}

/* 3. 登录卡片：悬浮感白色面板 */
.login-box {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  padding: 50px 40px;
  background: #ffffff;
  border-radius: 8px; /* 商务风格不需要太大圆角 */
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3); /* 深邃阴影 */
  transition: transform 0.3s ease;
}

.login-box:hover {
  transform: translateY(-2px);
}

/* 头部 */
.header {
  text-align: center;
  margin-bottom: 40px;
}
.logo-container {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
}
.logo {
  width: 100%;
  height: 100%;
}
.title {
  font-family: "Georgia", "Times New Roman", serif; /* 标题使用衬线体，体现权威感 */
  font-size: 28px;
  font-weight: bold;
  color: #051c2c; /* 深蓝字 */
  margin: 0 0 8px;
  letter-spacing: 0.5px;
}
.subtitle {
  font-size: 14px;
  color: #8c9fa9;
  margin: 0;
  letter-spacing: 1px;
  text-transform: uppercase; /* 大写，增加高级感 */
}

/* 表单元素优化 */
.login-form {
  margin-bottom: 30px;
}
.input-label {
  font-size: 13px;
  font-weight: 600;
  color: #334e68;
  margin-bottom: 6px;
  line-height: 1;
}
.input-label-row {
  display: flex;
  justify-content: space-between;
  width: 100%;
  font-size: 13px;
  font-weight: 600;
  color: #334e68;
  margin-bottom: 6px;
}
.forgot-btn {
  color: #0056b3;
  cursor: pointer;
  font-weight: 400;
  font-size: 12px;
}
.forgot-btn:hover {
  text-decoration: underline;
}

/* 输入框微调：更加锐利 */
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #d9e2ec inset;
  padding: 10px 15px;
  border-radius: 4px;
  background-color: #f8fafc;
  transition: all 0.3s;
}
:deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 1px #0056b3 inset !important; /* 聚焦变成深蓝 */
}
:deep(.el-input__inner) {
  height: 24px;
  font-size: 15px;
  color: #102a43;
}

/* 按钮：深沉普鲁士蓝 */
.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 4px;
  margin-top: 15px;
  background-color: #004080; /* 麦肯锡风格深蓝 */
  border: none;
  letter-spacing: 1px;
  transition: background-color 0.3s;
}
.submit-btn:hover {
  background-color: #003366;
}

/* 底部区域 */
.footer {
  text-align: center;
}
.divider {
  display: block;
  height: 1px;
  background: #f0f4f8;
  margin-bottom: 20px;
}
.switch-area {
  font-size: 14px;
  color: #627d98;
}
.link-btn {
  color: #0056b3;
  cursor: pointer;
  margin-left: 5px;
  font-weight: 600;
}
.link-btn:hover {
  color: #003366;
  text-decoration: underline;
}

/* 弹窗样式 */
.verify-row {
  display: flex;
  gap: 12px;
}
.send-btn {
  width: 110px;
  color: #0056b3;
  border-color: #0056b3;
  background-color: transparent;
}
.send-btn:hover {
  background-color: #f0f7ff;
}
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.dialog-btn {
  padding: 10px 24px;
  height: 38px;
  border-radius: 4px;
}
.dialog-btn.primary {
  background-color: #004080;
  border-color: #004080;
}
</style>