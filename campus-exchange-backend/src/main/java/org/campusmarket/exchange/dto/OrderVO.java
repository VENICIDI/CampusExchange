package org.campusmarket.exchange.dto;

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
     * 订单状态
     */
    private OrderStatusEnum status;
    
    /**
     * 交易方式
     */
    private TradeTypeEnum tradeType;
    
    /**
     * 收货人姓名
     */
    private String receiverName;
    
    /**
     * 收货人手机号
     */
    private String receiverPhone;
    
    /**
     * 收货地址
     */
    private String receiverAddress;
    
    /**
     * 快递公司
     */
    private String expressCompany;
    
    /**
     * 快递单号
     */
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