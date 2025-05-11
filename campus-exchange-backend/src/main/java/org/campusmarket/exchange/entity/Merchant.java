// 文件路径: E:\soft_ware\idea_projects\CampusExchange\campus-exchange-backend\src\main\java\org\campusmarket\exchange\entity\Merchant.java
package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 商家信息实体类, 对应数据库 merchant 表
@Data
@TableName("merchant")
public class Merchant {

    // 商家ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;

    // 关联的用户ID (外键)
    @TableField("user_id")
    private Long userId;

    // 营业执照图片URL
    private String businessLicense;

    // 身份证图片URL
    private String idCard;

    // 店铺名称
    private String storeName;

    // 商家等级ID (外键, 关联merchant_level表)
    private Long levelId;

    // 总销量（商品件数）
    private Integer totalSalesCount;

    // 总销售额
    private BigDecimal totalSalesAmount;

    // 店铺好评率 (基于商家服务评价)
    private BigDecimal storePositiveRate;

    // 店铺描述/公告
    private String description;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}