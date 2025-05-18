package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分账户实体类，对应数据库 points_account 表
 */
@Data
@TableName("points_account")
public class PointsAccount {
    
    // 积分账户ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 用户ID (外键)
    private Long userId;
    
    // 积分余额
    private Integer totalPoints;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 