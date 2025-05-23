import request from '@/utils/request'

/**
 * 获取待支付订单列表
 * @returns {Promise} 待支付订单列表
 */
export function getPendingOrders() {
  return request({
    url: '/api/admin/discounts/pending-orders',
    method: 'get'
  })
}

/**
 * 为订单发放折扣
 * @param {Number} orderId 订单ID
 * @param {Number} discountAmount 折扣金额
 * @returns {Promise} 操作结果
 */
export function createDiscount(orderId, discountAmount) {
  return request({
    url: `/api/admin/discounts/${orderId}`,
    method: 'post',
    data: {
      discountAmount: discountAmount
    }
  })
}

/**
 * 获取订单折扣详情
 * @param {Number} orderId 订单ID
 * @returns {Promise} 订单折扣详情
 */
export function getOrderDiscount(orderId) {
  return request({
    url: `/api/admin/discounts/${orderId}`,
    method: 'get'
  })
} 