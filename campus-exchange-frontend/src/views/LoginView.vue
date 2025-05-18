<template>
  <div class="login-container">
    <div class="login-box">
      <h2>校园二手交易平台登录</h2>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="principal">账号</label>
          <input
            type="text"
            id="principal"
            v-model="loginForm.principal"
            placeholder="用户名/手机号/邮箱"
            required
          />
          <div class="error-message" v-if="errors.principal">{{ errors.principal }}</div>
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
            type="password"
            id="password"
            v-model="loginForm.password"
            placeholder="请输入密码"
            required
          />
          <div class="error-message" v-if="errors.password">{{ errors.password }}</div>
        </div>

        <div class="form-group captcha-group">
          <label for="captchaCode">验证码</label>
          <div class="captcha-wrapper">
            <input
              type="text"
              id="captchaCode"
              v-model="loginForm.captchaCode"
              placeholder="请输入验证码"
              required
            />
            <img 
              v-if="captchaImageUrl && !isCaptchaLoading" 
              :src="captchaImageUrl" 
              @click="refreshCaptcha" 
              @error="handleCaptchaError"
              alt="验证码" 
              title="点击刷新验证码"
            />
            <div v-else class="captcha-loading" @click="refreshCaptcha">
              <span class="loading-spinner"></span>
              加载中...
            </div>
          </div>
          <div class="error-message" v-if="errors.captchaCode">{{ errors.captchaCode }}</div>
        </div>

        <div v-if="errors.general" class="error-general">
          {{ errors.general }}
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-login" :disabled="isSubmitting">
            <span class="btn-text">{{ isSubmitting ? '登录中...' : '登录' }}</span>
            <span class="btn-icon" v-if="!isSubmitting">→</span>
            <span class="btn-loading" v-else></span>
          </button>
        </div>
        
        <div class="form-links">
          <router-link to="/register" class="register-link">没有账号？立即注册</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { authApi, captchaApi } from '@/api/all';
import { validatePassword, validateCaptcha } from '@/utils/validate';

const router = useRouter();

// 表单数据
const loginForm = ref({
  principal: '',
  password: '',
  captchaCode: '',
  captchaId: ''
});

// 错误信息
const errors = ref({
  principal: '',
  password: '',
  captchaCode: '',
  general: ''
});

// 加载状态
const isSubmitting = ref(false);
const captchaImageUrl = ref('');
const isCaptchaLoading = ref(false);

// 是否为开发环境
const isDev = process.env.NODE_ENV === 'development';

// 计算属性 - 表单是否有效
const isFormValid = computed(() => {
  return (
    loginForm.value.principal.trim() !== '' &&
    validatePassword(loginForm.value.password) &&
    validateCaptcha(loginForm.value.captchaCode) &&
    loginForm.value.captchaId !== ''
  );
});

// 处理验证码加载失败
const handleCaptchaError = (e) => {
  const target = e.target;
  console.error('验证码图片加载失败:', {
    error: e,
    url: target.src,
    captchaId: loginForm.value.captchaId,
    captchaImageUrl: captchaImageUrl.value
  });
  
  errors.value.captchaCode = "验证码加载失败，请点击刷新";
  isCaptchaLoading.value = false;
  
  // 尝试异步重新加载验证码
  setTimeout(async () => {
    try {
      console.log('自动重新加载验证码');
      await refreshCaptcha();
      console.log('验证码已重新加载');
    } catch (error) {
      console.error('重新加载验证码失败:', error);
    }
  }, 1500);
};

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    console.log('刷新验证码函数被调用');
    
    // 清空当前验证码和错误信息
    loginForm.value.captchaCode = '';
    isCaptchaLoading.value = true;
    
    // 直接调用API获取新验证码
    const response = await captchaApi.getCaptchaId();
    console.log('获取新验证码响应:', response);
    
    if (response && response.data && response.data.code === 200) {
      let captchaId = response.data.data || response.data.message;
      
      if (captchaId) {
        // 更新验证码ID和图片URL
        loginForm.value.captchaId = captchaId;
        captchaImageUrl.value = captchaApi.getCaptchaImageUrl(captchaId);
        console.log('验证码已更新:', captchaId);
      } else {
        console.error('无法获取验证码ID');
        errors.value.captchaCode = '获取验证码失败，请点击刷新';
      }
    } else {
      console.error('获取验证码失败:', response);
      errors.value.captchaCode = '获取验证码失败，请点击刷新';
    }
  } catch (error) {
    console.error('刷新验证码出错:', error);
    errors.value.captchaCode = '获取验证码失败，请点击刷新';
  } finally {
    isCaptchaLoading.value = false;
  }
};

