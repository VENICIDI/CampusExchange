import request from '@/utils/request'

/**
 * 获取待审核用户列表
 */
export function getPendingUsers() {
  return request({
    url: '/api/admin/users/pending',
    method: 'get'
  })
}

/**
 * 审核通过用户
 * @param {number} userId - 用户ID
 */
export function approveUser(userId) {
  return request({
    url: `/api/admin/users/${userId}/approve`,
    method: 'put'
  })
}

/**
 * 拒绝用户注册
 * @param {number} userId - 用户ID
 */
export function rejectUser(userId) {
  return request({
    url: `/api/admin/users/${userId}/reject`,
    method: 'put'
  })
}

/**
 * 获取用户列表（支持分页和条件筛选）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} [params.role] - 角色筛选
 * @param {string} [params.status] - 状态筛选
 * @param {string} [params.keyword] - 关键词搜索
 */
export function getAllUsers(params) {
  return request({
    url: '/api/admin/users',
    method: 'get',
    params
  })
}

/**
 * 更新用户状态
 * @param {number} userId - 用户ID
 * @param {string} status - 新状态（NORMAL/DISABLED）
 */
export function updateUserStatus(userId, status) {
  return request({
    url: `/api/admin/users/${userId}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 获取商家详细信息
 * @param {number} userId - 用户ID
 * @returns {Promise} - 包含商家详情的Promise
 */
export function getMerchantDetails(userId) {
  return request({
    url: `/api/admin/merchants/user/${userId}`,
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {number} userId - 用户ID
 * @param {Object} userData - 用户更新数据
 * @returns {Promise} - 包含更新结果的Promise
 */
export function updateUserInfo(userId, userData) {
  return request({
    url: `/api/admin/users/${userId}`,
    method: 'put',
    data: userData
  })
}

/**
 * 获取用户统计数据（总数等）
 * @returns {Promise} - 包含用户统计数据的Promise
 */
export function getUserStats() {
  return request({
    url: '/api/admin/users/stats',
    method: 'get'
  })
} 