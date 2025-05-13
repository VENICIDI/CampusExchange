package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 商品评价状态枚举
 */
@Getter
public enum ReviewStatusEnum {
    NOT_REVIEWED("NOT_REVIEWED", "未评价"),
    REVIEWED("REVIEWED", "已评价");
    
    @EnumValue
    private final String code;
    
    @JsonValue
    private final String desc;
    
    ReviewStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 