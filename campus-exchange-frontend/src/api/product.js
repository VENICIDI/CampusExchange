import api from './index';

// 商品相关接口
export const productApi = {
  // 获取商品列表
  getProducts: (params) => {
    console.log('调用获取商品列表API，参数:', params);
    return api.get('/products', { params });
  },
  
  // 获取商品详情
  getProductById: (id) => api.get(`/products/${id}`),
  
  // 发布新商品
  createProduct: (productData) => {
    // 获取当前用户信息，确保用户信息在请求头中
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const headers = {};
    
    // 添加用户ID
    if (user.userId) {
      headers['X-User-Id'] = user.userId.toString();
    }
    
    // 添加用户名
    if (user.username) {
      headers['X-User-Name'] = user.username;
    }
    
    // 添加用户角色
    if (user.role) {
      headers['X-User-Role'] = user.role;
    }
    
    return api.post('/products', productData, { headers });
  },
  
  // 更新商品信息
  updateProduct: (id, productData) => api.put(`/products/${id}`, productData),
  
  // 删除商品
  deleteProduct: (id) => api.delete(`/products/${id}`),
  
  // 上传商品图片
  uploadProductImages: (formData) => api.post('/products/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),
  
  // 搜索商品
  searchProducts: (keyword, pageNum = 1, pageSize = 10) => 
    api.get('/products/search', { 
      params: { keyword, pageNum, pageSize } 
    }),
  
  // 获取商品分类
  getCategories: () => api.get('/products/categories'),
  
  // 获取所有分类
  getAllCategories: () => api.get('/categories'),
  
  // 获取顶级分类
  getTopCategories: () => api.get('/categories/top'),
  
  // 获取子分类
  getChildCategories: (parentId) => api.get('/categories/children', { 
    params: { parentId } 
  }),
  
  // 获取商家商品列表
  getMerchantProducts: (merchantId) => {
    console.log('调用getMerchantProducts API，商家ID:', merchantId);
    // 确保merchantId是数值类型
    const numericMerchantId = Number(merchantId);
    
    if (isNaN(numericMerchantId) || numericMerchantId <= 0) {
      console.error('无效的商家ID:', merchantId);
      return Promise.reject(new Error('无效的商家ID'));
    }
    
    console.log('发送获取商家商品请求，URL:', `/products/merchant/${numericMerchantId}`);
    
    // 获取当前用户信息，确保用户信息在请求头中
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const headers = {};
    
    // 添加用户ID
    if (user.userId) {
      headers['X-User-Id'] = user.userId.toString();
      console.log('添加用户ID到请求头:', user.userId);
    }
    
    return api.get(`/products/merchant/${numericMerchantId}`, { headers });
  },
}; 