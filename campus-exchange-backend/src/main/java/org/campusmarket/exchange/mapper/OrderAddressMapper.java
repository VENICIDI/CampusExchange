package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.campusmarket.exchange.entity.OrderAddress;

/**
 * 订单地址 Mapper 接口
 */
public interface OrderAddressMapper extends BaseMapper<OrderAddress> {
    
    /**
     * 根据订单ID查询订单地址
     * @param orderId 订单ID
     * @return 订单地址
     */
    OrderAddress selectByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 根据订单编号查询订单地址
     * @param orderNo 订单编号
     * @return 订单地址
     */
    OrderAddress selectByOrderNo(@Param("orderNo") String orderNo);
} 