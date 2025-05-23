package org.campusmarket.exchange.dto;

import lombok.Data;
import org.campusmarket.exchange.entity.OrderItem;
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
     * 优惠券折扣金额（管理员发放）
     */
    private BigDecimal discountAmount;
    
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
     * 退货申请信息
     */
    private ReturnRequestInfo returnRequestInfo;
    
    /**
     * 订单项列表
     */
    private List<OrderItem> orderItems;
    
    /**
     * 订单状态的中文描述
     */
    private String statusDesc;
    
    /**
     * 交易方式的中文描述
     */
    private String tradeTypeDesc;
    
    /**
     * 收货地址信息（非数据库字段，从用户表获取）
     */
    private OrderAddressVO orderAddress;
    
    /**
     * 买家可访问链接
     * 用于跳转至买家信息详情页面
     */
    private String buyerInfoUrl;
    
    /**
     * 是否展示商品总额
     * 默认为false，商家端不展示
     */
    private Boolean showTotalProductAmount = false;
    
    /**
     * 是否展示积分抵扣
     * 默认为false，商家端不展示
     */
    private Boolean showPointsDeduction = false;
    
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
     * 退货申请信息
     */
    @Data
    public static class ReturnRequestInfo {
        /**
         * 退货原因
         */
        private String reason;
        
        /**
         * 拒绝原因 (若被拒绝)
         */
        private String rejectionReason;
        
        /**
         * 申请时间
         */
        private LocalDateTime applicationTime;
        
        /**
         * 退货状态
         */
        private String status;
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
} 