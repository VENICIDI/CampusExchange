package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 买家评价实体类（由商家发起），对应数据库 buyer_review_by_merchant 表
 */
@Data
@TableName("buyer_review_by_merchant")
public class BuyerReviewByMerchant {
    
    // 评价ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 订单ID (外键)
    private Long orderId;
    
    // 被评价买家用户ID
    private Long buyerUserId;
    
    // 评价商家ID
    private Long merchantId;
    
    // 评分：1-5星
    private Integer ratingScore;
    
    // 评价内容
    private String content;
    
    // 创建时间
    private LocalDateTime createTime;
} 