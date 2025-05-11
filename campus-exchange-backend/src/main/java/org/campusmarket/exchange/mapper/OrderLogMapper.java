package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.campusmarket.exchange.entity.OrderLog;

import java.util.List;

/**
 * 订单日志 Mapper 接口
 */
public interface OrderLogMapper extends BaseMapper<OrderLog> {
    
    /**
     * 根据订单ID查询订单日志
     * @param orderId 订单ID
     * @return 订单日志列表
     */
    List<OrderLog> selectByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 根据订单编号查询订单日志
     * @param orderNo 订单编号
     * @return 订单日志列表
     */
    List<OrderLog> selectByOrderNo(@Param("orderNo") String orderNo);
} 