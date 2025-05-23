import request from '@/utils/request';

/**
 * 获取所有商家列表(分页)
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} [params.keyword] - 关键词搜索
 */
export function getAllMerchants(params) {
  return request({
    url: '/api/admin/merchants',
    method: 'get',
    params
  });
}

/**
 * 根据ID获取商家详情
 * @param {number} id - 商家ID
 * @returns {Promise}
 */
export function getMerchantById(id) {
  return request({
    url: `/api/admin/merchants/${id}`,
    method: 'get'
  });
}

/**
 * 根据用户ID获取商家详情
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function getMerchantByUserId(userId) {
  return request({
    url: `/api/admin/merchants/user/${userId}`,
    method: 'get'
  });
}

/**
 * 获取商家等级列表
 */
export function getMerchantLevels() {
  return request({
    url: '/api/admin/merchants/levels',
    method: 'get'
  });
}

/**
 * 更新商家等级
 * @param {number} merchantId - 商家ID
 * @param {number} levelId - 新等级ID
 */
export function updateMerchantLevel(merchantId, levelId) {
  return request({
    url: `/api/admin/merchants/${merchantId}/level`,
    method: 'put',
    params: { levelId }
  });
}

/**
 * 获取商家商品列表
 * @param {number} merchantId - 商家ID
 * @param {string} [status] - 商品状态过滤
 */
export function getMerchantProducts(merchantId, status) {
  return request({
    url: `/api/admin/merchants/${merchantId}/products`,
    method: 'get',
    params: { status }
  });
}

/**
 * 批量下架商家所有商品
 * @param {number} merchantId - 商家ID
 */
export function takeDownAllProducts(merchantId) {
  return request({
    url: `/api/admin/merchants/${merchantId}/products/take-down`,
    method: 'put'
  });
} 