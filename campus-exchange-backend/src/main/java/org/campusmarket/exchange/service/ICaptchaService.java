package org.campusmarket.exchange.service;

import java.awt.image.BufferedImage;

// 验证码服务接口
public interface ICaptchaService {
    
    // 生成验证码并返回验证码ID
    String generateCaptcha();
    
    // 根据验证码ID获取验证码图片
    BufferedImage getCaptchaImage(String captchaId);
    
    // 验证验证码是否正确
    boolean validateCaptcha(String captchaId, String captchaCode);
} 