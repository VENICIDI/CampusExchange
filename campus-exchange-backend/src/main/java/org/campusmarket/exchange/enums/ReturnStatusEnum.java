package org.campusmarket.exchange.enums;

/**
 * 退货状态枚举
 */
public enum ReturnStatusEnum {
    PENDING_APPROVAL("待商家审核"),
    APPROVED_PENDING_RETURN("同意待退货"),
    GOODS_RECEIVED_PENDING_REFUND("收到货待退款"),
    COMPLETED_REFUNDED("已完成退款"),
    REJECTED("已拒绝");
    
    private final String desc;
    
    ReturnStatusEnum(String desc) {
        this.desc = desc;
    }
    
    public String getDesc() {
        return desc;
    }
} 