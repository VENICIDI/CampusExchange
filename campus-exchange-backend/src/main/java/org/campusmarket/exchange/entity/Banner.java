package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.BannerLinkTypeEnum;
import org.campusmarket.exchange.enums.BannerStatusEnum;

import java.time.LocalDateTime;

/**
 * 轮播图实体类，对应数据库 banner 表
 */
@Data
@TableName("banner")
public class Banner {
    
    // 轮播图ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 标题
    private String title;
    
    // 图片URL
    private String imageUrl;
    
    // 链接类型: NONE-无链接, PRODUCT-商品详情, MERCHANT_STORE-商家店铺, CATEGORY-分类页, EXTERNAL_URL-外部链接
    private BannerLinkTypeEnum linkType;
    
    // 链接目标 (商品ID, 商家ID, 分类ID, 或外部URL)
    private String linkTarget;
    
    // 排序序号
    private Integer sortOrder;
    
    // 状态：ENABLED-启用，DISABLED-禁用
    private BannerStatusEnum status;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 