package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// 验证码记录实体类, 对应数据库 captcha_record 表
@Data
@TableName("captcha_record")
public class CaptchaRecord {

    // ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 验证码实例的唯一UUID
    private String captchaId;
    
    // 验证码
    private String captchaCode;
    
    // 验证码图片Base64
    private String captchaImage;
    
    // 过期时间
    private LocalDateTime expiredTime;
    
    // 是否已使用：0-否，1-是
    private Boolean isUsed;
    
    // 创建时间
    private LocalDateTime createTime;
} 