// 文件路径: E:\soft_ware\idea_projects\CampusExchange\campus-exchange-backend\src\main\java\org\campusmarket\exchange\enums\UserStatusEnum.java
package org.campusmarket.exchange.enums;

// 用户状态枚举
public enum UserStatusEnum {
    // 待审核
    PENDING,
    // 正常
    NORMAL,
    // 已禁用
    DISABLED
    // 注意：数据库设计中有 REJECTED 状态，根据需要可以在这里添加，或者在业务逻辑中处理（比如审核不通过直接删除或标记为禁用？）
    // 暂时先保持与朋友设计中的 PENDING, NORMAL, DISABLED 一致
}
