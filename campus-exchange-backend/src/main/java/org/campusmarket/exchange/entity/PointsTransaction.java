package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分交易记录实体类，对应数据库 points_transaction 表
 */
@Data
@TableName("points_transaction")
public class PointsTransaction {
    
    // 交易ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 积分账户ID (外键)
    private Long pointsAccountId;
    
    // 关联订单ID
    private Long relatedOrderId;
    
    // 积分变动数量 (正为获得, 负为消耗)
    private Integer pointsChange;
    
    // 交易类型：PURCHASE_EARNED-消费获得，ORDER_DEDUCTION_USED-订单抵扣使用，SYSTEM_REWARD-系统奖励，SYSTEM_DEDUCTION-系统扣减, REFUND_RETURNED-退款返还积分
    private String type;
    
    // 交易描述
    private String description;
    
    // 交易后积分余额
    private Integer balanceAfterTransaction;
    
    // 创建时间
    private LocalDateTime createTime;
} 