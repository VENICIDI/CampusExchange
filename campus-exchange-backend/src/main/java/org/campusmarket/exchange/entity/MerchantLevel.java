package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家等级实体类，对应数据库 merchant_level 表
 */
@Data
@TableName("merchant_level")
public class MerchantLevel {

    // 等级ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 等级名称
    private String levelName;
    
    // 手续费率
    private BigDecimal commissionRate;
    
    // 等级描述
    private String description;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 