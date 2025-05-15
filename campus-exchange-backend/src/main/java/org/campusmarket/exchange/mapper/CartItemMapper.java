package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.campusmarket.exchange.entity.CartItem;

import java.util.List;

/**
 * 购物车项Mapper接口
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
    
    /**
     * 根据购物车ID查询购物车项
     * @param cartId 购物车ID
     * @return 购物车项列表
     */
    @Select("SELECT * FROM cart_item WHERE cart_id = #{cartId}")
    List<CartItem> selectByCartId(@Param("cartId") Long cartId);
    
    /**
     * 根据购物车ID和商品ID查询购物车项
     * @param cartId 购物车ID
     * @param productId 商品ID
     * @return 购物车项
     */
    @Select("SELECT * FROM cart_item WHERE cart_id = #{cartId} AND product_id = #{productId}")
    CartItem selectByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);
    
    /**
     * 根据购物车ID查询已选中的购物车项
     * @param cartId 购物车ID
     * @return 购物车项列表
     */
    @Select("SELECT * FROM cart_item WHERE cart_id = #{cartId} AND selected = 1")
    List<CartItem> selectSelectedByCartId(@Param("cartId") Long cartId);
} 