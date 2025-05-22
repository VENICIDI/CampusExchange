package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;
import org.campusmarket.exchange.entity.ReturnRequest;

/**
 * 退货申请表 Mapper 接口
 */
@Mapper
public interface ReturnRequestMapper extends BaseMapper<ReturnRequest> {
    
    /**
     * 根据订单ID查询退货申请
     * @param orderId 订单ID
     * @return 退货申请信息
     */
    @Select("SELECT * FROM return_request WHERE order_id = #{orderId}")
    ReturnRequest selectByOrderId(@Param("orderId") Long orderId);
} 