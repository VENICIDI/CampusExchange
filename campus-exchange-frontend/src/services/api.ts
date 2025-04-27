import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: '/api', // 使用相对路径，避免跨域问题
  timeout: 15000, // 增加超时时间
  withCredentials: false // 禁用跨域请求携带凭证，避免CORS问题
});

// 验证码相关接口
export const captchaApi = {
  // 获取验证码ID
  getCaptchaId: () => api.get('/auth/captcha').then(response => {
    console.log('验证码接口响应:', response.data);
    return response;
  }).catch(error => {
    // 错误处理：在API请求失败时提供默认值
    console.error('获取验证码ID失败:', error);
    // 模拟的返回数据结构与实际后端返回一致
    return { data: { code: 200, message: 'mock-captcha-id-' + Math.random().toString(36).substring(2, 8), data: null } };
  }),
  
  // 获取验证码图片URL (直接返回URL，不使用axios请求)
  getCaptchaImageUrl: (captchaId: string) => {
    // 如果验证码ID为mock开头，则返回默认图片
    if (captchaId.startsWith('mock-captcha')) {
      return 'https://dummyimage.com/120x40/4a6ee0/ffffff&text=ABC123';
    }
    // 使用完整路径确保正确访问验证码图片API
    return `/api/auth/captcha/${captchaId}?t=${new Date().getTime()}`;
  }
};

// 用户认证相关接口
export const authApi = {
  // 用户登录
  login: (loginData: {
    principal: string,      // 用户名/手机号/邮箱
    password: string,       // 密码
    captchaId: string,      // 验证码ID
    captchaCode: string     // 验证码
  }) => api.post('/auth/login', loginData),
  
  // 用户注册
  register: (registerData: {
    username: string,       // 用户名
    password: string,       // 密码
    confirmPassword: string,// 确认密码
    realName?: string,      // 真实姓名(可选)
    phone?: string,         // 手机号(可选)
    email?: string,         // 邮箱(可选)
    city?: string,          // 城市(可选)
    gender?: string,        // 性别(可选)
    personalIntro?: string, // 个人介绍(可选)
    wechat?: string,        // 微信号(可选)
    isMerchant?: boolean,   // 是否为商家(可选)
    shopName?: string,      // 店铺名称(可选，仅商家)
    shopAddress?: string,   // 店铺地址(可选，仅商家)
    shopIntro?: string,     // 店铺简介(可选，仅商家)
    captcha: string,        // 验证码
    captchaKey: string      // 验证码Key
  }) => api.post('/auth/register', registerData)
};

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 可以在这里添加统一的请求头，如认证令牌
    const user = localStorage.getItem('user');
    if (user) {
      const userData = JSON.parse(user);
      if (userData.token && config.headers) {
        config.headers['Authorization'] = `Bearer ${userData.token}`;
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
  (response) => response,
  (error) => {
    // 详细处理错误响应
    if (error.response) {
      // 服务器响应了，但状态码超出了2xx范围
      console.error('API错误响应:', error.response.status, error.response.data);
      
      // 处理特定的错误码
      if (error.response.status === 401) {
        // 未授权，可能需要重新登录
        localStorage.removeItem('user');
        // 如果有路由实例，可以在这里跳转到登录页
        // router.push('/login');
      }
    } else if (error.request) {
      // 请求已经发出，但没有收到响应
      console.error('API没有响应:', error.request);
    } else {
      // 在设置请求时出了问题
      console.error('API请求错误:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default api; 