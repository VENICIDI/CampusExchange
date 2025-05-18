import request from '@/utils/request';
import api from './index';

/**
 * 评价相关API
 */
export const reviewApi = {
  /**
   * 提交订单评价
   * @param {Object} data 评价数据
   */
  submitOrderReview: (data) => api.post('/reviews/order', data),

  /**
   * 商家提交买家评价
   * @param {String} orderNo 订单号
   * @param {Object} data 评价数据
   */
  submitBuyerReview: (orderNo, data) => {
    // 首先尝试从localStorage直接获取merchantId
    let merchantId = localStorage.getItem('merchantId');
    
    // 如果merchantId为空，尝试从user对象中获取
    if (!merchantId) {
      try {
        const userJson = localStorage.getItem('user');
        if (userJson) {
          const userData = JSON.parse(userJson);
          // 如果user对象中有merchantId字段
          if (userData && userData.merchantId) {
            merchantId = userData.merchantId;
            console.log('从user对象中获取merchantId:', merchantId);
            // 为了后续使用方便，同时保存到localStorage
            localStorage.setItem('merchantId', merchantId);
          } else if (userData && userData.role === 'MERCHANT') {
            // 如果是商家角色但没有merchantId，可能需要获取
            console.log('用户是商家角色但未保存merchantId，尝试从商家信息中获取');
          }
        }
      } catch (e) {
        console.error('解析user数据失败:', e);
      }
    }

    console.log('商家评价买家，使用merchantId:', merchantId);
    const headers = {
      'X-Merchant-Id': merchantId || ''
    };
    return api.post(`/reviews/buyer/${orderNo}`, data, { headers });
  },

  /**
   * 获取商品评价列表
   * @param {Number} productId 商品ID
   */
  getProductReviews: (productId) => api.get(`/reviews/product/${productId}`),

  /**
   * 检查用户是否已评价订单
   * @param {String} orderNo 订单号
   */
  hasReviewedOrder: (orderNo) => api.get(`/reviews/check/${orderNo}`),

  /**
   * 获取用户未评价订单列表
   */
  getUnreviewedOrders: () => api.get('/reviews/unreviewedOrders'),

  /**
   * 获取商家服务评分
   * @param {String} merchantId 商家ID
   */
  getMerchantServiceRating: (merchantId) => api.get(`/reviews/merchant/${merchantId}/rating`),

  /**
   * 获取订单的评价信息（商品评价、商家服务评价和买家评价）
   * @param {String} orderNo 订单号
   */
  getOrderReviews: (orderNo) => api.get(`/reviews/order/${orderNo}/all`)
}; 