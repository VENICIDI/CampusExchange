package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单地址实体类，保存订单收货地址信息
 */
@Data
@TableName("order_address")
public class OrderAddress {
    
    // 地址ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 订单ID (外键)
    private Long orderId;
    
    // 订单编号
    private String orderNo;
    
    // 收货人姓名
    private String receiverName;
    
    // 收货人手机号
    private String receiverPhone;
    
    // 省份
    private String province;
    
    // 城市
    private String city;
    
    // 区/县
    private String district;
    
    // 详细地址
    private String detailAddress;
    
    // 邮政编码
    private String postalCode;
    
    // 是否为默认地址
    private Boolean isDefault;
    
    // 用户ID (冗余，方便查询)
    private Long userId;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 