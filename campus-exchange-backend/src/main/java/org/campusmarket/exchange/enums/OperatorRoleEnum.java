package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 操作者角色枚举
 */
@Getter
public enum OperatorRoleEnum {
    
    ADMIN("ADMIN", "管理员"),
    MERCHANT("MERCHANT", "商家");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    OperatorRoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 