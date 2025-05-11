package org.campusmarket.exchange.dto;

import lombok.Data;
import org.campusmarket.exchange.entity.OrderAddress;
import org.campusmarket.exchange.entity.OrderItem;
import org.campusmarket.exchange.entity.OrderLog;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.enums.TradeTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 */
@Data
public class OrderDetailVO {
    
    /**
     * 订单ID
     */
    private Long id;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 用户ID (买家)
     */
    private Long userId;
    
    /**
     * 用户名 (买家)
     */
    private String username;
    
    /**
     * 商家ID
     */
    private Long merchantId;
    
    /**
     * 商家店铺名
     */
    private String storeName;
    
    /**
     * 商品总金额
     */
    private BigDecimal totalProductAmount;
    
    /**
     * 使用的积分数量
     */
    private Integer pointsUsed;
    
    /**
     * 积分抵扣金额
     */
    private BigDecimal pointsDeductionAmount;
    
    /**
     * 实际支付金额
     */
    private BigDecimal actualPaymentAmount;
    
    /**
     * 平台手续费
     */
    private BigDecimal platformCommissionAmount;
    
    /**
     * 订单状态
     */
    private OrderStatusEnum status;
    
    /**
     * 交易方式
     */
    private TradeTypeEnum tradeType;
    
    /**
     * 线下交易地点
     */
    private String offlineMeetingLocation;
    
    /**
     * 线下交易时间
     */
    private LocalDateTime offlineMeetingTime;
    
    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 发货时间
     */
    private LocalDateTime shippingTime;
    
    /**
     * 收货确认时间
     */
    private LocalDateTime receiptConfirmationTime;
    
    /**
     * 订单完成时间
     */
    private LocalDateTime completionTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 订单项列表
     */
    private List<OrderItem> orderItems;
    
    /**
     * 收货地址信息
     */
    private OrderAddress orderAddress;
    
    /**
     * 订单状态日志
     */
    private List<OrderLog> orderLogs;
    
    /**
     * 订单状态的中文描述
     */
    private String statusDesc;
    
    /**
     * 交易方式的中文描述
     */
    private String tradeTypeDesc;
    
    /**
     * 获取订单状态的中文描述
     */
    public String getStatusDesc() {
        return status != null ? status.getDesc() : null;
    }
    
    /**
     * 获取交易方式的中文描述
     */
    public String getTradeTypeDesc() {
        return tradeType != null ? tradeType.getDesc() : null;
    }
} 