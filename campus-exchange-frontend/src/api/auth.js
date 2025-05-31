import api from './index';

// 验证码相关接口
export const captchaApi = {
  // 获取验证码ID
  getCaptchaId: () => {
    console.log('请求验证码ID');
    return api.get('/auth/captcha')
      .then(response => {
        console.log( '验证码接口原始响应:', response);
        // 确保返回标准格式数据
        if (response.data) {
          console.log('验证码接口处理后响应:', response.data);
          return response;
        } else {
          console.error('验证码接口响应格式异常:', response);
          throw new Error('验证码响应格式错误');
        }
      })
      .catch(error => {
        // 详细记录错误
        console.error('获取验证码ID失败:', error);
        if (error.response) {
          console.error('错误状态码:', error.response.status);
          console.error('错误数据:', error.response.data);
        }
        // 在开发环境中提供模拟数据以便测试
        if (process.env.NODE_ENV === 'development') {
          console.warn('使用模拟验证码数据 (仅用于开发环境)');
          // 返回与后端一致的数据结构
          return { 
            data: { 
              code: 200, 
              message: "验证码生成成功",
              data: 'mock-captcha-id-' + Math.random().toString(36).substring(2, 8)
            } 
          };
        }
        
        // 在生产环境中抛出错误
        throw error;
      });
  },

  // 获取验证码图片URL (直接返回URL，不使用axios请求)
  getCaptchaImageUrl: (captchaId) => {
    // 如果验证码ID为mock开头，返回默认图片
    if (captchaId && captchaId.startsWith('mock-captcha')) {
      console.log('使用模拟验证码图片URL');
      return 'https://dummyimage.com/120x40/4a6ee0/ffffff&text=ABC123';
    }
    
    // 验证captchaId有效性
    if (!captchaId) {
      console.error('captchaId为空，无法获取验证码图片');
      return 'https://dummyimage.com/120x40/ff0000/ffffff&text=ERROR';
    }
    
    // 使用完整路径确保正确访问验证码图片API
    // 根据后端的CaptchaController和AuthController中的映射，验证码图片路径可能是两种格式之一
    // 尝试AuthController格式
    const url = `/api/auth/captcha/${captchaId}?t=${new Date().getTime()}`;
    console.log('验证码图片URL:', url);
    return url;
  }
};

// 用户认证相关接口
export const authApi = {
  // 用户登录
  login: (loginData) => {
    console.log('发送登录请求:', { ...loginData, password: '******' });
    
    // 验证必要字段
    if (!loginData.principal || !loginData.password || !loginData.captchaCode || !loginData.captchaId) {
      console.error('登录请求缺少必要字段');
      return Promise.reject(new Error('登录请求缺少必要字段'));
    }
    
    return api.post('/auth/login', loginData)
      .then(response => {
        console.log('登录响应成功:', response);
        
        // 验证响应格式
        if (response.data && response.data.code === 200) {
          return response;
        } else {
          console.error('登录响应格式异常:', response);
          const error = new Error(response.data?.message || '登录响应格式错误');
          error.response = response;
          throw error;
        }
      })
      .catch(error => {
        console.error('登录请求失败:', error);
        
        if (error.response && error.response.data) {
          console.error('服务器返回错误:', error.response.data);
          // 直接从后端响应中获取错误信息和代码
          const responseData = error.response.data;
          
          // 如果后端返回了格式化的错误信息
          if (responseData.code && responseData.message) {
            error.message = responseData.message;
            error.code = responseData.code;
            console.log('使用后端提供的错误信息:', error.message);
          }
        }
        
        throw error;
      });
  },

  // 用户注册
  register: (registerData) => {
    console.log('发送注册请求:', { 
      ...registerData, 
      password: '******', 
      confirmPassword: '******'
    });
    
    // 发送前验证必要字段
    if (!registerData.username || !registerData.password || !registerData.confirmPassword ||
        !registerData.captcha || !registerData.captchaKey) {
      console.error('注册请求缺少必要字段');
      return Promise.reject(new Error('注册请求缺少必要字段'));
    }
    
    return api.post('/auth/register', registerData)
      .then(response => {
        console.log('注册响应成功:', response);
        
        // 验证响应格式
        if (response.data && response.data.code === 200) {
          return response;
        } else {
          console.error('注册响应格式异常:', response);
          const error = new Error(response.data?.message || '注册响应格式错误');
          error.response = response;
          throw error;
        }
      })
      .catch(error => {
        console.error('注册请求失败:', error);
        throw error;
      });
  },
  
  // 退出登录
  logout: () => api.post('/auth/logout')
    .then(response => {
      console.log('退出登录成功:', response);
      return response;
    })
    .catch(error => {
      console.error('退出登录失败:', error);
      throw error;
    })
}; 