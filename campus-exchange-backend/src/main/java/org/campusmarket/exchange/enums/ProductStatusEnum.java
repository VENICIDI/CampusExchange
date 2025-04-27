package org.campusmarket.exchange.enums;

// 商品状态枚举
public enum ProductStatusEnum {
    // 待审核
    PENDING,
    // 在售
    ON_SALE,
    // 已锁定（下单但未付款）
    LOCKED,
    // 已售出
    SOLD,
    // 已下架
    REMOVED
}
