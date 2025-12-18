import request from '@/utils/request'

/**
 * 获取指定账套下的所有科目
 * @param {number} bookId - 账套ID
 */
export function getAccountList(bookId) {
  return request({
    url: '/accounts',
    method: 'get',
    params: { bookId }
  })
}

/**
 * [新增] 初始化标准会计科目体系
 * @param {number} bookId - 账套ID
 */
export function initializeAccounts(bookId) {
  return request({
    url: '/accounts/initialize',
    method: 'post',
    params: { bookId } // 注意：后端是用 @RequestParam 接收的，所以放在 params 里
  })
}

/**
 * 创建新科目
 */
export function createAccount(data) {
  return request({
    url: '/accounts',
    method: 'post',
    data
  })
}

/**
 * 更新科目信息
 */
export function updateAccount(id, data) {
  return request({
    url: `/accounts/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除科目
 */
export function deleteAccount(id) {
  return request({
    url: `/accounts/${id}`,
    method: 'delete'
  })
}