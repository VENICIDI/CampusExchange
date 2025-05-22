package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.campusmarket.exchange.dto.CartItemDTO;
import org.campusmarket.exchange.entity.CartItem;
import org.campusmarket.exchange.service.ICartService;
import org.campusmarket.exchange.util.Result;
import org.campusmarket.exchange.util.UserContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Resource
    private ICartService cartService;

    /**
     * 获取当前用户的购物车商品
     */
    @GetMapping
    public Result<List<CartItemDTO>> getCartItems() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(403, "未登录或用户信息缺失");
        }
        
        List<CartItemDTO> cartItems = cartService.getUserCartItems(userId);
        return Result.success(cartItems);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Result<Boolean> addToCart(@RequestBody Map<String, Object> params) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(403, "未登录或用户信息缺失");
        }
        
        // 从请求体中解析参数
        Long productId = null;
        Integer quantity = null;
        
        try {
            if (params.containsKey("productId")) {
                productId = Long.valueOf(params.get("productId").toString());
            }
            if (params.containsKey("quantity")) {
                quantity = Integer.valueOf(params.get("quantity").toString());
            }
        } catch (Exception e) {
            return Result.error(400, "参数格式错误: " + e.getMessage());
        }
        
        // 参数校验
        if (productId == null || quantity == null) {
            return Result.error(400, "缺少必要参数");
        }
        
        // 调用服务方法
        boolean success = cartService.addToCart(userId, productId, quantity);
        return Result.success(success);
    }

    /**
     * 更新购物车商品数量
     */
    @PostMapping("/{cartItemId}/quantity")
    public Result<Boolean> updateQuantity(@PathVariable("cartItemId") Long cartItemId, 
                                         @RequestParam("quantity") Integer quantity) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(403, "未登录或用户信息缺失");
        }
        
        boolean result = cartService.updateCartItemQuantity(userId, cartItemId, quantity);
        return Result.success(result);
    }

    /**
     * 更新购物车商品选中状态
     */
    @PostMapping("/{cartItemId}/selected")
    public Result<Boolean> updateSelected(@PathVariable("cartItemId") Long cartItemId, 
                                         @RequestParam("selected") Boolean selected) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(403, "未登录或用户信息缺失");
        }
        
        boolean result = cartService.updateCartItemSelected(userId, cartItemId, selected);
        return Result.success(result);
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/{cartItemId}")
    public Result<Boolean> removeItem(@PathVariable("cartItemId") Long cartItemId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(403, "未登录或用户信息缺失");
        }
        
        boolean result = cartService.removeFromCart(userId, cartItemId);
        return Result.success(result);
    }

    /**
     * 测试请求头的API
     */
    @GetMapping("/test-headers")
    public Result<Map<String, String>> testHeaders(HttpServletRequest request) {
        Map<String, String> headerMap = new java.util.HashMap<>();
        
        // 获取所有请求头
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            headerMap.put(name, value);
        }
        
        // 输出认证信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            headerMap.put("_authenticated", String.valueOf(auth.isAuthenticated()));
            headerMap.put("_principal", String.valueOf(auth.getPrincipal()));
            headerMap.put("_name", auth.getName());
        } else {
            headerMap.put("_authenticated", "false");
            headerMap.put("_principal", "null");
        }
        
        return Result.success(headerMap);
    }

    /**
     * 清空已选中的购物车商品
     */
    @DeleteMapping("/selected")
    public Result<Boolean> clearSelectedItems() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(403, "未登录或用户信息缺失");
        }
        
        boolean result = cartService.clearSelectedCartItems(userId);
        return Result.success(result);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        // 首先尝试从请求头中获取
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    try {
                        // 打印调试信息
                        System.out.println("从请求头获取到用户ID: " + userIdHeader);
                        return Long.parseLong(userIdHeader);
                    } catch (NumberFormatException e) {
                        System.err.println("X-User-Id格式不正确: " + userIdHeader);
                    }
                } else {
                    System.out.println("请求头中没有X-User-Id");
                }
            }
        } catch (Exception e) {
            System.err.println("从请求头获取用户ID时出错: " + e.getMessage());
        }
        
        // 尝试从Spring Security上下文获取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getPrincipal().equals("anonymousUser")) {
            
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                try {
                    return Long.parseLong(userDetails.getUsername());
                } catch (NumberFormatException e) {
                    System.err.println("用户名不是有效的用户ID: " + userDetails.getUsername());
                }
            } else {
                System.out.println("认证信息的Principal不是UserDetails: " + 
                                 authentication.getPrincipal().getClass().getName());
            }
        } else {
            System.out.println("未经过认证的请求");
        }
        
        return null;
    }
} 