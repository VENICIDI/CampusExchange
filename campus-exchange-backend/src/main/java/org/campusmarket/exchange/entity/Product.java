package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableField("condition_desc")
    private ProductConditionEnum productCondition;
    
    // 是否可议价：0-否，1-是
    private Boolean negotiable;
    
    // 库存数量
    private Integer stock;
    
    // 销量
    @TableField("sales_count")
    private Integer sales;
    
    // 商品尺寸
    @TableField("size_info")
    private String size;
    
    // 使用说明
    private String usageInstructions;
    
    // 状态：PENDING_APPROVAL-待审核，REJECTED_RESUBMIT-审核不通过可修改，ON_SALE-在售，LOCKED-已锁定，SOLD_OUT-已售罄，REMOVED_BY_SELLER-商家下架
    private ProductStatusEnum status;
    
    // 平均评分
    @TableField("average_rating")
    private BigDecimal rating;
    
    // 发布时间
    private LocalDateTime publishTime;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
    
    // 获取商品新旧程度（兼容旧代码）
    public ProductConditionEnum getCondition() {
        return this.productCondition;
    }
    
    // 设置商品新旧程度（兼容旧代码）
    public void setCondition(ProductConditionEnum condition) {
        this.productCondition = condition;
    }
    
    // 获取商品销量（兼容旧代码）
    public Integer getSalesCount() {
        return this.sales;
    }
    
    // 设置商品销量（兼容旧代码）
    public void setSalesCount(Integer salesCount) {
        this.sales = salesCount;
    }
    
    // 获取商品尺寸信息（兼容旧代码）
    public String getSizeInfo() {
        return this.size;
    }
    
    // 设置商品尺寸信息（兼容旧代码）
    public void setSizeInfo(String sizeInfo) {
        this.size = sizeInfo;
    }
    
    // 获取平均评分（兼容旧代码）
    public BigDecimal getAverageRating() {
        return this.rating;
    }
    
    // 设置平均评分（兼容旧代码）
    public void setAverageRating(BigDecimal averageRating) {
        this.rating = averageRating;
    }
} 