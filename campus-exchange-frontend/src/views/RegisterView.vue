<template>
  <div class="register-container">
    <div class="register-box">
      <h2>用户注册</h2>
      
      <div v-if="registerSuccess" class="success-message">
        <div class="success-icon">✓</div>
        <h3>注册成功！</h3>
        <p>您的账号需要管理员审核后才能登录，请耐心等待。</p>
        <button class="btn-primary" @click="$router.push('/login')">返回登录</button>
      </div>
      
      <form v-else @submit.prevent="handleRegister" class="register-form">
        <!-- 基本信息部分 -->
        <div class="form-section">
          <h3>基本信息</h3>
          
          <div class="form-group">
            <label for="username">用户名 <span class="required">*</span></label>
            <input
              type="text"
              id="username"
              v-model="registerForm.username"
              placeholder="4-20个字符，只能包含字母、数字和下划线"
              required
            />
            <div class="error-message" v-if="errors.username">{{ errors.username }}</div>
          </div>
          
          <div class="form-group">
            <label for="password">密码 <span class="required">*</span></label>
            <input
              type="password"
              id="password"
              v-model="registerForm.password"
              placeholder="6-20个字符"
              required
            />
            <div class="error-message" v-if="errors.password">{{ errors.password }}</div>
          </div>
          
          <div class="form-group">
            <label for="confirmPassword">确认密码 <span class="required">*</span></label>
            <input
              type="password"
              id="confirmPassword"
              v-model="registerForm.confirmPassword"
              placeholder="请再次输入密码"
              required
            />
            <div class="error-message" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</div>
          </div>
          
          <div class="form-group">
            <label for="phone">手机号</label>
            <input
              type="text"
              id="phone"
              v-model="registerForm.phone"
              placeholder="请输入手机号"
            />
            <div class="error-message" v-if="errors.phone">{{ errors.phone }}</div>
          </div>
          
          <div class="form-group">
            <label for="email">邮箱</label>
            <input
              type="email"
              id="email"
              v-model="registerForm.email"
              placeholder="请输入邮箱"
            />
            <div class="error-message" v-if="errors.email">{{ errors.email }}</div>
          </div>
        </div>
        
        <!-- 附加信息部分 -->
        <div class="form-section">
          <h3>附加信息</h3>
          
          <div class="form-group">
            <label for="realName">真实姓名</label>
            <input
              type="text"
              id="realName"
              v-model="registerForm.realName"
              placeholder="请输入真实姓名"
            />
          </div>
          
          <div class="form-group">
            <label for="city">城市</label>
            <input
              type="text"
              id="city"
              v-model="registerForm.city"
              placeholder="请输入所在城市"
            />
          </div>
          
          <div class="form-group">
            <label>性别</label>
            <div class="radio-group">
              <label class="radio-label">
                <input type="radio" v-model="registerForm.gender" value="MALE" />
                男
              </label>
              <label class="radio-label">
                <input type="radio" v-model="registerForm.gender" value="FEMALE" />
                女
              </label>
            </div>
          </div>
          
          <div class="form-group">
            <label for="personalIntro">个人简介</label>
            <textarea
              id="personalIntro"
              v-model="registerForm.personalIntro"
              rows="3"
              placeholder="请输入个人简介"
            ></textarea>
          </div>
        </div>
        
        <!-- 商家信息部分 -->
        <div class="form-section">
          <div class="merchant-toggle">
            <label class="checkbox-label">
              <input type="checkbox" v-model="registerForm.isMerchant" />
              我想成为商家
            </label>
          </div>
          
          <div v-if="registerForm.isMerchant" class="merchant-fields">
            <div class="form-group">
              <label for="shopName">店铺名称 <span class="required">*</span></label>
              <input
                type="text"
                id="shopName"
                v-model="registerForm.shopName"
                placeholder="请输入店铺名称"
                :required="registerForm.isMerchant"
              />
              <div class="error-message" v-if="errors.shopName">{{ errors.shopName }}</div>
            </div>
            
            <div class="form-group">
              <label for="shopAddress">店铺地址</label>
              <input
                type="text"
                id="shopAddress"
                v-model="registerForm.shopAddress"
                placeholder="请输入店铺地址"
              />
            </div>
            
            <div class="form-group">
              <label for="shopIntro">店铺简介</label>
              <textarea
                id="shopIntro"
                v-model="registerForm.shopIntro"
                rows="3"
                placeholder="请输入店铺简介"
              ></textarea>
            </div>
          </div>
        </div>
        
        <!-- 验证码部分 -->
        <div class="form-section">
          <div class="form-group captcha-group">
            <label for="captcha">验证码 <span class="required">*</span></label>
            <div class="captcha-wrapper">
              <input
                type="text"
                id="captcha"
                v-model="registerForm.captcha"
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
              <div v-else class="captcha-loading" @click="refreshCaptcha">加载中...</div>
            </div>
            <div class="error-message" v-if="errors.captcha">{{ errors.captcha }}</div>
          </div>
        </div>
        
        <!-- 表单操作按钮 -->
        <div class="form-actions">
          <div class="error-message general-error" v-if="errors.general">{{ errors.general }}</div>
          
          <button type="submit" class="btn-register" :disabled="isSubmitting">
            {{ isSubmitting ? '注册中...' : '立即注册' }}
          </button>
          
          <div class="form-links">
            <router-link to="/login">已有账号？返回登录</router-link>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { authApi, captchaApi } from '@/services/api';
