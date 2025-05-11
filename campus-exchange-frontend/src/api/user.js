import api from './index';

// 用户信息相关接口
export const userApi = {
  // 获取当前用户信息
  getCurrentUser: () => api.get('/user/current'),
  
  // 获取用户资料
  getUserProfile: () => api.get('/users/profile'),
  
  // 更新用户信息
  updateUserInfo: (userData) => api.put('/user/update', userData),
  
  // 更新用户资料
  updateUserProfile: (userData) => api.put('/users/profile', userData),
  
  // 更新用户头像
  updateAvatar: (formData) => api.post('/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),
  
  // 修改密码
  changePassword: (passwordData) => api.post('/user/password', passwordData),
  
  // 获取用户统计信息
  getUserStats: () => api.get('/user/statistics')
}; 