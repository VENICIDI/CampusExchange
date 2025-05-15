package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 商品新旧程度枚举
 */
@Getter
public enum ProductConditionEnum {
    NEW("NEW", "全新"),
    LIKE_NEW("LIKE_NEW", "九成新"),
    GOOD("GOOD", "八成新"),
    FAIR("FAIR", "七成新"),
    POOR("POOR", "六成新及以下");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    ProductConditionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
