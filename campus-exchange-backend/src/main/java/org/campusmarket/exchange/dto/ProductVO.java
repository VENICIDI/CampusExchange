package org.campusmarket.exchange.dto;

import lombok.Data;
import org.campusmarket.exchange.enums.ProductConditionEnum;
import org.campusmarket.exchange.enums.ProductStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品视图对象
 */
@Data
public class ProductVO {
    
    /**
     * 商品ID
     */
    private Long id;
    
    /**
     * 商家ID
     */
    private Long merchantId;
    
    /**
     * 商家店铺名称
     */
    private String storeName;
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 原价
     */
    private BigDecimal originalPrice;
    
    /**
     * 现价/折扣价
     */
    private BigDecimal currentPrice;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 新旧程度
     */
    private ProductConditionEnum condition;
    
    /**
     * 是否可议价
     */
    private Boolean negotiable;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 销量
     */
    private Integer sales;
    
    /**
     * 商品尺寸
     */
    private String size;
    
    /**
     * 使用说明
     */
    private String usageInstructions;
    
    /**
     * 状态
     */
    private ProductStatusEnum status;
    
    /**
     * 平均评分
     */
    private BigDecimal rating;
    
    /**
     * 好评率
     */
    private BigDecimal positiveRate;
    
    /**
     * 商品主图URL
     */
    private String mainImage;
    
    /**
     * 商品图片URL列表
     */
    private List<String> images;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
} 