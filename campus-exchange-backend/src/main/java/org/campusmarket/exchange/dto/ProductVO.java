package org.campusmarket.exchange.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.campusmarket.exchange.enums.ProductConditionEnum;
import org.campusmarket.exchange.enums.ProductStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 商品视图对象
@Data
public class ProductVO {
    
    // 商品ID
    private Long id;
    
    // 商家ID
    private Long merchantId;
    
    // 商家店铺名称
    private String storeName;
    
    // 商品名称
    private String name;
    
    // 分类ID
    private Long categoryId;
    
    // 分类名称
    private String categoryName;
    
    // 原价
    private BigDecimal originalPrice;
    
    // 现价/折扣价
    private BigDecimal currentPrice;
    
    // 商品描述
    private String description;
    
    // 新旧程度
    private ProductConditionEnum productCondition;
    
    // 是否可议价
    private Boolean negotiable;
    
    // 库存数量
    private Integer stock;
    
    // 销量
    private Integer salesCount;
    
    // 商品尺寸
    private String sizeInfo;
    
    // 使用说明
    private String usageInstructions;
    
    // 状态
    private ProductStatusEnum status;
    
    // 平均评分
    private BigDecimal averageRating;
    
    // 商品主图URL
    private String mainImage;
    
    // 商品图片URL列表
    private List<String> images;
    
    // 兼容字段：商品图片URL列表
    private List<String> imageUrls;
    
    // 发布时间
    private LocalDateTime publishTime;
    
    // 获取平均评分 (兼容旧代码)
    public BigDecimal getRating() {
        return this.averageRating;
    }
    
    // 设置平均评分 (兼容旧代码)
    public void setRating(BigDecimal rating) {
        this.averageRating = rating;
    }
    
    // 获取销量 (兼容旧代码)
    public Integer getSales() {
        return this.salesCount;
    }
    
    // 设置销量 (兼容旧代码)
    public void setSales(Integer sales) {
        this.salesCount = sales;
    }
    
    // 获取商品尺寸 (兼容旧代码)
    public String getSize() {
        return this.sizeInfo;
    }
    
    // 设置商品尺寸 (兼容旧代码)
    public void setSize(String size) {
        this.sizeInfo = size;
    }
    
    // 获取商品新旧程度 (兼容旧代码)
    public ProductConditionEnum getCondition() {
        return this.productCondition;
    }
    
    // 设置商品新旧程度 (兼容旧代码)
    public void setCondition(ProductConditionEnum condition) {
        this.productCondition = condition;
    }
    
    // 获取商品主图 (兼容旧代码)
    public String getCoverImage() {
        return this.mainImage;
    }
    
    // 设置商品主图 (兼容旧代码)
    public void setCoverImage(String coverImage) {
        this.mainImage = coverImage;
    }
    
    // 获取商品图片列表 (兼容旧代码)
    public List<String> getImages() {
        return this.imageUrls != null ? this.imageUrls : this.images;
    }
    
    // 设置商品图片列表 (兼容旧代码)
    public void setImages(List<String> images) {
        this.images = images;
        this.imageUrls = images;
    }
} 