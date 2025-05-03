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
              placeholder="6-20个字符，至少包含字母和数字"
              required
              @input="updatePasswordStrength"
            />
            <div class="password-strength" v-if="passwordStrength.score > 0">
              <div 
                class="strength-meter"
              >
                <div 
                  class="strength-meter-fill" 
                  :style="{ width: `${passwordStrength.score * 16.67}%`, backgroundColor: strengthColor }"
                ></div>
              </div>
              <span class="strength-text">强度: {{ passwordStrength.label }}</span>
              <div class="strength-suggestions" v-if="passwordStrength.suggestions.length > 0">
                <ul>
                  <li v-for="(suggestion, index) in passwordStrength.suggestions" :key="index">
                    {{ suggestion }}
                  </li>
                </ul>
              </div>
            </div>
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
        <div class="form-section merchant-section">
          <div class="merchant-toggle">
            <label class="checkbox-label">
              <div class="custom-checkbox">
                <input type="checkbox" v-model="registerForm.isMerchant" />
                <span class="checkbox-icon"></span>
              </div>
              <span class="merchant-label">我想成为商家</span>
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
                rows="2"
                placeholder="请输入店铺简介"
              ></textarea>
            </div>
          </div>
        </div>
        
        <!-- 验证码部分 -->
        <div class="form-section captcha-section">
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
            <div class="error-message" v-if="errors.captcha">{{ errors.captcha }}</div>
          </div>
        </div>
        
        <!-- 表单操作按钮 -->
        <div class="form-actions">
          <div class="error-message general-error" v-if="errors.general">{{ errors.general }}</div>
          
          <button type="submit" class="btn-register" :disabled="isSubmitting">
            <span class="btn-text">{{ isSubmitting ? '注册中...' : '立即注册' }}</span>
            <span class="btn-icon" v-if="!isSubmitting">→</span>
            <span class="btn-loading" v-else></span>
          </button>
          
          <div class="form-links">
            <router-link to="/login" class="login-link">已有账号？返回登录</router-link>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { authApi, captchaApi } from '@/api/all';
import { validateUsername, validatePassword, validatePhone, validateEmail, validateCaptcha, checkPasswordStrength } from '@/utils/validate';

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
  captcha: '',          // 验证码值
  captchaKey: ''        // 验证码ID，与后端UserRegisterDTO字段对应
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
const isCaptchaLoading = ref(false);

// 密码强度相关
const passwordStrength = ref({ score: 0, label: '', suggestions: [] });

const strengthColor = computed(() => {
  const score = passwordStrength.value.score;
  if (score < 2) return '#ff4d4f'; // 红色 - 弱
  if (score < 4) return '#faad14'; // 黄色 - 中
  if (score < 6) return '#52c41a'; // 绿色 - 强
  return '#1890ff'; // 蓝色 - 非常强
});

const updatePasswordStrength = () => {
  passwordStrength.value = checkPasswordStrength(registerForm.value.password);
};

// 获取验证码
const fetchCaptcha = async () => {
  try {
    errors.value.captcha = '';
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
          registerForm.value.captchaKey = captchaId;
          captchaImageUrl.value = captchaApi.getCaptchaImageUrl(captchaId);
          console.log('验证码图片URL:', captchaImageUrl.value);
        } else {
          console.error('未能从响应中提取验证码ID');
          errors.value.captcha = '获取验证码失败，请点击刷新重试';
        }
      } else {
        console.error('验证码API返回错误:', response.data);
        errors.value.captcha = response.data.message || '获取验证码失败，请点击刷新重试';
      }
    } else {
      console.error('验证码API响应格式无效:', response);
      errors.value.captcha = '获取验证码失败，请点击刷新重试';
    }
  } catch (error) {
    console.error('获取验证码失败:', error);
    errors.value.captcha = '获取验证码失败，请点击刷新重试';
  } finally {
    isCaptchaLoading.value = false;
  }
};

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    console.log('刷新验证码函数被调用');
    
    // 清空当前验证码
    captchaImageUrl.value = '';
    registerForm.value.captcha = '';
    isCaptchaLoading.value = true;
    
    console.log('验证码状态重置完成，准备获取新验证码');
    
    // 直接调用API获取新验证码
    const response = await captchaApi.getCaptchaId();
    console.log('获取新验证码响应:', response);
    
    if (response && response.data && response.data.code === 200) {
      let captchaId = null;
      
      // 从response中提取captchaId
      if (response.data.data) {
        captchaId = response.data.data;
      } else if (response.data.message) {
        captchaId = response.data.message;
      }
      
      console.log('新验证码ID:', captchaId);
      
      if (captchaId) {
        // 更新验证码ID和图片URL
        registerForm.value.captchaKey = captchaId;
        captchaImageUrl.value = captchaApi.getCaptchaImageUrl(captchaId);
        console.log('验证码图片URL已更新:', captchaImageUrl.value);
      } else {
        console.error('无法获取新的验证码ID');
      }
    } else {
      console.error('获取新验证码失败:', response);
    }
  } catch (error) {
    console.error('刷新验证码出错:', error);
  } finally {
    console.log('验证码刷新流程结束');
    isCaptchaLoading.value = false;
  }
};

