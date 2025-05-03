package org.campusmarket.exchange.service.impl;

import com.google.code.kaptcha.Producer;
import jakarta.annotation.Resource;
import org.campusmarket.exchange.entity.CaptchaRecord;
import org.campusmarket.exchange.mapper.CaptchaRecordMapper;
import org.campusmarket.exchange.service.ICaptchaService;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.UUID;

// 验证码服务实现类
@Service
public class CaptchaServiceImpl implements ICaptchaService {
    
    @Resource
    private Producer kaptchaProducer;
    
    @Resource
    private CaptchaRecordMapper captchaRecordMapper;
    
    // 验证码有效期（分钟）
    private static final int CAPTCHA_EXPIRE_MINUTES = 10;
    
    @Override
    public String generateCaptcha() {
        // 生成随机验证码
        String captchaCode = kaptchaProducer.createText();
        
        // 生成验证码ID
        String captchaId = UUID.randomUUID().toString();
        
        // 生成验证码图片的Base64编码
        BufferedImage image = kaptchaProducer.createImage(captchaCode);
        String base64Image = ""; // 此处实际应转换图片为Base64字符串，简化实现
        
        // 存储验证码记录
        CaptchaRecord captchaRecord = new CaptchaRecord();
        captchaRecord.setCaptchaId(captchaId);
        captchaRecord.setCaptchaCode(captchaCode);
        captchaRecord.setCaptchaImage(base64Image);
        captchaRecord.setExpiredTime(LocalDateTime.now().plusMinutes(CAPTCHA_EXPIRE_MINUTES));
        captchaRecord.setIsUsed(false);
        
        captchaRecordMapper.insert(captchaRecord);
        
        return captchaId;
    }
    
    @Override
    public BufferedImage getCaptchaImage(String captchaId) {
        CaptchaRecord record = captchaRecordMapper.selectByCaptchaId(captchaId);
        if (record == null) {
            return null;
        }
        
        // 验证码过期或已使用
        if (record.getExpiredTime().isBefore(LocalDateTime.now()) || Boolean.TRUE.equals(record.getIsUsed())) {
            return null;
        }
        
        return kaptchaProducer.createImage(record.getCaptchaCode());
    }
    
    @Override
    public boolean validateCaptcha(String captchaId, String captchaCode) {
        // 添加调试日志
        System.out.println("验证码校验: captchaId=" + captchaId + ", captchaCode=" + captchaCode);
        
        if (captchaId == null || captchaCode == null) {
            System.out.println("验证码ID或验证码为空");
            return false;
        }
        
        CaptchaRecord record = captchaRecordMapper.selectByCaptchaId(captchaId);
        if (record == null) {
            System.out.println("未找到验证码记录: " + captchaId);
            return false;
        }
        
        // 验证码过期或已使用
        if (record.getExpiredTime().isBefore(LocalDateTime.now()) || Boolean.TRUE.equals(record.getIsUsed())) {
            System.out.println("验证码已过期或已使用: 过期时间=" + record.getExpiredTime() + ", 是否已使用=" + record.getIsUsed());
            return false;
        }
        
        // 验证码不区分大小写
        boolean isValid = captchaCode.equalsIgnoreCase(record.getCaptchaCode());
        System.out.println("验证码比对: 输入=" + captchaCode + ", 实际=" + record.getCaptchaCode() + ", 是否有效=" + isValid);
        
        if (isValid) {
            // 标记验证码已使用
            record.setIsUsed(true);
            captchaRecordMapper.updateById(record);
        }
        
        return isValid;
    }
} 