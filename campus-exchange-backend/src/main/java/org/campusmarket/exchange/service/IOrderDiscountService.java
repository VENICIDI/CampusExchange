package org.campusmarket.exchange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.campusmarket.exchange.entity.OrderDiscount;

import java.math.BigDecimal;

/**
 * 订单折扣服务接口
 */
public interface IOrderDiscountService extends IService<OrderDiscount> {
    
    /**
     * 为订单创建折扣
     *
     * @param orderId 订单ID
     * @param discountAmount 折扣金额
     * @param adminId 管理员ID
     * @return 是否成功
     */
    boolean createDiscount(Long orderId, BigDecimal discountAmount, Long adminId);
    
    /**
     * 根据订单ID获取折扣
     *
     * @param orderId 订单ID
     * @return 订单折扣
     */
    OrderDiscount getByOrderId(Long orderId);
} 