package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.campusmarket.exchange.entity.Cart;

/**
 * 购物车Mapper接口
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
    
    /**
     * 根据用户ID查询购物车
     * @param userId 用户ID
     * @return 购物车
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId}")
    Cart selectByUserId(@Param("userId") Long userId);
} 