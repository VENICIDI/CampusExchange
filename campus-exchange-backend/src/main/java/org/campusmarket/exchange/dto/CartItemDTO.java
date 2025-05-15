package org.campusmarket.exchange.dto;

import lombok.Data;
import org.campusmarket.exchange.entity.CartItem;
import org.campusmarket.exchange.entity.Product;

import java.math.BigDecimal;

/**
 * 购物车项DTO，用于前端展示
 */
@Data
public class CartItemDTO {
    
    // 购物车项ID
    private Long id;
    
    // 购物车ID
    private Long cartId;
    
    // 商品ID
    private Long productId;
    
    // 商家ID
    private Long merchantId;
    
    // 商家名称
    private String merchantName;
    
    // 商品数量
    private Integer quantity;
    
    // 是否选中
    private Boolean selected;
    
    // 商品名称
    private String productName;
    
    // 商品主图
    private String productImage;
    
    // 商品价格
    private BigDecimal price;
    
    // 商品库存
    private Integer stock;
    
    // 是否还有库存
    private Boolean inStock;
    
    /**
     * 从CartItem和Product构建DTO
     */
    public static CartItemDTO fromCartItemAndProduct(CartItem cartItem, Product product, String merchantName) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setCartId(cartItem.getCartId());
        dto.setProductId(product.getId());
        dto.setMerchantId(product.getMerchantId());
        dto.setMerchantName(merchantName);
        dto.setQuantity(cartItem.getQuantity());
        dto.setSelected(cartItem.getSelected());
        dto.setProductName(product.getName());
        // 设置商品主图 - 此处简化处理，实际可能需要查询商品主图
        dto.setProductImage(null);
        dto.setPrice(product.getCurrentPrice());
        dto.setStock(product.getStock());
        dto.setInStock(product.getStock() > 0);
        return dto;
    }
} 