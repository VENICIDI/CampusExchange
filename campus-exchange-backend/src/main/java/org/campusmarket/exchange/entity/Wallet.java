package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包实体类，对应数据库 wallet 表
 */
@Data
@TableName("wallet")
public class Wallet {
    
    // 钱包ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 用户ID (外键)
    private Long userId;
    
    // 可用余额
    private BigDecimal balance;
    
    // 冻结金额 (可选, 用于支付中间态或提现)
    private BigDecimal frozenAmount;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 