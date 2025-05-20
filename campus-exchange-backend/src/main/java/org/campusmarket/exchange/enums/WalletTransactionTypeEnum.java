package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 钱包交易类型枚举
 */
@Getter
public enum WalletTransactionTypeEnum {
    
    RECHARGE("RECHARGE", "充值"),
    CONSUMPTION("CONSUMPTION", "消费"),
    REFUND("REFUND", "退款"),
    MERCHANT_INCOME("MERCHANT_INCOME", "商家收入"),
    PLATFORM_FEE("PLATFORM_FEE", "平台手续费支出"),
    SYSTEM_ADJUSTMENT("SYSTEM_ADJUSTMENT", "系统调整");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    WalletTransactionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 获取枚举值对应的描述
     */
    public static String getDescByCode(String code) {
        for (WalletTransactionTypeEnum type : WalletTransactionTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type.getDesc();
            }
        }
        return code;
    }
} 