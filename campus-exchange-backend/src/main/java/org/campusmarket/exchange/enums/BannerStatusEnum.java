package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 轮播图状态枚举
 */
@Getter
public enum BannerStatusEnum {
    
    ENABLED("ENABLED", "启用"),
    DISABLED("DISABLED", "禁用");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    BannerStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 