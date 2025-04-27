package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品图片实体类, 对应数据库 product_image 表
 */
@Data
@TableName("product_image")
public class ProductImage {

    /**
     * 图片ID (主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商品ID (外键)
     */
    private Long productId;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 排序序号
     */
    private Integer sort;
    
    /**
     * 是否主图：0-否，1-是
     */
    private Boolean isMain;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 