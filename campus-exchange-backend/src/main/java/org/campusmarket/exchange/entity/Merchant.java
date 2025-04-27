// 文件路径: E:\soft_ware\idea_projects\CampusExchange\campus-exchange-backend\src\main\java\org\campusmarket\exchange\entity\Merchant.java
package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.MerchantLevelEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家信息实体类, 对应数据库 merchant 表
 */
@Data
@TableName("merchant")
public class Merchant {

    /**
     * 商家ID (主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的用户ID (外键)
     * 使用 @TableField 明确指定数据库列名 user_id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 营业执照图片URL
     */
    private String businessLicense;

    /**
     * 身份证图片URL
     */
    private String idCard;

    /**
     * 店铺名称
     */
    private String storeName;

    /**
     * 商家等级：LEVEL1-1级...LEVEL5-5级
     */
    private MerchantLevelEnum level;

    /**
     * 手续费率 (DECIMAL(5,4))
     * 使用 BigDecimal 保证精度
     */
    private BigDecimal commissionRate;

    /**
     * 总销量
     */
    private Integer totalSales;

    /**
     * 总销售额 (DECIMAL(12,2))
     */
    private BigDecimal totalAmount;

    /**
     * 好评率 (DECIMAL(5,2))
     */
    private BigDecimal positiveRate;

    /**
     * 店铺描述/公告
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}