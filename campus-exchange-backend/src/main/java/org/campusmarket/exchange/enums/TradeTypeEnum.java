package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 交易类型枚举
 */
@Getter
public enum TradeTypeEnum {
    
    EXPRESS("EXPRESS", "快递配送"),
    OFFLINE("OFFLINE", "线下交易");
    
    @EnumValue
    private final String code;
    
    @JsonValue
    private final String desc;
    
    TradeTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 