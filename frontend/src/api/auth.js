import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// [新增] 发送验证码
export function sendVerifyCode(phone) {
  return request({
    url: '/auth/send-code',
    method: 'post',
    params: { phone }
  })
}

// [新增] 重置密码
export function resetPassword(data) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data // { phone, code, newPassword }
  })
}