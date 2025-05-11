package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// 图片验证码记录实体类, 对应数据库 image_captcha 表
@Data
@TableName("image_captcha")
public class CaptchaRecord {

    // 验证码实例的唯一UUID (主键)
    @TableId
    private String id;
    
    // 验证码文本
    private String codeText;
    
    // 验证码图片Base64数据
    private String imageBase64;
    
    // 过期时间
    private LocalDateTime expiresAt;
    
    // 是否已使用：0-否，1-是
    private Boolean isUsed;
    
    // 创建时间
    private LocalDateTime createTime;
} 