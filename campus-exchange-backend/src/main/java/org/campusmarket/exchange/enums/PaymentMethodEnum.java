package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

// 支付方式枚举
@Getter
public enum PaymentMethodEnum {
    
    WECHAT("WECHAT", "微信"),
    ALIPAY("ALIPAY", "支付宝"),
    OTHER("OTHER", "其他");
    
    @EnumValue
    private final String code;
    
    @JsonValue
    private final String desc;
    
    PaymentMethodEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 