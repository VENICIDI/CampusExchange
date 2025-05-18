package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 积分交易类型枚举
 */
@Getter
public enum PointsTransactionTypeEnum {
    
    PURCHASE_EARNED("PURCHASE_EARNED", "消费获得"),
    ORDER_DEDUCTION_USED("ORDER_DEDUCTION_USED", "订单抵扣使用"),
    SYSTEM_REWARD("SYSTEM_REWARD", "系统奖励"),
    SYSTEM_DEDUCTION("SYSTEM_DEDUCTION", "系统扣减"),
    REFUND_RETURNED("REFUND_RETURNED", "退款返还积分");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    PointsTransactionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 