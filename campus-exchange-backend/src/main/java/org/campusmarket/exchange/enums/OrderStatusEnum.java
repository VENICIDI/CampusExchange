package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatusEnum {
    
    PENDING("PENDING", "待付款"),
    PAID("PAID", "已付款"),
    SHIPPED("SHIPPED", "已发货"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    REFUNDING("REFUNDING", "退款中"),
    REFUNDED("REFUNDED", "已退款");
    
    @EnumValue
    private final String code;
    
    @JsonValue
    private final String desc;
    
    OrderStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 