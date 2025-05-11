package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体类，对应订单中的每个商品
 */
@Data
@TableName("order_item")
public class OrderItem {
    
    // 订单项ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 订单ID (外键)
    private Long orderId;
    
    // 订单编号
    private String orderNo;
    
    // 商品ID (外键)
    private Long productId;
    
    // 商品名称 (冗余，方便查询)
    private String productName;
    
    // 商品封面图 (冗余，方便展示)
    private String productImage;
    
    // 商品单价
    private BigDecimal price;
    
    // 购买数量
    private Integer quantity;
    
    // 小计金额 (单价 × 数量)
    private BigDecimal subtotal;
    
    // 商品规格描述（如尺寸、新旧程度等）
    private String specifications;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 