package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 商家惩罚类型枚举
 */
@Getter
public enum MerchantPenaltyTypeEnum {
    
    TEMP_BAN_POSTING("TEMP_BAN_POSTING", "限时禁止发布"),
    UNLIST_ALL_PRODUCTS("UNLIST_ALL_PRODUCTS", "下架所有商品"),
    SHOP_CLOSURE("SHOP_CLOSURE", "店铺关闭");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    MerchantPenaltyTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 