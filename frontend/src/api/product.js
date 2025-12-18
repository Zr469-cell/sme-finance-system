import request from '@/utils/request'

// 新增商品
export function createProduct(data) {
  return request({
    url: '/products',
    method: 'post',
    data
  })
}

// 获取商品列表
export function getProductList(bookId) {
  return request({
    url: '/products',
    method: 'get',
    params: { bookId }
  })
}

// 获取单个商品详情
export function getProduct(id) {
  return request({
    url: `/products/${id}`,
    method: 'get'
  })
}

// 删除商品
export function deleteProduct(id) {
  return request({
    url: `/products/${id}`,
    method: 'delete'
  })
}