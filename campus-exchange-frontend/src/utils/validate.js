// 验证用户名
export const validateUsername = (username) => {
  if (!username) return false;
  // 4-20个字符，只能包含字母、数字和下划线
  const usernameRegex = /^[a-zA-Z0-9_]{4,20}$/;
  return usernameRegex.test(username);
};

// 验证密码
export const validatePassword = (password) => {
  if (!password) return false;
  // 6-20个字符，至少包含一个字母和一个数字
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,20}$/;
  return passwordRegex.test(password);
};

// 检查密码强度
export const checkPasswordStrength = (password) => {
  if (!password) return { score: 0, label: '无', suggestions: ['请输入密码'] };
  
  let score = 0;
  const suggestions = [];

  // 长度检查
  if (password.length < 8) {
    suggestions.push('密码长度应至少为8个字符');
  } else {
    score += Math.min(2, Math.floor(password.length / 8));
  }

  // 复杂度检查
  if (/[A-Z]/.test(password)) score += 1;
  else suggestions.push('添加大写字母');
  
  if (/[a-z]/.test(password)) score += 1;
  else suggestions.push('添加小写字母');
  
  if (/\d/.test(password)) score += 1;
  else suggestions.push('添加数字');
  
  if (/[@$!%*#?&]/.test(password)) score += 1;
  else suggestions.push('添加特殊字符(@$!%*#?&)');

  // 评分结果
  let label = '';
  if (score < 2) label = '弱';
  else if (score < 4) label = '中';
  else if (score < 6) label = '强';
  else label = '非常强';

  return { score, label, suggestions };
};

// 验证手机号
export const validatePhone = (phone) => {
  if (!phone) return false;
  // 中国大陆手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/;
  return phoneRegex.test(phone);
};

// 验证邮箱
export const validateEmail = (email) => {
  if (!email) return false;
  // 简单的邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// 验证验证码
export const validateCaptcha = (captcha) => {
  if (!captcha) return false;
  // 验证码通常是4-6位字母数字组合
  const captchaRegex = /^[a-zA-Z0-9]{4,6}$/;
  return captchaRegex.test(captcha);
}; 