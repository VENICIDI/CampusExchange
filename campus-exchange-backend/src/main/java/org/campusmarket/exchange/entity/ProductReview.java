package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评价实体类，对应数据库 product_review 表
 */
@Data
@TableName("product_review")
public class ProductReview {
    
    // 评价ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 订单项ID (外键, 唯一确定评价对象)
    private Long orderItemId;
    
    // 评价用户ID (买家)
    private Long userId;
    
    // 商品ID (冗余)
    private Long productId;
    
    // 商家ID (冗余)
    private Long merchantId;
    
    // 评分：1-5星
    private Integer ratingScore;
    
    // 评价内容
    private String content;
    
    // 创建时间
    private LocalDateTime createTime;
} 