package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.enums.TradeTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 订单实体类, 对应数据库 order 表
@Data
@TableName("`order`")
public class Order {

    // 订单ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 订单编号 (唯一)
    private String orderNo;
    
    // 用户ID (买家, 外键)
    private Long userId;
    
    // 商家ID (外键)
    private Long merchantId;
    
    // 订单商品总金额 (优惠前, 所有订单项价格*数量之和)
    private BigDecimal totalProductAmount;
    
    // 使用的积分数量
    private Integer pointsUsed;
    
    // 积分抵扣金额
    private BigDecimal pointsDeductionAmount;
    
    // 折扣金额（管理员发放的优惠券）
    private BigDecimal discountAmount;
    
    // 实际支付金额 (商品总金额 - 积分抵扣 - 折扣金额)
    private BigDecimal actualPaymentAmount;
    
    // 平台手续费总额
    private BigDecimal platformCommissionAmount;
    
    // 订单状态
    private OrderStatusEnum status;
    
    // 交易方式：EXPRESS-快递，OFFLINE-线下交易
    private TradeTypeEnum tradeType;
    
    // 线下交易地点
    private String offlineMeetingLocation;
    
    // 线下交易时间
    private LocalDateTime offlineMeetingTime;
    
    // 支付时间 (模拟)
    private LocalDateTime paymentTime;
    
    // 发货时间
    private LocalDateTime shippingTime;
    
    // 确认收货时间
    private LocalDateTime receiptConfirmationTime;
    
    // 订单完成时间
    private LocalDateTime completionTime;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 