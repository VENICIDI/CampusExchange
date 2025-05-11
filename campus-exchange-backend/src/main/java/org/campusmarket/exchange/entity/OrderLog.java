package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.OrderStatusEnum;

import java.time.LocalDateTime;

/**
 * 订单日志实体类，记录订单状态变更历史
 */
@Data
@TableName("order_log")
public class OrderLog {
    
    // 日志ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 订单ID (外键)
    private Long orderId;
    
    // 订单编号
    private String orderNo;
    
    // 前一个状态
    private OrderStatusEnum previousStatus;
    
    // 当前状态
    private OrderStatusEnum currentStatus;
    
    // 操作人ID
    private Long operatorId;
    
    // 操作人类型 (USER-用户, MERCHANT-商家, ADMIN-管理员, SYSTEM-系统)
    private String operatorType;
    
    // 操作人名称
    private String operatorName;
    
    // 操作备注
    private String operationNote;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // IP地址
    private String ipAddress;
} 