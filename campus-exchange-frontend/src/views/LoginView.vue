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
              v-if="captchaImageUrl" 
              :src="captchaImageUrl" 
              @click="refreshCaptcha" 
              @error="handleCaptchaError"
              alt="验证码" 
              title="点击刷新验证码"
            />
            <div v-else class="captcha-loading" @click="refreshCaptcha">
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
            {{ isSubmitting ? '登录中...' : '登录' }}
          </button>
        </div>
        
        <div class="form-links">
          <router-link to="/register">没有账号？立即注册</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { authApi, captchaApi } from '@/services/api';
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
const handleCaptchaError = (e: Event) => {
  const target = e.target as HTMLImageElement;
  console.error('验证码图片加载失败:', {
    error: e,
    url: target.src,
    captchaId: loginForm.value.captchaId,
    captchaImageUrl: captchaImageUrl.value
  });
  
  // 尝试重新加载验证码
  setTimeout(() => {
    refreshCaptcha();
  }, 1500);
};

// 刷新验证码
const refreshCaptcha = () => {
  captchaImageUrl.value = '';
  loginForm.value.captchaCode = '';
  fetchCaptcha();
};

// 获取验证码
const fetchCaptcha = async () => {
  try {
    const response = await captchaApi.getCaptchaId();
    loginForm.value.captchaId = response.data.message;
    captchaImageUrl.value = captchaApi.getCaptchaImageUrl(loginForm.value.captchaId);
    console.log('验证码ID:', loginForm.value.captchaId);
    console.log('验证码图片URL:', captchaImageUrl.value);
  } catch (error) {
    console.error('获取验证码失败:', error);
    errors.value.general = '获取验证码失败，请刷新页面重试';
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
    errors.value.password = '密码长度必须在6-20个字符之间';
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

  try {
    const response = await authApi.login(loginForm.value);
    const { data } = response.data;
    
    // 登录成功，存储用户信息并跳转
    localStorage.setItem('user', JSON.stringify(data));
    router.push('/');
  } catch (error: any) {
    // 登录失败处理
    console.error('登录失败:', error);
    
    if (error.response?.data?.message) {
      errors.value.general = error.response.data.message;
    } else {
      errors.value.general = '登录失败，请检查账号和密码';
    }
    
    // 刷新验证码
    refreshCaptcha();
  } finally {
    isSubmitting.value = false;
  }
};

// 组件挂载时获取验证码
onMounted(() => {
  fetchCaptcha();
});
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 60px 20px;
  background-color: #f5f5f5;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

label {
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

input {
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border 0.3s;
}

input:focus {
  border-color: #4a6ee0;
  outline: none;
}

.captcha-group {
  position: relative;
}

.captcha-wrapper {
  display: flex;
  gap: 10px;
}

.captcha-wrapper img, 
.captcha-loading {
  height: 42px;
  min-width: 100px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f9f9f9;
}

.btn-login {
  width: 100%;
  padding: 12px;
  background-color: #4a6ee0;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-login:hover {
  background-color: #3a5cc5;
}

.btn-login:disabled {
  background-color: #a0aed8;
  cursor: not-allowed;
}

.form-links {
  text-align: center;
  margin-top: 10px;
}

.form-links a {
  color: #4a6ee0;
  text-decoration: none;
  font-size: 14px;
}

.form-links a:hover {
  text-decoration: underline;
}

.error-message {
  color: #e53935;
  font-size: 12px;
  margin-top: 4px;
}

.error-general {
  color: #e53935;
  background-color: rgba(229, 57, 53, 0.1);
  padding: 10px;
  border-radius: 4px;
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
}
</style> 