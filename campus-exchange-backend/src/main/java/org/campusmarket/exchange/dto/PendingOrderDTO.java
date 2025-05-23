package org.campusmarket.exchange.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 待处理订单DTO
 */
@Data
public class PendingOrderDTO {
    /**
     * 订单ID
     */
    private Long id;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 商家ID
     */
    private Long merchantId;
    
    /**
     * 商家名称
     */
    private String merchantName;
    
    /**
     * 订单商品总金额
     */
    private BigDecimal totalProductAmount;
    
    /**
     * 积分抵扣金额
     */
    private BigDecimal pointsDeductionAmount;
    
    /**
     * 已发放的折扣金额
     */
    private BigDecimal discountAmount;
    
    /**
     * 实际支付金额
     */
    private BigDecimal actualPaymentAmount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 是否已发放折扣
     */
    private Boolean hasDiscount;
} 