// 获取验证码
const fetchCaptcha = async () => {
  try {
    errors.value.captchaCode = '';
    errors.value.general = '';
    isCaptchaLoading.value = true;

    console.log('开始获取验证码...');
    const response = await captchaApi.getCaptchaId();
    
    console.log('验证码API响应:', response);
    
    // 检查响应格式，确保我们拿到正确的captchaId
    if (response && response.data) {
      // 根据后端返回格式获取验证码ID
      if (response.data.code === 200) {
        let captchaId = null;
        
        // 优先从data中获取验证码ID，这是标准格式
        if (response.data.data) {
          captchaId = response.data.data;
          console.log('从data中获取验证码ID:', captchaId);
        }
        // 可能验证码ID在message中 - 根据后端日志显示
        else if (response.data.message) {
          captchaId = response.data.message;
          console.log('从message中获取验证码ID:', captchaId);
        }
        
        // 获取验证码图片URL
        if (captchaId) {
          loginForm.value.captchaId = captchaId;
          captchaImageUrl.value = captchaApi.getCaptchaImageUrl(captchaId);
          console.log('验证码图片URL:', captchaImageUrl.value);
        } else {
          console.error('未能从响应中提取验证码ID');
          errors.value.captchaCode = '获取验证码失败，请点击刷新重试';
        }
      } else {
        console.error('验证码API返回错误:', response.data);
        errors.value.captchaCode = response.data.message || '获取验证码失败，请点击刷新重试';
      }
    } else {
      console.error('验证码API响应格式无效:', response);
      errors.value.captchaCode = '获取验证码失败，请点击刷新重试';
    }
  } catch (error) {
    console.error('获取验证码失败:', error);
    errors.value.captchaCode = '获取验证码失败，请点击刷新重试';
  } finally {
    isCaptchaLoading.value = false;
  }
};

// 表单验证
const validateForm = () => {
  let isValid = true;
  errors.value = {
    principal: '',
    password: '',
    captchaCode: '',
    general: ''
  };

  if (!loginForm.value.principal.trim()) {
    errors.value.principal = '账号不能为空';
    isValid = false;
  }

  if (!validatePassword(loginForm.value.password)) {
    errors.value.password = '密码格式不正确，请确认密码格式';
    isValid = false;
  }

  if (!validateCaptcha(loginForm.value.captchaCode)) {
    errors.value.captchaCode = '验证码格式不正确';
    isValid = false;
  }

  return isValid;
};

