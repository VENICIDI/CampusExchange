// 文件路径: E:\soft_ware\idea_projects\CampusExchange\campus-exchange-backend\src\main\java\org\campusmarket\exchange\enums\UserStatusEnum.java
package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatusEnum {
    
    PENDING("PENDING", "待审核"),
    NORMAL("NORMAL", "正常"),
    DISABLED("DISABLED", "已禁用");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String desc;
    
    UserStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
