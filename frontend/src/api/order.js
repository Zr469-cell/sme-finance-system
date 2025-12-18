import request from '@/utils/request'

// 创建订单 (采购/销售/生产) - 草稿状态
export function createOrder(data) {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

/**
 * 审核并执行订单
 * @param {number} id 订单ID
 * @param {object} data { executeDate: 'YYYY-MM-DD', useTransit: boolean }
 */
export function approveOrder(id, data) {
  return request({
    url: `/orders/${id}/approve`,
    method: 'post',
    data // 将日期和冲销选项作为请求体发送
  })
}

// 查询订单列表
export function getOrderList(params) {
  // params 包含: bookId, status (可选)
  return request({
    url: '/orders',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrder(id) {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  })
}