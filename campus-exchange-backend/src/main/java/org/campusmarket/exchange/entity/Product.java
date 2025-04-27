package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.ProductConditionEnum;
import org.campusmarket.exchange.enums.ProductStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 商品实体类, 对应数据库 product 表
@Data
@TableName("product")
public class Product {

    // 商品ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 商家ID (外键)
    private Long merchantId;
    
    // 商品名称
    private String name;
    
    // 分类ID (外键)
    private Long categoryId;
    
    // 原价
    private BigDecimal originalPrice;
    
    // 现价/折扣价
    private BigDecimal currentPrice;
    
    // 商品描述
    private String description;
    
    // 新旧程度：NEW-全新，LIKE_NEW-九成新，GOOD-八成新，FAIR-七成新，POOR-六成新及以下
    private ProductConditionEnum condition;
    
    // 是否可议价：0-否，1-是
    private Boolean negotiable;
    
    // 库存数量
    private Integer stock;
    
    // 销量
    private Integer sales;
    
    // 商品尺寸
    private String size;
    
    // 使用说明
    private String usageInstructions;
    
    // 状态：PENDING-待审核，ON_SALE-在售，LOCKED-已锁定，SOLD-已售出，REMOVED-已下架
    private ProductStatusEnum status;
    
    // 平均评分
    private BigDecimal rating;
    
    // 好评率
    private BigDecimal positiveRate;
    
    // 发布时间
    private LocalDateTime publishTime;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 