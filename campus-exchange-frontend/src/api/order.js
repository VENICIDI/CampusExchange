import api from './index';
import request from '@/utils/request';

// 订单相关接口
export const orderApi = {
  // 创建订单
  createOrder: (orderData) => api.post('/orders', orderData),
  
  // 从购物车创建订单
  createOrderFromCart: (orderData) => api.post('/orders/from-cart', orderData),
  
  // 获取用户订单列表
  getUserOrders: (params) => api.get('/orders/user', { params }),
  
  // 获取商家订单列表
  getMerchantOrders: (params) => api.get('/orders/merchant-orders', { params }),
  
  // 获取订单状态统计
  getOrderStatusCounts: () => api.get('/orders/status-counts'),
  
  // 获取用户订单状态统计
  getUserOrderStatusCounts: () => api.get('/orders/user-status-counts'),
  
  // 获取订单详情
  getOrderDetail: (orderNo) => api.get(`/orders/${orderNo}`),
  
  // 取消订单
  cancelOrder: (orderNo) => api.post(`/orders/${orderNo}/cancel`),
  
  // 确认收货
  confirmReceipt: (orderNo) => api.post(`/orders/${orderNo}/confirm`),
  
  // 支付订单（模拟支付）
  payOrder: (orderNo) => api.post(`/orders/${orderNo}/pay`),
  
  // 商家发货
  shipOrder: (params) => {
    if (typeof params === 'string') {
      // 兼容直接传入订单号的调用方式
      return api.post(`/orders/${params}/ship`);
    } else {
      // 处理传入对象的情况
      return api.post(`/orders/${params.orderNo}/ship`);
    }
  },
  
  // 申请退货/退款
  requestReturn: (orderNo, reason) => api.post(
    `/orders/${orderNo}/return/request`,
    null,
    { params: { reason } }
  ),
  
  // 商家处理退货/退款申请
  processReturnRequest: (params, approve, remark) => {
    if (typeof params === 'string') {
      // 兼容直接传入订单号的调用方式
      return api.post(
        `/orders/${params}/return/process`,
        null,
        { params: { approve, remark } }
      );
    } else {
      // 处理传入对象的情况
      return api.post(
        `/orders/${params.orderNo}/return/process`,
        null,
        { params: { approve: params.approve, remark: params.remark } }
      );
    }
  },
  
  // 买家发出退货
  buyerReturnGoods: (orderNo, trackingInfo) => api.post(
    `/orders/${orderNo}/return/ship`,
    null,
    { params: { trackingInfo } }
  ),
  
  // 商家确认收到退货
  confirmReturnReceived: (orderNo) => api.post(`/orders/${orderNo}/return/received`),
  
  // 获取用户待评价订单
  getUnreviewedOrders: () => api.get('/reviews/unreviewedOrders'),
  
  // 订单状态文本映射
  orderStatusText: {
    PENDING_PAYMENT: '待付款',
    PENDING_SHIPMENT: '待发货',
    SHIPPED: '已发货',
    RECEIVED: '已收货',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    RETURN_REQUESTED: '申请退货中',
    RETURN_APPROVED: '退货审核通过',
    RETURN_GOODS_RECEIVED: '收到退货',
    RETURNED: '已退货退款',
    RETURN_REJECTED: '拒绝退货'
  },
  
  // 获取订单状态文本
  getStatusText: (status) => {
    return orderApi.orderStatusText[status] || status;
  }
}; 