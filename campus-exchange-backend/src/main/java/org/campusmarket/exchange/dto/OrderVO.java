package org.campusmarket.exchange.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.campusmarket.exchange.entity.OrderItem;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.enums.TradeTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单列表VO（精简版，不含详细信息）
 */
@Data
public class OrderVO {
    
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
    private String userName;
    
    /**
     * 商家ID
     */
    private Long merchantId;
    
    /**
     * 商家店铺名
     */
    private String merchantName;
    
    /**
     * 订单商品总金额
     */
    private BigDecimal totalProductAmount;
    
    /**
     * 实际支付金额
     */
    private BigDecimal actualPaymentAmount;
    
    /**
     * 平台佣金金额
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
     * 快递公司 (非数据库字段)
     */
    @TableField(exist = false)
    private String expressCompany;
    
    /**
     * 快递单号 (非数据库字段)
     */
    @TableField(exist = false)
    private String trackingNo;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 收货地址信息（非数据库字段，从用户表获取）
     */
    private OrderAddressVO orderAddress;
    
    /**
     * 商家是否已评价买家
     */
    private Boolean buyerReviewed;
    
    /**
     * 订单项列表（只包含简要信息）
     */
    private List<SimpleOrderItem> orderItems;
    
    /**
     * 订单状态的中文描述
     */
    private String statusDesc;
    
    /**
     * 交易方式的中文描述
     */
    private String tradeTypeDesc;
    
    /**
     * 收货地址VO
     */
    @Data
    public static class OrderAddressVO {
        /**
         * 收件人
         */
        private String recipient;
        
        /**
         * 收件人电话
         */
        private String phone;
        
        /**
         * 完整地址
         */
        private String fullAddress;
    }
    
    /**
     * 获取订单状态的中文描述
     */
    public String getStatusDesc() {
        if (status == null) return null;
        
        switch (status) {
            case PENDING_PAYMENT: return "待付款";
            case PENDING_SHIPMENT: return "待发货";
            case SHIPPED: return "已发货";
            case RECEIVED: return "已收货";
            case COMPLETED: return "已完成";
            case CANCELLED: return "已取消";
            case RETURN_REQUESTED: return "申请退货";
            case RETURN_APPROVED: return "退货审核通过";
            case RETURN_GOODS_RECEIVED: return "收到退货";
            case RETURNED: return "已退货退款";
            case RETURN_REJECTED: return "拒绝退货";
            default: return status.toString();
        }
    }
    
    /**
     * 获取交易方式的中文描述
     */
    public String getTradeTypeDesc() {
        if (tradeType == null) return null;
        
        switch (tradeType) {
            case EXPRESS: return "快递配送";
            case OFFLINE: return "线下交易";
            default: return tradeType.toString();
        }
    }
    
    /**
     * 简化版订单项，只包含基本信息
     */
    @Data
    public static class SimpleOrderItem {
        /**
         * 商品ID
         */
        private Long productId;
        
        /**
         * 商品名称
         */
        private String productName;
        
        /**
         * 商品图片
         */
        private String productImage;
        
        /**
         * 单价
         */
        private BigDecimal price;
        
        /**
         * 数量
         */
        private Integer quantity;
        
        /**
         * 从OrderItem转换为SimpleOrderItem
         */
        public static SimpleOrderItem fromOrderItem(OrderItem orderItem) {
            SimpleOrderItem simpleItem = new SimpleOrderItem();
            simpleItem.setProductId(orderItem.getProductId());
            simpleItem.setProductName(orderItem.getProductNameSnapshot());
            simpleItem.setProductImage(orderItem.getProductImageSnapshot());
            simpleItem.setPrice(orderItem.getPriceAtPurchase());
            simpleItem.setQuantity(orderItem.getQuantity());
            return simpleItem;
        }
    }
} 