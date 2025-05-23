package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单折扣实体类
 */
@Data
@TableName("order_discount")
public class OrderDiscount {
    /**
     * 折扣ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;
    
    /**
     * 管理员ID
     */
    private Long adminId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 