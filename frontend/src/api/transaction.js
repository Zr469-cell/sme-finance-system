import request from '@/utils/request'

// 录入凭证 (核心记账)
export function createTransaction(data) {
  return request({
    url: '/transactions',
    method: 'post',
    data
  })
}

// 【新增】红字冲销凭证
export function voidTransaction(id) {
  return request({
    url: `/transactions/${id}/void`,
    method: 'post'
  })
}

// 查询凭证流水 (支持日期筛选)
export function getTransactionList(params) {
  // params 包含: bookId, startDate, endDate
  return request({
    url: '/transactions',
    method: 'get',
    params
  })
}

// 获取凭证详情
export function getTransaction(id) {
  return request({
    url: `/transactions/${id}`,
    method: 'get'
  })
}