package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家服务评价实体类，对应数据库 merchant_service_review 表
 */
@Data
@TableName("merchant_service_review")
public class MerchantServiceReview {
    
    // 评价ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 订单ID (外键)
    private Long orderId;
    
    // 评价用户ID (买家)
    private Long userId;
    
    // 被评价商家ID
    private Long merchantId;
    
    // 服务态度评分：1-5星
    private Integer serviceAttitudeRating;
    
    // 评价内容
    private String content;
    
    // 创建时间
    private LocalDateTime createTime;
} 