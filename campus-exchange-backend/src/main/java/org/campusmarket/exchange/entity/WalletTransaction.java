package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.WalletTransactionTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易记录实体类，对应数据库 wallet_transaction 表
 */
@Data
@TableName("wallet_transaction")
public class WalletTransaction {
    
    // 交易ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 钱包ID (外键)
    private Long walletId;
    
    // 关联订单ID
    private Long relatedOrderId;
    
    // 交易金额 (正为入账, 负为出账)
    private BigDecimal amount;
    
    // 交易类型：RECHARGE-充值，CONSUMPTION-消费，REFUND-退款，MERCHANT_INCOME-商家收入，PLATFORM_FEE-平台手续费支出，SYSTEM_ADJUSTMENT-系统调整
    private String type;
    
    // 交易描述
    private String description;
    
    // 交易后钱包可用余额
    private BigDecimal balanceAfterTransaction;
    
    // 创建时间
    private LocalDateTime createTime;
} 