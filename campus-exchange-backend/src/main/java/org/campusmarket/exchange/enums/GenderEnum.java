// 文件路径: E:\soft_ware\idea_projects\CampusExchange\campus-exchange-backend\src\main\java\org\campusmarket\exchange\enums\GenderEnum.java
package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
public enum GenderEnum {
    
    FEMALE("FEMALE", "女性"),
    MALE("MALE", "男性"),
    UNKNOWN("UNKNOWN", "未知");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    GenderEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}