package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 轮播图链接类型枚举
 */
@Getter
public enum BannerLinkTypeEnum {
    
    NONE("NONE", "无链接"),
    PRODUCT("PRODUCT", "商品详情"),
    MERCHANT_STORE("MERCHANT_STORE", "商家店铺"),
    CATEGORY("CATEGORY", "分类页"),
    EXTERNAL_URL("EXTERNAL_URL", "外部链接");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    BannerLinkTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 