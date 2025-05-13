package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 商品状态枚举
 */
@Getter
public enum ProductStatusEnum {
    PENDING_APPROVAL("PENDING_APPROVAL", "待审核"),
    REJECTED_RESUBMIT("REJECTED_RESUBMIT", "审核不通过可修改"),
    ON_SALE("ON_SALE", "在售"),
    LOCKED("LOCKED", "已锁定"),
    SOLD_OUT("SOLD_OUT", "已售罄"),
    REMOVED_BY_SELLER("REMOVED_BY_SELLER", "商家下架");
    
    @EnumValue
    private final String code;
    
    @JsonValue
    private final String desc;
    
    ProductStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
