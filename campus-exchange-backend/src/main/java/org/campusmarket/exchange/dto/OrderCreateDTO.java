package org.campusmarket.exchange.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.campusmarket.exchange.enums.TradeTypeEnum;

import java.util.List;

/**
 * 订单创建DTO
 */
@Data
public class OrderCreateDTO {
    
    /**
     * 商家ID
     */
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;
    
    /**
     * 交易方式：EXPRESS-快递，OFFLINE-线下交易
     */
    @NotNull(message = "交易方式不能为空")
    private TradeTypeEnum tradeType;
    
    /**
     * 使用的积分数量
     */
    private Integer pointsUsed;
    
    /**
     * 线下交易地点
     */
    private String offlineMeetingLocation;
    
    /**
     * 线下交易时间，格式：yyyy-MM-dd HH:mm:ss
     */
    private String offlineMeetingTime;
    
    /**
     * 收货地址ID（快递交易必填）
     */
    private Long addressId;
    
    /**
     * 收货地址信息（快递交易必填，如果addressId为空则使用该地址信息）
     */
    @Valid
    private OrderAddressDTO address;
    
    /**
     * 订单项列表
     */
    @NotEmpty(message = "订单项不能为空")
    @Valid
    private List<OrderItemDTO> items;
    
    /**
     * 订单备注
     */
    private String remark;
    
    /**
     * 订单项DTO
     */
    @Data
    public static class OrderItemDTO {
        
        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long productId;
        
        /**
         * 购买数量
         */
        @NotNull(message = "购买数量不能为空")
        private Integer quantity;
    }
    
    /**
     * 收货地址DTO
     */
    @Data
    public static class OrderAddressDTO {
        
        /**
         * 收货人姓名
         */
        @NotBlank(message = "收货人姓名不能为空")
        private String receiverName;
        
        /**
         * 收货人手机号
         */
        @NotBlank(message = "收货人手机号不能为空")
        private String receiverPhone;
        
        /**
         * 完整地址
         */
        @NotBlank(message = "收货地址不能为空")
        private String fullAddress;
        
        /**
         * 是否为默认地址
         */
        private Boolean isDefault;
    }
} 