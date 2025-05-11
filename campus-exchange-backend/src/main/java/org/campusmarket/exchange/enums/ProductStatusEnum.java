package org.campusmarket.exchange.enums;

// 商品状态枚举
public enum ProductStatusEnum {
    // 待审核
    PENDING_APPROVAL,
    // 审核不通过可修改
    REJECTED_RESUBMIT,
    // 在售
    ON_SALE,
    // 已锁定（下单但未付款）
    LOCKED,
    // 已售罄
    SOLD_OUT,
    // 商家下架
    REMOVED_BY_SELLER;
    
    /**
     * 获取状态描述
     * @return 状态描述
     */
    public String getDesc() {
        switch (this) {
            case PENDING_APPROVAL: return "待审核";
            case REJECTED_RESUBMIT: return "审核不通过";
            case ON_SALE: return "在售";
            case LOCKED: return "已锁定";
            case SOLD_OUT: return "已售罄";
            case REMOVED_BY_SELLER: return "已下架";
            default: return this.name();
        }
    }
}
