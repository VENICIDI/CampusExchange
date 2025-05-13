package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.ReviewStatusEnum;

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
    
    // 商品ID (外键)
    private Long productId;
    
    // 商家ID (冗余, 便于查询)
    private Long merchantId;
    
    // 商品名称快照
    private String productNameSnapshot;
    
    // 商品主图快照
    private String productImageSnapshot;
    
    // 购买时商品单价
    private BigDecimal priceAtPurchase;
    
    // 购买数量
    private Integer quantity;
    
    // 该项商品总金额 (单价*数量)
    private BigDecimal itemTotalAmount;
    
    // 下单时手续费率快照
    private BigDecimal commissionRateSnapshot;
    
    // 该项商品产生的手续费金额快照
    private BigDecimal commissionAmountSnapshot;
    
    // 商品评价状态：NOT_REVIEWED-未评价，REVIEWED-已评价
    private ReviewStatusEnum reviewStatus;
    
    // 创建时间
    private LocalDateTime createTime;
} 