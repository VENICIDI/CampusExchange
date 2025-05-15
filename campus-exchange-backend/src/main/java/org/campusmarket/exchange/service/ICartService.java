package org.campusmarket.exchange.service;

import java.util.List;
import java.util.Map;

import org.campusmarket.exchange.dto.CartItemDTO;
import org.campusmarket.exchange.entity.Cart;
import org.campusmarket.exchange.entity.CartItem;

/**
 * 购物车服务接口
 */
public interface ICartService {
    
    /**
     * 获取用户购物车
     * @param userId 用户ID
     * @return 购物车
     */
    Cart getUserCart(Long userId);
    
    /**
     * 添加商品到购物车
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean addToCart(Long userId, Long productId, Integer quantity);
    
    /**
     * 更新购物车商品数量
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity);
    
    /**
     * 删除购物车项
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @return 是否成功
     */
    boolean removeFromCart(Long userId, Long cartItemId);
    
    /**
     * 获取用户购物车商品列表
     * @param userId 用户ID
     * @return 购物车商品列表
     */
    List<CartItemDTO> getUserCartItems(Long userId);
    
    /**
     * 更新购物车项选中状态
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @param selected 是否选中
     * @return 是否成功
     */
    boolean updateCartItemSelected(Long userId, Long cartItemId, Boolean selected);
    
    /**
     * 获取用户购物车中已选中的商品, 按商家分组
     * @param userId 用户ID
     * @return 商家ID -> 该商家的购物车商品列表
     */
    Map<Long, List<CartItem>> getSelectedCartItemsByMerchant(Long userId);
    
    /**
     * 清空用户购物车中已选中的商品
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearSelectedCartItems(Long userId);
} 