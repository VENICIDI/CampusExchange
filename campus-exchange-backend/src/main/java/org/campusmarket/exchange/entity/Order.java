package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.enums.PaymentMethodEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类, 对应数据库 order 表
 */
@Data
@TableName("`order`")
public class Order {

    /**
     * 订单ID (主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 买家ID (外键)
     */
    private Long userId;
    
    /**
     * 商品ID (外键)
     */
    private Long productId;
    
    /**
     * 商家ID (外键)
     */
    private Long merchantId;
    
    /**
     * 订单编号
     */
    private String orderNumber;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 订单状态：PENDING-待付款，PAID-已付款，SHIPPED-已发货，COMPLETED-已完成，CANCELLED-已取消，REFUNDING-退款中，REFUNDED-已退款
     */
    private OrderStatusEnum status;
    
    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 支付方式：WECHAT-微信，ALIPAY-支付宝，OTHER-其他
     */
    private PaymentMethodEnum paymentMethod;
    
    /**
     * 收货地址ID
     */
    private Long addressId;
    
    /**
     * 收货人姓名
     */
    private String receiverName;
    
    /**
     * 收货人电话
     */
    private String receiverPhone;
    
    /**
     * 收货地址
     */
    private String receiverAddress;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 