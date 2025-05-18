package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.MerchantPenaltyTypeEnum;

import java.time.LocalDateTime;

/**
 * 商家惩罚记录实体类，对应数据库 merchant_penalty_record 表
 */
@Data
@TableName("merchant_penalty_record")
public class MerchantPenaltyRecord {
    
    // 惩罚记录ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 被惩罚商家ID
    private Long merchantId;
    
    // 操作管理员ID
    private Long adminId;
    
    // 惩罚类型：TEMP_BAN_POSTING-限时禁止发布, UNLIST_ALL_PRODUCTS-下架所有商品, SHOP_CLOSURE-店铺关闭
    private MerchantPenaltyTypeEnum penaltyType;
    
    // 惩罚原因
    private String reason;
    
    // 封禁结束时间 (针对限时类型)
    private LocalDateTime banEndTime;
    
    // 当前是否生效：1-是，0-否
    private Boolean isActive;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 