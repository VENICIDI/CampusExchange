import axios from 'axios';
import { ElMessage } from 'element-plus';

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  withCredentials: true
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    // 添加调试日志
    console.log(`[请求] ${config.method.toUpperCase()} ${config.url}`, {
      url: config.url,
      method: config.method,
      data: config.data,
      params: config.params,
      headers: config.headers
    });
    
    return config;
  },
  error => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    
    // 添加调试日志
    console.log(`[响应] ${response.config.method.toUpperCase()} ${response.config.url}`, {
      status: response.status,
      data: res,
    });
    
    // 如果是文件下载直接返回
    if (response.config.responseType === 'blob') {
      return response;
    }
    
    // 请求成功
    if (res.code === 200) {
      return response;
    }
    
    // 未登录或token过期
    if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setTimeout(() => {
        window.location.href = '/login';
      }, 1500);
      return Promise.reject(new Error('未登录或登录已过期'));
    }
    
    // 其他错误
    ElMessage.error(res.message || '请求失败');
    return Promise.reject(new Error(res.message || '请求失败'));
  },
  error => {
    console.error('响应错误:', error);
    let message = error.message || '请求失败';
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未登录或登录已过期';
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          setTimeout(() => {
            window.location.href = '/login';
          }, 1500);
          break;
        case 403:
          message = '没有权限进行此操作';
          break;
        case 404:
          message = '请求的资源不存在';
          break;
        case 500:
          message = '服务器错误';
          break;
        default:
          message = error.response.data?.message || `请求失败(${error.response.status})`;
      }
    }
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

export default service; 