// 登录提交
const handleLogin = async () => {
  if (!validateForm() || isSubmitting.value) {
    return;
  }

  isSubmitting.value = true;
  errors.value.general = '';
  errors.value.captchaCode = ''; // 清除之前的验证码错误

  try {
    // 创建请求数据，确保字段名与后端LoginRequestDTO匹配
    const requestData = {
      principal: loginForm.value.principal,
      password: loginForm.value.password,
      captchaCode: loginForm.value.captchaCode,  // 后端期望captchaCode，与LoginRequestDTO一致
      captchaId: loginForm.value.captchaId       // 后端期望captchaId，与LoginRequestDTO一致
    };

    console.log('提交登录数据:', {
      ...requestData,
      password: '******' // 隐藏密码
    });

    const response = await authApi.login(requestData);
    
    // 登录成功
    console.log('登录成功:', response.data);
    
    // 检查响应中是否包含用户信息
    if (response.data && response.data.code === 200 && response.data.data) {
      // 提取用户数据
      const responseData = response.data.data;
      
      // 验证userId有效性
      if (!responseData.userId || responseData.userId <= 0) {
        throw new Error('登录失败：服务器返回的用户ID无效 (ID: ' + (responseData.userId || 'undefined') + ')');
      }
      
      // 保存用户信息到本地存储
      const userData = {
        userId: responseData.userId,
        username: responseData.username || '用户',
        role: responseData.role || 'USER',
        avatar: responseData.avatar || '',
        // 如果服务器返回了merchantId，也保存到userData中
        merchantId: responseData.merchantId || null
      };
      
      console.log('保存用户数据到本地存储:', userData);
      localStorage.setItem('user', JSON.stringify(userData));
      
      // 如果是商家角色，并且接口返回了merchantId，则单独保存merchantId
      if (responseData.role === 'MERCHANT' && responseData.merchantId) {
        console.log('将商家ID保存到localStorage:', responseData.merchantId);
        localStorage.setItem('merchantId', responseData.merchantId.toString());
      }
      
      // 触发用户登录事件，通知App.vue更新用户状态
      window.dispatchEvent(new Event('user-login'));
      window.dispatchEvent(new Event('storage'));
      console.log('已触发user-login和storage事件，通知App.vue更新用户状态');
      
      // 提示用户登录成功
      const successMessage = response.data.message || '登录成功';
      console.log(successMessage);
      
      // 根据用户角色跳转到不同页面
      console.log('根据用户角色跳转:', userData.role);
      
      // 检查是否有重定向页面
      const redirectPath = router.currentRoute.value.query.redirect;
      
      // 如果有重定向路径，且用户角色允许访问该路径，则跳转到该路径
      if (redirectPath) {
        console.log('有重定向路径:', redirectPath);
        router.push(redirectPath).catch(err => {
          console.error('重定向跳转失败:', err);
          // 如果重定向失败，回退到基于角色的默认跳转
          redirectBasedOnRole(userData.role);
        });
      } else {
        // 没有重定向路径，根据角色进行默认跳转
        redirectBasedOnRole(userData.role);
      }
    } else {
      throw new Error('登录响应数据异常');
    }
  } catch (error) {
    console.error('登录失败:', error);
    
    // 处理各种错误
    if (error.response) {
      const { data, status } = error.response;
      
      console.log('登录错误状态:', status);
      console.log('登录错误数据:', data);
      
      // 直接处理验证码错误 - 根据后端日志，验证码错误返回的是 code=400
      if (data && status === 400 && data.code === 400 && data.message) {
        console.log('检测到错误响应:', data.message);
        
        // 判断是否与验证码相关
        if (data.message.includes('验证码')) {
          console.log('捕获到验证码错误:', data.message);
          
          // 设置错误信息
          errors.value.captchaCode = data.message;
          
          // 立即刷新验证码
          refreshCaptcha();
          
          return;
        }
      }
      
      // 处理一般错误
      if (data && data.message) {
        errors.value.general = data.message;
      } else {
        // 根据HTTP状态码给出通用错误信息
        if (status === 400) {
          errors.value.general = '请求参数错误，请检查输入';
        } else if (status === 401) {
          errors.value.general = '用户名或密码错误';
        } else if (status === 403) {
          errors.value.general = '账号未激活或已被禁用，请联系管理员';
        } else if (status === 404) {
          errors.value.general = '用户不存在，请检查账号或注册新账号';
        } else if (status >= 500) {
          errors.value.general = '服务器错误，请稍后重试';
        } else {
          errors.value.general = '登录失败，请稍后重试';
        }
      }
    } else if (error.request) {
      errors.value.general = '网络错误，服务器无响应，请检查网络连接';
    } else {
      errors.value.general = error.message || '登录失败，请稍后重试';
    }
  } finally {
    isSubmitting.value = false;
  }
};

// 根据角色跳转到对应页面
function redirectBasedOnRole(role) {
  if (role === 'ADMIN') {
    console.log('管理员登录，跳转到管理后台');
    router.push('/admin');
  } else if (role === 'MERCHANT') {
    console.log('商家登录，跳转到商家中心');
    router.push('/merchant');
  } else {
    console.log('普通用户登录，跳转到首页');
    router.push('/');
  }
}

