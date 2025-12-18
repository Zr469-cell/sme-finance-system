import request from '@/utils/request'

// 创建往来单位 (客户/供应商/员工)
export function createContact(data) {
  return request({
    url: '/contacts',
    method: 'post',
    data
  })
}

// 查询列表
export function getContactList(params) {
  // params 包含: bookId, type (可选)
  return request({
    url: '/contacts',
    method: 'get',
    params
  })
}

// 删除单位
export function deleteContact(id) {
  return request({
    url: `/contacts/${id}`,
    method: 'delete'
  })
}