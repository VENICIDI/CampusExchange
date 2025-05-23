package org.campusmarket.exchange.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 买家信息展示VO
 */
@Data
public class BuyerInfoVO {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 注册时间
     */
    private String registerTime;
    
    /**
     * 总交易次数
     */
    private Integer totalTransactionCount;
    
    /**
     * 买家好评率
     */
    private BigDecimal positiveRate;
    
    /**
     * 买家评价列表
     */
    private List<BuyerReviewInfo> reviews;
    
    /**
     * 买家评价信息
     */
    @Data
    public static class BuyerReviewInfo {
        /**
         * 评价ID
         */
        private Long id;
        
        /**
         * 评分
         */
        private Integer ratingScore;
        
        /**
         * 评价内容
         */
        private String content;
        
        /**
         * 评价时间
         */
        private String createTime;
        
        /**
         * 商家名称
         */
        private String merchantName;
        
        /**
         * 订单号
         */
        private String orderNo;
    }
} 