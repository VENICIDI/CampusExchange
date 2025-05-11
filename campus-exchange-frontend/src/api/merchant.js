import api from './index';

// 商家相关接口
export const merchantApi = {
  // 获取商家信息(通过用户ID)
  getMerchantByUserId: (userId) => {
    console.log('调用获取商家信息API，用户ID:', userId);
    return api.get(`/merchants/user/${userId}`);
  },
  
  // 获取商家信息(通过商家ID)
  getMerchantById: (merchantId) => {
    console.log('调用获取商家信息API，商家ID:', merchantId);
    return api.get(`/merchants/${merchantId}`);
  },
  
  // 获取商家主页信息
  getMerchantProfile: (merchantId) => {
    console.log('调用获取商家主页信息API，商家ID:', merchantId);
    return api.get(`/merchants/${merchantId}/profile`);
  },
  
  // 创建商家
  createMerchant: (merchantData) => {
    console.log('调用创建商家API');
    return api.post('/merchants', merchantData);
  },
  
  // 更新商家信息
  updateMerchant: (merchantData) => {
    console.log('调用更新商家信息API');
    return api.put(`/merchants/${merchantData.id}`, merchantData);
  }
}; 