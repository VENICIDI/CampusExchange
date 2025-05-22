package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.dto.CartItemDTO;
import org.campusmarket.exchange.entity.Cart;
import org.campusmarket.exchange.entity.CartItem;
import org.campusmarket.exchange.entity.Merchant;
import org.campusmarket.exchange.entity.Product;
import org.campusmarket.exchange.entity.ProductImage;
import org.campusmarket.exchange.enums.ProductStatusEnum;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.mapper.CartItemMapper;
import org.campusmarket.exchange.mapper.CartMapper;
import org.campusmarket.exchange.mapper.MerchantMapper;
import org.campusmarket.exchange.mapper.ProductImageMapper;
import org.campusmarket.exchange.mapper.ProductMapper;
import org.campusmarket.exchange.service.ICartService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 */
@Slf4j
@Service
public class CartServiceImpl implements ICartService {

    @Resource
    private CartMapper cartMapper;
    
    @Resource
    private CartItemMapper cartItemMapper;
    
    @Resource
    private ProductMapper productMapper;
    
    @Resource
    private MerchantMapper merchantMapper;
    
    @Resource
    private ProductImageMapper productImageMapper;
    
    @Override
    public Cart getUserCart(Long userId) {
        Cart cart = cartMapper.selectByUserId(userId);
        if (cart == null) {
            // 如果用户没有购物车，则创建一个
            cart = new Cart();
            cart.setUserId(userId);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cart);
        }
        return cart;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addToCart(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "商品数量必须大于0");
        }
        
        // 校验商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "商品不存在");
        }
        
        // 校验商品是否在售
        if (!ProductStatusEnum.ON_SALE.equals(product.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "商品已下架或售罄");
        }
        
        // 校验库存
        if (product.getStock() < quantity) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "商品库存不足，当前库存:" + product.getStock());
        }
        
        // 获取或创建用户购物车
        Cart cart = getUserCart(userId);
        
        // 查询是否已有该商品
        CartItem existingItem = cartItemMapper.selectByCartIdAndProductId(cart.getId(), productId);
        
        if (existingItem != null) {
            // 已有该商品，更新数量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setUpdateTime(LocalDateTime.now());
            cartItemMapper.updateById(existingItem);
        } else {
            // 添加新商品到购物车
            CartItem cartItem = new CartItem();
            cartItem.setCartId(cart.getId());
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
            cartItem.setSelected(true);
            cartItem.setCreateTime(LocalDateTime.now());
            cartItem.setUpdateTime(LocalDateTime.now());
            cartItemMapper.insert(cartItem);
        }
        
        return true;
    }
    
    @Override
    public boolean updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "商品数量必须大于0");
        }
        
        // 查询购物车项
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "购物车项不存在");
        }
        
        // 查询所属购物车
        Cart cart = cartMapper.selectById(cartItem.getCartId());
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "不能修改其他用户的购物车");
        }
        
        // 查询商品
        Product product = productMapper.selectById(cartItem.getProductId());
        if (product == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "商品不存在");
        }
        
        // 校验库存
        if (product.getStock() < quantity) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "商品库存不足，当前库存:" + product.getStock());
        }
        
        // 更新数量
        cartItem.setQuantity(quantity);
        cartItem.setUpdateTime(LocalDateTime.now());
        cartItemMapper.updateById(cartItem);
        
        return true;
    }
    
    @Override
    public boolean removeFromCart(Long userId, Long cartItemId) {
        // 查询购物车项
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null) {
            // 已经不存在，视为删除成功
            return true;
        }
        
        // 查询所属购物车
        Cart cart = cartMapper.selectById(cartItem.getCartId());
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "不能删除其他用户的购物车项");
        }
        
        // 删除购物车项
        cartItemMapper.deleteById(cartItemId);
        
        return true;
    }
    
    @Override
    public List<CartItemDTO> getUserCartItems(Long userId) {
        // 获取用户购物车
        Cart cart = getUserCart(userId);
        
        // 获取购物车项
        List<CartItem> cartItems = cartItemMapper.selectByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有商品ID
        List<Long> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());
        
        // 批量查询商品
        LambdaQueryWrapper<Product> productQueryWrapper = Wrappers.<Product>lambdaQuery()
                .in(Product::getId, productIds);
        List<Product> products = productMapper.selectList(productQueryWrapper);
        
        // 构建商品ID到商品的映射
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        
        // 获取所有商家ID
        List<Long> merchantIds = products.stream()
                .map(Product::getMerchantId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询商家
        LambdaQueryWrapper<Merchant> merchantQueryWrapper = Wrappers.<Merchant>lambdaQuery()
                .in(Merchant::getId, merchantIds);
        List<Merchant> merchants = merchantMapper.selectList(merchantQueryWrapper);
        
        // 构建商家ID到商家名称的映射
        Map<Long, String> merchantNameMap = merchants.stream()
                .collect(Collectors.toMap(Merchant::getId, Merchant::getStoreName));
        
        // 查询所有商品的主图
        Map<Long, String> productImageMap = new HashMap<>();
        for (Long productId : productIds) {
            // 查询是否有主图
            LambdaQueryWrapper<ProductImage> imageQuery = Wrappers.<ProductImage>lambdaQuery()
                    .eq(ProductImage::getProductId, productId)
                    .eq(ProductImage::getIsMain, true)
                    .orderByAsc(ProductImage::getSortOrder)
                    .last("LIMIT 1");
            
            ProductImage mainImage = productImageMapper.selectOne(imageQuery);
            if (mainImage != null) {
                productImageMap.put(productId, mainImage.getImageUrl());
            }
        }
        
        // 构建CartItemDTO列表
        List<CartItemDTO> cartItemDTOs = new ArrayList<>();
        for (CartItem item : cartItems) {
            Product product = productMap.get(item.getProductId());
            if (product != null) {
                String merchantName = merchantNameMap.getOrDefault(product.getMerchantId(), "未知商家");
                CartItemDTO dto = CartItemDTO.fromCartItemAndProduct(item, product, merchantName);
                
                // 设置商品主图
                String imageUrl = productImageMap.get(product.getId());
                if (imageUrl != null) {
                    dto.setProductImage(imageUrl);
                }
                
                cartItemDTOs.add(dto);
            }
        }
        
        return cartItemDTOs;
    }
    
    @Override
    public boolean updateCartItemSelected(Long userId, Long cartItemId, Boolean selected) {
        // 查询购物车项
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "购物车项不存在");
        }
        
        // 查询所属购物车
        Cart cart = cartMapper.selectById(cartItem.getCartId());
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "不能修改其他用户的购物车");
        }
        
        // 更新选中状态
        cartItem.setSelected(selected);
        cartItem.setUpdateTime(LocalDateTime.now());
        cartItemMapper.updateById(cartItem);
        
        return true;
    }
    
    @Override
    public Map<Long, List<CartItem>> getSelectedCartItemsByMerchant(Long userId) {
        // 获取用户购物车
        Cart cart = getUserCart(userId);
        
        // 获取已选中的购物车项
        List<CartItem> selectedItems = cartItemMapper.selectSelectedByCartId(cart.getId());
        if (selectedItems.isEmpty()) {
            return new HashMap<>();
        }
        
        // 获取所有商品ID
        List<Long> productIds = selectedItems.stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());
        
        // 批量查询商品
        LambdaQueryWrapper<Product> productQueryWrapper = Wrappers.<Product>lambdaQuery()
                .in(Product::getId, productIds);
        List<Product> products = productMapper.selectList(productQueryWrapper);
        
        // 构建商品ID到商品的映射
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        
        // 遍历购物车项，设置商品和商家ID
        for (CartItem item : selectedItems) {
            Product product = productMap.get(item.getProductId());
            if (product != null) {
                item.setProduct(product);
                item.setMerchantId(product.getMerchantId());
            }
        }
        
        // 按商家ID分组
        Map<Long, List<CartItem>> merchantItemsMap = selectedItems.stream()
                .filter(item -> item.getMerchantId() != null) // 过滤掉没有关联到商品的项
                .collect(Collectors.groupingBy(CartItem::getMerchantId));
        
        return merchantItemsMap;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearSelectedCartItems(Long userId) {
        // 获取用户购物车
        Cart cart = getUserCart(userId);
        
        // 删除已选中的购物车项
        LambdaUpdateWrapper<CartItem> updateWrapper = Wrappers.<CartItem>lambdaUpdate()
                .eq(CartItem::getCartId, cart.getId())
                .eq(CartItem::getSelected, true);
        cartItemMapper.delete(updateWrapper);
        
        return true;
    }
} 