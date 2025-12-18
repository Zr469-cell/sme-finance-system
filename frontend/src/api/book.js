import request from '@/utils/request'

// 获取当前用户的账套列表
export function getMyBooks(userId = 1) {
  return request({
    url: '/books',
    method: 'get',
    params: { userId } // GET 请求参数放在 params 中
  })
}

// 创建新账套
export function createBook(data) {
  return request({
    url: '/books',
    method: 'post',
    data // POST 请求体放在 data 中
  })
}

// 获取单个账套详情
export function getBook(id) {
  return request({
    url: `/books/${id}`,
    method: 'get'
  })
}

// 【新增】删除账套
export function deleteBook(id) {
  return request({
    url: `/books/${id}`,
    method: 'delete'
  })
}