// 处理验证码加载失败
const handleCaptchaError = (e) => {
  const target = e.target;
  console.error('验证码图片加载失败:', {
    error: e,
    url: target.src,
    captchaId: registerForm.value.captchaKey,
    captchaImageUrl: captchaImageUrl.value
  });
  
  errors.value.captcha = "验证码加载失败，请点击刷新";
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
    errors.value.password = '密码必须是6-20个字符，至少包含一个字母和一个数字';
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
  errors.value.captcha = ''; // 清除之前的验证码错误

  try {
    // 创建一个准确匹配后端UserRegisterDTO格式的请求数据对象
    const requestData = {
      // 必填字段
      username: registerForm.value.username,
      password: registerForm.value.password,
      confirmPassword: registerForm.value.confirmPassword,
      captcha: registerForm.value.captcha,
      captchaKey: registerForm.value.captchaKey,
      
      // 选填字段
      realName: registerForm.value.realName || '',
      phone: registerForm.value.phone || '',
      email: registerForm.value.email || '',
      city: registerForm.value.city || '',
      gender: registerForm.value.gender || null, // 使用枚举类型对应的字符串
      personalIntro: registerForm.value.personalIntro || '',
      wechat: registerForm.value.wechat || '',
      
      // 商家相关信息
      isMerchant: registerForm.value.isMerchant === true,
      shopName: registerForm.value.isMerchant ? (registerForm.value.shopName || '') : '',
      shopAddress: registerForm.value.isMerchant ? (registerForm.value.shopAddress || '') : '',
      shopIntro: registerForm.value.isMerchant ? (registerForm.value.shopIntro || '') : ''
    };

    console.log('提交注册数据:', {
      ...requestData,
      password: '******', // 隐藏密码
      confirmPassword: '******' // 隐藏确认密码
    });

    const response = await authApi.register(requestData);
    
    console.log('注册成功，响应数据:', response.data);
    
    // 设置注册成功状态
    registerSuccess.value = true;
    
    // 清除表单数据
    registerForm.value = {
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
      captchaKey: ''
    };
  } catch (error) {
    console.error('注册失败:', error);
    
    // 增强错误处理逻辑
    if (error.response) {
      const { data, status } = error.response;
      
      console.log('服务器响应状态:', status);
      console.log('服务器响应数据:', data);
      
      // 直接处理验证码错误 - 根据后端日志，验证码错误返回的是 code=400
      if (data && status === 400 && data.code === 400 && data.message) {
        console.log('检测到错误响应:', data.message);
        
        // 判断是否与验证码相关
        if (data.message.includes('验证码')) {
          console.log('捕获到验证码错误:', data.message);
          
          // 设置错误信息
          errors.value.captcha = data.message;
          
          // 异步刷新验证码
          console.log('开始刷新验证码...');
          (async () => {
            try {
              console.log('执行异步验证码刷新');
              await refreshCaptcha();
              console.log('验证码刷新完成');
            } catch (e) {
              console.error('刷新验证码时出错:', e);
            }
          })();
          
          return;
        }
      }
      
      // 特定错误码处理
      if (data && status === 400) {
        if (data.code === 4001) {
          errors.value.username = '用户名已存在';
          return;
        } else if (data.code === 4002) {
          errors.value.phone = '手机号已被注册';
          return;
        } else if (data.code === 4003) {
          errors.value.email = '邮箱已被注册';
          return;
        } else if (data.code === 4004) {
          // 设置错误信息
          errors.value.captcha = '验证码错误或已过期';
          
          // 异步刷新验证码
          console.log('开始刷新验证码...');
          (async () => {
            try {
              console.log('执行异步验证码刷新');
              await refreshCaptcha();
              console.log('验证码刷新完成');
            } catch (e) {
              console.error('刷新验证码时出错:', e);
            }
          })();
          
          return;
        }
      }
      
      // 处理标准Result格式返回的错误
      if (data && data.message) {
        errors.value.general = data.message;
      }
      // 处理字段验证错误
      else if (data && data.errors && Array.isArray(data.errors)) {
        // 后端返回的字段验证错误列表
        errors.value.general = data.errors.map(err => err.message).join(', ');
      } 
      // 没有明确错误信息时提供通用错误信息
      else {
        if (status === 400) {
          errors.value.general = '请求参数错误，请检查输入';
        } else if (status === 409) {
          errors.value.general = '用户名、手机号或邮箱已存在';
        } else if (status >= 500) {
          errors.value.general = '服务器错误，请稍后重试';
        } else {
          errors.value.general = '注册失败，请稍后重试';
        }
      }
    } else if (error.request) {
      // 请求发出但没有收到响应
      errors.value.general = '服务器无响应，请检查网络连接';
    } else {
      // 请求设置时出现错误
      errors.value.general = '请求错误: ' + error.message;
    }
    
    // 刷新验证码，除非是验证码错误（已经在错误处理中刷新过）
    if (!errors.value.captcha) {
      refreshCaptcha();
    }
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
  min-height: 100vh;
}

.register-box {
  width: 100%;
  max-width: 600px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 35px;
  margin-bottom: 40px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-weight: 600;
  position: relative;
  padding-bottom: 15px;
}

h2::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #4a6ee0, #6a8fff);
  border-radius: 3px;
}

