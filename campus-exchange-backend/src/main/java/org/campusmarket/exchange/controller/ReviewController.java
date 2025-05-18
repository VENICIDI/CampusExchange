package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.util.UserContext;
import org.campusmarket.exchange.dto.BuyerReviewDTO;
import org.campusmarket.exchange.dto.OrderReviewDTO;
import org.campusmarket.exchange.entity.ProductReview;
import org.campusmarket.exchange.entity.MerchantServiceReview;
import org.campusmarket.exchange.entity.BuyerReviewByMerchant;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.service.IReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 评价管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    
    @Resource
    private IReviewService reviewService;
    
    @Resource
    private HttpServletRequest request;
    
    /**
     * 用户提交订单评价
     * @param orderReviewDTO 订单评价数据
     * @return 操作结果
     */
    @PostMapping("/order")
    public Result<Boolean> submitOrderReview(@RequestBody @Valid OrderReviewDTO orderReviewDTO) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        
        boolean success = reviewService.submitOrderReview(userId, orderReviewDTO);
        return Result.success(success);
    }
    
    /**
     * 商家提交买家评价
     * @param orderNo 订单号
     * @param buyerReviewDTO 买家评价数据
     * @return 操作结果
     */
    @PostMapping("/buyer/{orderNo}")
    public Result<Boolean> submitBuyerReview(
            @PathVariable String orderNo,
            @RequestBody @Valid BuyerReviewDTO buyerReviewDTO,
            @RequestHeader(value = "X-Merchant-Id", required = true) Long merchantId) {
        if (merchantId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录商家账号");
        }
        
        boolean success = reviewService.submitBuyerReview(merchantId, orderNo, buyerReviewDTO);
        return Result.success(success);
    }
    
    /**
     * 获取商品评价列表
     * @param productId 商品ID
     * @return 评价列表
     */
    @GetMapping("/product/{productId}")
    public Result<List<ProductReview>> getProductReviews(@PathVariable Long productId) {
        List<ProductReview> reviews = reviewService.getProductReviews(productId);
        return Result.success(reviews);
    }
    
    /**
     * 检查用户是否已评价订单
     * @param orderNo 订单号
     * @return 是否已评价
     */
    @GetMapping("/check/{orderNo}")
    public Result<Boolean> hasReviewedOrder(@PathVariable String orderNo) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        
        boolean reviewed = reviewService.hasReviewedOrder(userId, orderNo);
        return Result.success(reviewed);
    }
    
    /**
     * 获取用户未评价订单列表
     * @return 未评价订单号列表
     */
    @GetMapping("/unreviewedOrders")
    public Result<List<String>> getUnreviewedOrders() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        
        List<String> orderNos = reviewService.getUnreviewedOrders(userId);
        return Result.success(orderNos);
    }
    
    /**
     * 获取商家服务评分
     * @param merchantId 商家ID
     * @return 评分
     */
    @GetMapping("/merchant/{merchantId}/rating")
    public Result<Double> getMerchantServiceRating(@PathVariable Long merchantId) {
        Double rating = reviewService.getMerchantServiceRating(merchantId);
        return Result.success(rating);
    }
    
    /**
     * 获取订单的所有评价信息（商品评价、商家服务评价、买家评价）
     * @param orderNo 订单号
     * @return 评价信息
     */
    @GetMapping("/order/{orderNo}/all")
    public Result<Map<String, Object>> getOrderReviews(@PathVariable String orderNo) {
        // 创建结果Map
        Map<String, Object> result = new HashMap<>();
        
        // 尝试获取买家评价信息（如果是商家用户）
        Long merchantId = null;
        try {
            String merchantIdHeader = request.getHeader("X-Merchant-Id");
            if (merchantIdHeader != null && !merchantIdHeader.isEmpty()) {
                merchantId = Long.parseLong(merchantIdHeader);
            }
        } catch (Exception e) {
            log.warn("解析商家ID失败", e);
        }
        
        // 如果是商家，获取买家评价信息
        if (merchantId != null) {
            BuyerReviewByMerchant buyerReview = reviewService.getBuyerReviewByOrderNo(merchantId, orderNo);
            result.put("buyerReview", buyerReview);
        }
        
        // 获取商品评价信息
        List<ProductReview> productReviews = reviewService.getProductReviewsByOrderNo(orderNo);
        result.put("productReviews", productReviews);
        
        // 获取商家服务评价信息
        MerchantServiceReview merchantReview = reviewService.getMerchantServiceReviewByOrderNo(orderNo);
        result.put("merchantReview", merchantReview);
        
        return Result.success(result);
    }
} 