import { validateUsername, validatePassword, validatePhone, validateEmail, validateCaptcha } from '@/utils/validate';

const router = useRouter();

// 注册表单数据
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  city: '',
  gender: '',
  personalIntro: '',
  wechat: '',
  isMerchant: false,
  shopName: '',
  shopAddress: '',
  shopIntro: '',
  captcha: '',
  captchaKey: '' // 存储验证码ID
});

// 错误信息
const errors = ref({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  shopName: '',
  captcha: '',
  general: ''
});

// 加载和成功状态
const isSubmitting = ref(false);
const registerSuccess = ref(false);
const captchaImageUrl = ref('');

// 获取验证码
const fetchCaptcha = async () => {
  try {
    const response = await captchaApi.getCaptchaId();
    registerForm.value.captchaKey = response.data.message;
    captchaImageUrl.value = captchaApi.getCaptchaImageUrl(registerForm.value.captchaKey);
    console.log('验证码ID:', registerForm.value.captchaKey);
    console.log('验证码图片URL:', captchaImageUrl.value);
  } catch (error) {
    console.error('获取验证码失败:', error);
    errors.value.general = '获取验证码失败，请刷新页面重试';
  }
};

// 刷新验证码
const refreshCaptcha = () => {
  captchaImageUrl.value = '';
  registerForm.value.captcha = '';
  fetchCaptcha();
};

// 处理验证码图片加载失败
const handleCaptchaError = (e: Event) => {
  const target = e.target as HTMLImageElement;
  console.error('验证码图片加载失败:', {
    error: e,
    url: target.src,
    captchaId: registerForm.value.captchaKey,
    captchaImageUrl: captchaImageUrl.value
  });
  
  // 设置短暂延迟后重试
  setTimeout(() => {
    refreshCaptcha();
  }, 1500);
};

// 表单验证
const validateForm = () => {
  let isValid = true;
  
  // 重置错误信息
  errors.value = {
    username: '',
    password: '',
    confirmPassword: '',
    phone: '',
    email: '',
    shopName: '',
    captcha: '',
    general: ''
  };

  // 验证用户名
  if (!validateUsername(registerForm.value.username)) {
    errors.value.username = '用户名必须是4-20个字符，只能包含字母、数字和下划线';
    isValid = false;
  }

  // 验证密码
  if (!validatePassword(registerForm.value.password)) {
    errors.value.password = '密码长度必须在6-20个字符之间';
    isValid = false;
  }

  // 验证确认密码
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    errors.value.confirmPassword = '两次输入的密码不一致';
    isValid = false;
  }

  // 验证手机号(如果提供)
  if (registerForm.value.phone && !validatePhone(registerForm.value.phone)) {
    errors.value.phone = '手机号格式不正确';
    isValid = false;
  }

  // 验证邮箱(如果提供)
  if (registerForm.value.email && !validateEmail(registerForm.value.email)) {
    errors.value.email = '邮箱格式不正确';
    isValid = false;
  }

  // 验证商家信息(如果是商家)
  if (registerForm.value.isMerchant && !registerForm.value.shopName.trim()) {
    errors.value.shopName = '店铺名称不能为空';
    isValid = false;
  }

  // 验证验证码
  if (!validateCaptcha(registerForm.value.captcha)) {
    errors.value.captcha = '验证码格式不正确';
    isValid = false;
  }

  return isValid;
};

// 注册提交
const handleRegister = async () => {
  if (!validateForm() || isSubmitting.value) {
    return;
  }

  isSubmitting.value = true;
  errors.value.general = '';

  try {
    const response = await authApi.register(registerForm.value);
    registerSuccess.value = true;
  } catch (error: any) {
    console.error('注册失败:', error);
    
    if (error.response?.data?.message) {
      errors.value.general = error.response.data.message;
    } else {
      errors.value.general = '注册失败，请检查表单信息';
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
.register-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 60px 20px;
  background-color: #f5f5f5;
}

.register-box {
  width: 100%;
  max-width: 600px;
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

h3 {
  margin-bottom: 15px;
  color: #444;
  font-size: 18px;
  font-weight: 500;
}

.form-section {
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.form-section:last-of-type {
  border-bottom: none;
}

.register-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

.required {
  color: #e53935;
}

input, textarea {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border 0.3s;
}

input:focus, textarea:focus {
  border-color: #4a6ee0;
  outline: none;
}

textarea {
  resize: vertical;
  min-height: 80px;
}

.radio-group {
  display: flex;
  gap: 20px;
}

.radio-label, .checkbox-label {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.merchant-toggle {
  margin-bottom: 15px;
}

.merchant-fields {
  padding-top: 15px;
  border-top: 1px dashed #ddd;
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

.btn-register, .btn-primary {
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

.btn-register:hover, .btn-primary:hover {
  background-color: #3a5cc5;
}

.btn-register:disabled {
  background-color: #a0aed8;
  cursor: not-allowed;
}

.form-links {
  text-align: center;
  margin-top: 15px;
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

.general-error {
  text-align: center;
  margin-bottom: 15px;
  font-size: 14px;
}

.success-message {
  text-align: center;
  padding: 30px 20px;
}

.success-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 20px;
  background-color: #4caf50;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

.success-message h3 {
  margin-bottom: 10px;
  color: #4caf50;
}

.success-message p {
  margin-bottom: 20px;
  color: #666;
}
</style> 