h3 {
  margin-bottom: 20px;
  color: #444;
  font-size: 18px;
  font-weight: 500;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 25px;
  border-bottom: 1px solid #eee;
}

.form-section:last-of-type {
  border-bottom: none;
  margin-bottom: 15px;
}

.register-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

.required {
  color: #e53935;
  margin-left: 4px;
}

input, textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

input:focus, textarea:focus {
  border-color: #4a6ee0;
  outline: none;
  box-shadow: 0 0 0 3px rgba(74, 110, 224, 0.1);
}

textarea {
  resize: vertical;
  min-height: 90px;
}

.radio-group {
  display: flex;
  gap: 25px;
  margin-top: 5px;
}

.radio-label, .checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.merchant-section {
  margin-top: 20px;
  margin-bottom: 20px;
  padding: 0;
  border-bottom: none;
}

.merchant-toggle {
  margin-bottom: 15px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  padding: 10px 15px;
  background-color: #f0f4ff;
  border-radius: 8px;
  border: 1px solid #e0e6f7;
  transition: all 0.3s ease;
}

.checkbox-label:hover {
  background-color: #e6ecff;
}

.custom-checkbox {
  position: relative;
  width: 20px;
  height: 20px;
  margin-right: 10px;
}

.custom-checkbox input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.checkbox-icon {
  position: absolute;
  top: 0;
  left: 0;
  width: 20px;
  height: 20px;
  background-color: white;
  border: 2px solid #4a6ee0;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.custom-checkbox input:checked + .checkbox-icon {
  background-color: #4a6ee0;
}

