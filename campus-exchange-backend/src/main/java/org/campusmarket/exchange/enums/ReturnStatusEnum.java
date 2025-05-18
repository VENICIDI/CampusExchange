package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 退货状态枚举
 */
@Getter
public enum ReturnStatusEnum {
    
    PENDING_APPROVAL("PENDING_APPROVAL", "待商家审核"),
    APPROVED_PENDING_RETURN("APPROVED_PENDING_RETURN", "审核通过待退货"),
    GOODS_RECEIVED_PENDING_REFUND("GOODS_RECEIVED_PENDING_REFUND", "已收到退货待退款"),
    COMPLETED_REFUNDED("COMPLETED_REFUNDED", "已完成退款"),
    REJECTED("REJECTED", "已拒绝");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    ReturnStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 