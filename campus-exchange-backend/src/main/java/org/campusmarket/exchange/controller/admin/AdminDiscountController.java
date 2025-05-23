package org.campusmarket.exchange.controller.admin;

import lombok.RequiredArgsConstructor;
import org.campusmarket.exchange.dto.PendingOrderDTO;
import org.campusmarket.exchange.entity.Order;
import org.campusmarket.exchange.entity.OrderDiscount;
import org.campusmarket.exchange.entity.User;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.enums.RoleEnum;
import org.campusmarket.exchange.service.IMerchantService;
import org.campusmarket.exchange.service.IOrderDiscountService;
import org.campusmarket.exchange.service.IOrderService;
import org.campusmarket.exchange.service.IUserService;
import org.campusmarket.exchange.util.Result;
import org.campusmarket.exchange.util.UserContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员折扣控制器
 */
@RestController
@RequestMapping("/api/admin/discounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDiscountController {

    private final IOrderService orderService;
    private final IUserService userService;
    private final IMerchantService merchantService;
    private final IOrderDiscountService orderDiscountService;
    
    /**
     * 获取待付款订单列表
     */
    @GetMapping("/pending-orders")
    public Result<List<PendingOrderDTO>> getPendingOrders() {
        // 获取所有待付款订单
        List<Order> pendingOrders = orderService.list().stream()
                .filter(order -> OrderStatusEnum.PENDING_PAYMENT.equals(order.getStatus()))
                .collect(Collectors.toList());
        
        if (pendingOrders.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        
        // 获取用户和商家信息
        List<Long> userIds = pendingOrders.stream().map(Order::getUserId).collect(Collectors.toList());
        List<Long> merchantIds = pendingOrders.stream().map(Order::getMerchantId).collect(Collectors.toList());
        
        // 获取用户信息
        Map<Long, User> userMap = new HashMap<>();
        for (Long userId : userIds) {
            User user = userService.getUserById(userId);
            if (user != null) {
                userMap.put(userId, user);
            }
        }
        
        // 获取商家名称映射
        Map<Long, String> merchantNameMap = new HashMap<>();
        for (Long merchantId : merchantIds) {
            merchantNameMap.put(merchantId, getMerchantName(merchantId));
        }
        
        // 获取已存在的折扣信息
        Map<Long, OrderDiscount> discountMap = new HashMap<>();
        for (Long orderId : pendingOrders.stream().map(Order::getId).collect(Collectors.toList())) {
            OrderDiscount discount = orderDiscountService.getByOrderId(orderId);
            if (discount != null) {
                discountMap.put(orderId, discount);
            }
        }
        
        // 转换为DTO
        List<PendingOrderDTO> result = pendingOrders.stream().map(order -> {
            PendingOrderDTO dto = new PendingOrderDTO();
            dto.setId(order.getId());
            dto.setOrderNo(order.getOrderNo());
            dto.setUserId(order.getUserId());
            
            User user = userMap.get(order.getUserId());
            dto.setUserName(user != null ? user.getUsername() : "未知用户");
            
            dto.setMerchantId(order.getMerchantId());
            dto.setMerchantName(merchantNameMap.getOrDefault(order.getMerchantId(), "未知商家"));
            
            dto.setTotalProductAmount(order.getTotalProductAmount());
            dto.setPointsDeductionAmount(order.getPointsDeductionAmount());
            dto.setActualPaymentAmount(order.getActualPaymentAmount());
            dto.setCreateTime(order.getCreateTime());
            
            // 设置折扣信息
            OrderDiscount discount = discountMap.get(order.getId());
            dto.setHasDiscount(discount != null);
            dto.setDiscountAmount(discount != null ? discount.getDiscountAmount() : BigDecimal.ZERO);
            
            return dto;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }
    
    /**
     * 为订单发放折扣
     */
    @PostMapping("/{orderId}")
    public Result<Boolean> createDiscount(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> params) {
        
        // 获取当前管理员ID
        Long adminId = UserContext.getCurrentUserId();
        if (adminId == null) {
            return Result.error("未登录或登录已过期");
        }
        
        // 检查当前用户是否为管理员
        User admin = userService.getUserById(adminId);
        if (admin == null || !RoleEnum.ADMIN.equals(admin.getRole())) {
            return Result.error("非管理员用户无权操作");
        }
        
        // 获取折扣金额
        BigDecimal discountAmount;
        try {
            Object amountObj = params.get("discountAmount");
            if (amountObj instanceof Number) {
                discountAmount = new BigDecimal(amountObj.toString());
            } else if (amountObj instanceof String) {
                discountAmount = new BigDecimal((String) amountObj);
            } else {
                return Result.error("折扣金额格式不正确");
            }
        } catch (Exception e) {
            return Result.error("折扣金额格式不正确");
        }
        
        // 创建折扣
        boolean success = orderDiscountService.createDiscount(orderId, discountAmount, adminId);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("折扣发放失败，请检查订单状态和折扣金额");
        }
    }
    
    /**
     * 获取订单折扣详情
     */
    @GetMapping("/{orderId}")
    public Result<OrderDiscount> getOrderDiscount(@PathVariable Long orderId) {
        OrderDiscount discount = orderDiscountService.getByOrderId(orderId);
        if (discount != null) {
            return Result.success(discount);
        } else {
            return Result.error("未找到该订单的折扣信息");
        }
    }
    
    /**
     * 获取商家名称
     */
    private String getMerchantName(Long merchantId) {
        if (merchantId == null) {
            return "未知商家";
        }
        
        try {
            return merchantService.getMerchantById(merchantId).getStoreName();
        } catch (Exception e) {
            return "未知商家";
        }
    }
} 