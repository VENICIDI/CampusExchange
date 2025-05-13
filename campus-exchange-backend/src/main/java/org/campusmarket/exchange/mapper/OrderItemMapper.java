package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.campusmarket.exchange.entity.OrderItem;

import java.util.List;

/**
 * 订单项 Mapper 接口
 */
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    
    /**
     * 根据订单ID查询订单项
     * @param orderId 订单ID
     * @return 订单项列表
     */
    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 根据订单编号查询订单项
     * @param orderNo 订单编号
     * @return 订单项列表
     */
    @Select("SELECT oi.* FROM order_item oi JOIN `order` o ON oi.order_id = o.id WHERE o.order_no = #{orderNo}")
    List<OrderItem> selectByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 批量插入订单项
     * @param items 订单项列表
     * @return 影响行数
     */
    int batchInsert(@Param("items") List<OrderItem> items);
} 