import api from './index';

// 购物车相关接口
export const cartApi = {
  // 获取购物车列表
  getCartItems: () => api.get('/cart'),
  
  // 添加商品到购物车
  addToCart: (productId, quantity) => api.post('/cart/add', { productId, quantity }),
  
  // 更新商品数量
  updateQuantity: (cartItemId, quantity) => api.post(`/cart/${cartItemId}/quantity?quantity=${quantity}`),
  
  // 更新商品选中状态
  updateSelected: (cartItemId, selected) => api.post(`/cart/${cartItemId}/selected?selected=${selected}`),
  
  // 删除购物车商品
  removeItem: (cartItemId) => api.delete(`/cart/${cartItemId}`),
  
  // 清空已选中的购物车商品
  clearSelectedItems: () => api.delete('/cart/selected'),
  
  // 测试请求头
  testHeaders: () => api.get('/cart/test-headers')
}; 