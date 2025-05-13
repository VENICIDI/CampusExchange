package org.campusmarket.exchange.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue; // 保持这个
import lombok.Getter;

@Getter
public enum ProductStatusEnum {
    PENDING_APPROVAL("PENDING_APPROVAL", "待审核"),
    REJECTED_RESUBMIT("REJECTED_RESUBMIT", "审核不通过可修改"),
    ON_SALE("ON_SALE", "在售"),
    LOCKED("LOCKED", "已锁定"),
    SOLD_OUT("SOLD_OUT", "已售罄"),
    REMOVED_BY_SELLER("REMOVED_BY_SELLER", "商家下架");
    
    @EnumValue
    @JsonValue // ***** 修改点：将 @JsonValue 移到 code 属性上 *****
    private final String code; 
    
    // @JsonValue // 从 desc 属性上移除 @JsonValue
    private final String desc; 
    
    ProductStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    // 如果还需要通过 desc 获取枚举，或者在其他地方使用 desc，可以保留 getter
    public String getDesc() {
        return desc;
    }
    // code 的 getter 由 @Getter 生成
}