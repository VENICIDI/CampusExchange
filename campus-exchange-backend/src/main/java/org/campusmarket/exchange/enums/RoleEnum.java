// 文件路径: E:\soft_ware\idea_projects\CampusExchange\campus-exchange-backend\src\main\java\org\campusmarket\exchange\enums\RoleEnum.java
package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum RoleEnum {
    
    USER("USER", "普通用户"),
    MERCHANT("MERCHANT", "商家"),
    ADMIN("ADMIN", "管理员");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    RoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
