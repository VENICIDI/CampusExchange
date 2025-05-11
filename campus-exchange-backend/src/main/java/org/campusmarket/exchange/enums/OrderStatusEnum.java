package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatusEnum {
    
    PENDING_PAYMENT("PENDING_PAYMENT", "待付款"),
    PENDING_SHIPMENT("PENDING_SHIPMENT", "待发货"),
    SHIPPED("SHIPPED", "已发货"),
    RECEIVED("RECEIVED", "已收货"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    RETURN_REQUESTED("RETURN_REQUESTED", "申请退货"),
    RETURN_APPROVED("RETURN_APPROVED", "退货审核通过"), 
    RETURN_GOODS_RECEIVED("RETURN_GOODS_RECEIVED", "收到退货"),
    RETURNED("RETURNED", "已退货退款"),
    RETURN_REJECTED("RETURN_REJECTED", "拒绝退货");
    
    @EnumValue
    private final String code;
    
    @JsonValue
    private final String desc;
    
    OrderStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 