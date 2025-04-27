// 用户名验证 (4-20个字符，只允许字母、数字和下划线)
export const validateUsername = (username: string): boolean => {
  const regex = /^[a-zA-Z0-9_]{4,20}$/;
  return regex.test(username);
};

// 密码验证 (6-20个字符)
export const validatePassword = (password: string): boolean => {
  return password.length >= 6 && password.length <= 20;
};

// 手机号验证 (中国大陆手机号格式)
export const validatePhone = (phone: string): boolean => {
  const regex = /^1[3-9]\d{9}$/;
  return regex.test(phone);
};

// 邮箱验证
export const validateEmail = (email: string): boolean => {
  const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  return regex.test(email);
};

// 验证码验证 (通常为4-6位数字或字母)
export const validateCaptcha = (captcha: string): boolean => {
  return captcha.length >= 4 && captcha.length <= 6;
}; 