// 组件挂载时获取验证码
onMounted(() => {
  fetchCaptcha();
});
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 170px);
  padding: 40px 20px;
  background: linear-gradient(120deg, #f0f4ff 0%, #f8f9fa 100%);
  position: relative;
}

.login-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><circle cx="50" cy="50" r="1.5" fill="%234a6ee020"/></svg>');
  background-size: 50px 50px;
  opacity: 0.6;
}

.login-box {
  position: relative;
  width: 100%;
  max-width: 420px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  padding: 40px;
  transition: all 0.3s ease;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-box:hover {
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: 700;
  position: relative;
  padding-bottom: 12px;
}

h2::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 3px;
  background: linear-gradient(90deg, #4a6ee0, #6a8fff);
  border-radius: 3px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

label {
  font-weight: 600;
  color: #444;
  font-size: 14px;
}

input {
  padding: 14px 16px;
  border: 1px solid #e0e6f7;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  background-color: #f8f9fa;
}

input:focus {
  border-color: #4a6ee0;
  outline: none;
  box-shadow: 0 0 0 3px rgba(74, 110, 224, 0.15);
  background-color: #fff;
}

.captcha-group {
  position: relative;
  margin-bottom: 5px;
}

.captcha-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-wrapper input {
  flex: 1;
}

.captcha-wrapper img, 
.captcha-loading {
  height: 48px;
  min-width: 120px;
  border: 1px solid #e0e6f7;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  transition: all 0.3s ease;
  font-size: 13px;
  color: #666;
}

.captcha-wrapper img:hover,
.captcha-loading:hover {
  border-color: #4a6ee0;
  box-shadow: 0 0 0 3px rgba(74, 110, 224, 0.15);
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid rgba(74, 110, 224, 0.3);
  border-top-color: #4a6ee0;
  animation: spin 1s linear infinite;
  margin-right: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  color: #e53935;
  font-size: 13px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  line-height: 1.4;
}

.error-message:before {
  content: "⚠️";
  font-size: 12px;
  margin-right: 5px;
  flex-shrink: 0;
}

.form-actions {
  margin-bottom: 18px;
}

.btn-login {
  width: 100%;
  padding: 0;
  background: linear-gradient(135deg, #4a6ee0, #5a7ef2);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(74, 110, 224, 0.25);
  height: 50px;
  position: relative;
  overflow: hidden;
}

.btn-login:hover {
  background: linear-gradient(135deg, #3d5eca, #4a6ee0);
  box-shadow: 0 6px 15px rgba(74, 110, 224, 0.35);
  transform: translateY(-2px);
}

.btn-login:active {
  transform: translateY(0);
  box-shadow: 0 2px 5px rgba(74, 110, 224, 0.3);
}

.btn-login:disabled {
  background: linear-gradient(135deg, #a0aed8, #b3bfe6);
  box-shadow: none;
  cursor: not-allowed;
  transform: none;
}

.btn-text {
  display: inline-block;
  position: relative;
  z-index: 2;
}

.btn-icon {
  display: inline-block;
  position: relative;
  z-index: 2;
  margin-left: 8px;
  font-size: 18px;
  transition: transform 0.3s ease;
}

.btn-login:hover .btn-icon {
  transform: translateX(3px);
}

.btn-loading {
  display: inline-block;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  animation: spin 1s linear infinite;
  margin-left: 10px;
  vertical-align: middle;
  position: relative;
  z-index: 2;
}

.form-links {
  text-align: center;
  margin-top: 0;
}

.form-links a {
  color: #4a6ee0;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.form-links a:hover {
  color: #304b99;
  text-decoration: underline;
}

.error-general {
  color: #e53935;
  background-color: rgba(229, 57, 53, 0.08);
  padding: 12px;
  border-radius: 8px;
  text-align: center;
  margin-top: 12px;
  font-size: 14px;
  font-weight: 500;
  border-left: 3px solid #e53935;
}

@media (max-width: 480px) {
  .login-box {
    padding: 30px 20px;
  }
}
</style> 