package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车项实体类
 */
@Data
@TableName("cart_item")
public class CartItem {
    
    // 购物车项ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 购物车ID (外键)
    private Long cartId;
    
    // 商品ID (外键)
    private Long productId;
    
    // 商品数量
    private Integer quantity;
    
    // 是否选中下单：0-否，1-是
    private Boolean selected;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
    
    // 非数据库字段，用于关联查询 - 商品信息
    @TableField(exist = false)
    private Product product;
    
    // 非数据库字段 - 商家ID (从商品获取)
    @TableField(exist = false)
    private Long merchantId;
} 