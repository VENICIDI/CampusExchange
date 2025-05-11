package org.campusmarket.exchange.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家主页聚合信息VO
 */
@Data
public class MerchantProfileVO {
    // 商家ID
    private Long merchantId;
    // 店铺名称
    private String storeName;
    // 店铺头像（取自user.avatar）
    private String avatar;
    // 店铺公告/简介
    private String description;
    // 店铺好评率
    private BigDecimal storePositiveRate;
    // 总销量
    private Integer totalSalesCount;
    // 总销售额
    private BigDecimal totalSalesAmount;
    // 商家等级ID
    private Long levelId;
    // 商家等级名称
    private String levelName;
    // 商家等级描述
    private String levelDesc;
    // 手续费率
    private BigDecimal commissionRate;
    // 联系方式
    private String phone;
    private String wechat;
    private String email;
    // 店铺创建时间
    private LocalDateTime createTime;
    // 商品列表（只展示部分字段，可用ProductVO简化版）
    private List<ProductVO> products;
    // 商家服务评价（可选：好评率、评价内容等）
    private List<MerchantServiceReviewVO> reviews;

    // 内部类：商家服务评价VO
    @Data
    public static class MerchantServiceReviewVO {
        private Long id;
        private Long userId;
        private String username;
        private Integer serviceAttitudeRating;
        private String content;
        private LocalDateTime createTime;
    }
} 