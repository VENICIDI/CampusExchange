package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.BlacklistScopeEnum;
import org.campusmarket.exchange.enums.OperatorRoleEnum;

import java.time.LocalDateTime;

/**
 * 用户黑名单实体类，对应数据库 user_blacklist 表
 */
@Data
@TableName("user_blacklist")
public class UserBlacklist {
    
    // 黑名单ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 被拉黑用户ID
    private Long userId;
    
    // 拉黑范围: PLATFORM-平台级, MERCHANT_SPECIFIC-商家级
    private BlacklistScopeEnum scope;
    
    // 操作者ID (管理员或商家用户ID)
    private Long operatorId;
    
    // 操作者角色
    private OperatorRoleEnum operatorRole;
    
    // 目标商家ID (当scope为MERCHANT_SPECIFIC时填写)
    private Long targetMerchantId;
    
    // 拉黑原因
    private String reason;
    
    // 是否生效：1-是，0-否
    private Boolean isActive;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 