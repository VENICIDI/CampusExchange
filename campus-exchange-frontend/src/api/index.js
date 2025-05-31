import axios from 'axios';
// 导入路由以便在API响应中可以使用路由跳转
import router from '../router';
import * as user from './user'
import * as admin from './admin'
import * as adminUser from './adminUser'
import * as adminProduct from './adminProduct'
import * as adminMerchantLevel from './adminMerchantLevel'
import * as adminMerchant from './adminMerchant'

// 创建axios实例
const api = axios.create({
  baseURL: '/api', 
  timeout: 15000, 
  withCredentials: false 
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 记录请求开始时间，用于计算请求耗时
    config.metadata = { startTime: new Date() };
    
    // 记录请求详情（
    console.log(`请求 ${config.method.toUpperCase()} ${config.url}`, {
      url: config.url,
      method: config.method,
      params: config.params,
      data: config.data ? 
        (typeof config.data === 'object' && config.data.password ? 
          { ...config.data, password: '******' } : config.data) 
        : undefined
    });
    
    // 从本地存储获取用户信息
    const user = localStorage.getItem('user');
    if (user) {
      try {
        const userData = JSON.parse(user);
        // 有token，添加到Authorization头
        if (userData.token && config.headers) {
          config.headers['Authorization'] = `Bearer ${userData.token}`;
        }
        // 没有token，仍添加用户ID到请求头以便后端识别用户
        if (userData.userId && userData.userId > 0) {
          config.headers['X-User-Id'] = userData.userId;
          config.headers['X-User-Name'] = userData.username || '';
          
          // 添加用户角色到请求头
          if (userData.role) {
            config.headers['X-User-Role'] = userData.role;
          }
        }
      } catch (e) {
        console.error('解析用户数据失败:', e);
        // 如果用户数据有问题，清除它
        localStorage.removeItem('user');
      }
    }
    return config;
  },
  (error) => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 计算请求耗时
    const endTime = new Date();
    const duration = endTime - response.config.metadata.startTime;
    console.log(`请求 ${response.config.url} 耗时: ${duration}ms`);
    
    return response;
  },
  (error) => {
    // 详细处理错误响应
    if (error.response) {
      // 服务器响应了，但状态码超出了2xx范围
      console.error('API错误响应:', error.response.status, error.response.data);

      // 如果后端返回了格式化的错误信息
      if (error.response.data && error.response.data.message) {
        console.log('后端返回错误信息:', error.response.data.message);
        error.message = error.response.data.message;
      }

      // 处理特定的错误码
      if (error.response.status === 401) {
        // 未授权，需要重新登录
        localStorage.removeItem('user');
        // 使用导入的router实例进行跳转
        const currentRoute = router.currentRoute.value;
        if (currentRoute.name !== 'login') {
          router.push({
            name: 'login',
            query: { redirect: currentRoute.fullPath }
          });
        }
      } else if (error.response.status === 403) {
        // 没有权限访问
        console.error('无权限访问该资源');
      }
    } else if (error.request) {
      // 请求已经发出，但没有收到响应
      console.error('API没有响应:', error.request);
      
      // 检查是否是超时错误
      if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
        error.message = '请求超时，请检查网络连接并重试';
      } else {
        error.message = '服务器无响应，请稍后重试';
      }
    } else {
      // 在设置请求时出了问题
      console.error('API请求错误:', error.message);
    }

    return Promise.reject(error);
  }
);

// 添加全局请求/响应日志记录工具
api.logRequest = (config) => {
  console.log('发送请求:', {
    url: config.url,
    method: config.method,
    params: config.params,
    data: config.data ? 
      (typeof config.data === 'object' && config.data.password ? 
        { ...config.data, password: '******' } : config.data) 
      : undefined,
    headers: config.headers
  });
};

export default api;

export {
  user,
  admin,
  adminUser,
  adminProduct,
  adminMerchantLevel,
  adminMerchant
} 