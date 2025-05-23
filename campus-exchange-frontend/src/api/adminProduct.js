import request from '@/utils/request'

/**
 * 获取待审核商品列表
 * @param {Object} params - 分页参数
 */
export function getPendingProducts(params) {
  return request({
    url: '/api/admin/products/pending',
    method: 'get',
    params
  })
}

/**
 * 获取所有商品
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {number} [params.categoryId] - 分类ID
 * @param {string} [params.status] - 状态筛选
 * @param {string} [params.keyword] - 关键词搜索
 */
export function getAllProducts(params) {
  return request({
    url: '/api/admin/products',
    method: 'get',
    params
  })
}

/**
 * 审核通过商品
 * @param {number} productId - 商品ID
 */
export function approveProduct(productId) {
  return request({
    url: `/api/admin/products/${productId}/approve`,
    method: 'put'
  })
}

/**
 * 拒绝商品
 * @param {number} productId - 商品ID
 * @param {string} reason - 拒绝原因
 */
export function rejectProduct(productId, reason) {
  return request({
    url: `/api/admin/products/${productId}/reject`,
    method: 'put',
    params: { reason }
  })
}

/**
 * 下架商品
 * @param {number} productId - 商品ID
 */
export function offShelfProduct(productId) {
  return request({
    url: `/api/admin/products/${productId}/off-shelf`,
    method: 'put'
  })
}

/**
 * 获取商品详情
 * @param {number} productId - 商品ID
 */
export function getProductDetail(productId) {
  return request({
    url: `/api/admin/products/${productId}`,
    method: 'get'
  })
}

/**
 * 获取商品统计数据（总数等）
 * @returns {Promise} - 包含商品统计数据的Promise
 */
export function getProductStats() {
  return request({
    url: '/api/admin/products/stats',
    method: 'get'
  })
} 