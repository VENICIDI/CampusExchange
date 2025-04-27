package org.campusmarket.exchange.dto;

import lombok.Data;

/**
 * 商品查询DTO
 */
@Data
public class ProductQueryDTO {
    
    /**
     * 关键词
     */
    private String keyword;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 商家ID
     */
    private Long merchantId;
    
    /**
     * 最低价格
     */
    private Double minPrice;
    
    /**
     * 最高价格
     */
    private Double maxPrice;
    
    /**
     * 排序字段：price-价格，sales-销量，rating-评分，time-发布时间
     */
    private String orderBy = "time";
    
    /**
     * 排序方向：asc-升序，desc-降序
     */
    private String orderDirection = "desc";
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
} 