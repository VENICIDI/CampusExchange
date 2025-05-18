package org.campusmarket.exchange.service;

import org.campusmarket.exchange.dto.BuyerReviewDTO;
import org.campusmarket.exchange.dto.OrderReviewDTO;
import org.campusmarket.exchange.dto.ProductReviewDTO;
import org.campusmarket.exchange.entity.ProductReview;
import org.campusmarket.exchange.entity.MerchantServiceReview;
import org.campusmarket.exchange.entity.BuyerReviewByMerchant;

import java.util.List;

/**
 * 评价服务接口
 */
public interface IReviewService {
    
    /**
     * 提交商品评价和商家服务评价（买家评价）
     * @param userId 用户ID
     * @param orderReviewDTO 订单评价DTO
     * @return 是否成功
     */
    boolean submitOrderReview(Long userId, OrderReviewDTO orderReviewDTO);
    
    /**
     * 获取商品评价列表
     * @param productId 商品ID
     * @return 商品评价列表
     */
    List<ProductReview> getProductReviews(Long productId);
    
    /**
     * 商家对买家进行评价
     * @param merchantId 商家ID
     * @param orderNo 订单号
     * @param buyerReviewDTO 买家评价DTO
     * @return 是否成功
     */
    boolean submitBuyerReview(Long merchantId, String orderNo, BuyerReviewDTO buyerReviewDTO);
    
    /**
     * 检查用户是否已评价订单
     * @param userId 用户ID
     * @param orderNo 订单号
     * @return 是否已评价
     */
    boolean hasReviewedOrder(Long userId, String orderNo);
    
    /**
     * 获取未评价订单列表
     * @param userId 用户ID
     * @return 未评价订单列表
     */
    List<String> getUnreviewedOrders(Long userId);
    
    /**
     * 根据订单自动完成交易（未评价的情况）
     * @param orderNo 订单号
     * @return 是否成功
     */
    boolean autoCompleteOrderByTimeout(String orderNo);
    
    /**
     * 获取商家服务评分
     * @param merchantId 商家ID
     * @return 平均评分
     */
    Double getMerchantServiceRating(Long merchantId);
    
    /**
     * 获取订单商品评价列表
     * @param orderNo 订单号
     * @return 商品评价列表
     */
    List<ProductReview> getProductReviewsByOrderNo(String orderNo);
    
    /**
     * 获取订单商家服务评价
     * @param orderNo 订单号
     * @return 商家服务评价
     */
    MerchantServiceReview getMerchantServiceReviewByOrderNo(String orderNo);
    
    /**
     * 获取订单买家评价（由商家评价）
     * @param merchantId 商家ID
     * @param orderNo 订单号
     * @return 买家评价
     */
    BuyerReviewByMerchant getBuyerReviewByOrderNo(Long merchantId, String orderNo);
} 