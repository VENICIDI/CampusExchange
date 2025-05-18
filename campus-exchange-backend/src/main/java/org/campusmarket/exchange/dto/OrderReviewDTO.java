package org.campusmarket.exchange.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单评价数据传输对象，整合所有评价信息
 */
@Data
public class OrderReviewDTO {
    
    /**
     * 订单号
     */
    @NotNull(message = "订单号不能为空")
    private String orderNo;
    
    /**
     * 商品评价列表
     */
    @Valid
    @NotEmpty(message = "商品评价不能为空")
    private List<OrderItemReview> productReviews;
    
    /**
     * 商家服务评价
     */
    @Valid
    @NotNull(message = "商家服务评价不能为空")
    private MerchantServiceReviewDTO merchantServiceReview;
    
    /**
     * 单个商品评价
     */
    @Data
    public static class OrderItemReview {
        
        /**
         * 订单项ID
         */
        @NotNull(message = "订单项ID不能为空")
        private Long orderItemId;
        
        /**
         * 商品评价
         */
        @Valid
        @NotNull(message = "商品评价不能为空")
        private ProductReviewDTO productReview;
    }
} 