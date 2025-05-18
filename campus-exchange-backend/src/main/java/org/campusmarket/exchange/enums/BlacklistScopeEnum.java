package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 黑名单范围枚举
 */
@Getter
public enum BlacklistScopeEnum {
    
    PLATFORM("PLATFORM", "平台级"),
    MERCHANT_SPECIFIC("MERCHANT_SPECIFIC", "商家级");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    BlacklistScopeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 