.custom-checkbox input:checked + .checkbox-icon:after {
  content: "";
  position: absolute;
  left: 6px;
  top: 2px;
  width: 5px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.merchant-label {
  font-weight: 600;
  color: #4a6ee0;
  font-size: 15px;
}

.merchant-fields {
  padding: 20px;
  background-color: #f9faff;
  border-radius: 10px;
  border: 1px solid #e0e6f7;
  box-shadow: 0 2px 8px rgba(74, 110, 224, 0.1);
  margin-top: 10px;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.captcha-section {
  margin-bottom: 20px;
  border-bottom: none;
  padding-bottom: 15px;
}

.captcha-group {
  position: relative;
  margin-bottom: 10px;
}

.captcha-wrapper {
  display: flex;
  gap: 15px;
  align-items: center;
}

.captcha-wrapper input {
  flex: 1;
}

.captcha-wrapper img, 
.captcha-loading {
  height: 46px;
  min-width: 120px;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f9f9f9;
  transition: all 0.3s;
  font-size: 13px;
  color: #666;
}

.captcha-wrapper img:hover,
.captcha-loading:hover {
  border-color: #4a6ee0;
  box-shadow: 0 0 0 3px rgba(74, 110, 224, 0.1);
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
  font-size: 12px;
  margin-top: 6px;
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
  margin-top: 10px;
}

.btn-register, .btn-primary {
  width: 100%;
  padding: 0;
  background: linear-gradient(135deg, #4a6ee0, #5a7ef2);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 10px rgba(74, 110, 224, 0.25);
  height: 52px;
  position: relative;
  overflow: hidden;
}

.btn-register:hover, .btn-primary:hover {
  background: linear-gradient(135deg, #3d5eca, #4a6ee0);
  box-shadow: 0 6px 15px rgba(74, 110, 224, 0.35);
  transform: translateY(-2px);
}

.btn-register:active, .btn-primary:active {
  transform: translateY(0);
  box-shadow: 0 2px 5px rgba(74, 110, 224, 0.3);
}

.btn-register:disabled {
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

.btn-register:hover .btn-icon {
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
  margin-top: 20px;
}

.login-link {
  color: #4a6ee0;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s;
  padding: 5px 10px;
  border-radius: 4px;
  background-color: transparent;
}

.login-link:hover {
  color: #304b99;
  text-decoration: none;
  background-color: rgba(74, 110, 224, 0.05);
}

.success-message {
  text-align: center;
  padding: 40px 20px;
}

.success-icon {
  width: 70px;
  height: 70px;
  margin: 0 auto 25px;
  background-color: #4caf50;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 35px;
  box-shadow: 0 6px 15px rgba(76, 175, 80, 0.3);
}

.success-message h3 {
  margin-bottom: 15px;
  color: #4caf50;
  font-size: 22px;
}

.success-message p {
  margin-bottom: 25px;
  color: #666;
  font-size: 16px;
  line-height: 1.6;
}

.password-strength {
  margin-top: 8px;
  margin-bottom: 12px;
}

.strength-meter {
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.strength-meter-fill {
  height: 100%;
  transition: width 0.3s ease, background-color 0.3s ease;
}

.strength-text {
  font-size: 13px;
  color: #555;
  margin-left: 2px;
}

.strength-suggestions {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
  background-color: #f9f9f9;
  padding: 8px 12px;
  border-radius: 6px;
}

.strength-suggestions ul {
  list-style-type: disc;
  padding-left: 20px;
  margin: 5px 0 0;
}

.strength-suggestions li {
  margin-bottom: 3px;
}

@media (max-width: 640px) {
  .register-box {
    padding: 25px 20px;
  }
  
  .radio-group {
    flex-direction: column;
    gap: 10px;
  }
  
  .captcha-wrapper {
    flex-direction: column;
    align-items: stretch;
  }
  
  .captcha-wrapper img, 
  .captcha-loading {
    height: 50px;
    width: 100%;
    margin-top: 10px;
  }
}

.general-error {
  text-align: center;
  margin-bottom: 20px;
  font-size: 14px;
  padding: 12px;
  background-color: rgba(229, 57, 53, 0.08);
  border-radius: 8px;
  border-left: 3px solid #e53935;
  display: flex;
  align-items: center;
  justify-content: center;
}

.general-error:before {
  content: "⚠️";
  font-size: 14px;
  margin-right: 8px;
}
</style> 