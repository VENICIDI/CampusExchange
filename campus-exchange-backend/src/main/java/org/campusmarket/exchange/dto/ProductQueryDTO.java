package org.campusmarket.exchange.dto;

import lombok.Data;

// 商品查询DTO
@Data
public class ProductQueryDTO {
    
    // 关键词
    private String keyword;
    
    // 分类ID
    private Long categoryId;
    
    // 商家ID
    private Long merchantId;
    
    // 最低价格
    private Double minPrice;
    
    // 最高价格
    private Double maxPrice;
    
    // 排序字段：price-价格，sales_count-销量，average_rating-评分，publish_time-发布时间
    private String orderBy = "publish_time";
    
    // 排序方向：asc-升序，desc-降序
    private String orderDirection = "desc";
    
    // 页码
    private Integer pageNum = 1;
    
    // 每页数量
    private Integer pageSize = 10;
    
    /**
     * 获取排序字段名
     * @return 数据库字段名
     */
    public String getSortField() {
        // 从orderBy映射到数据库字段名
        switch (this.orderBy) {
            case "price":
                return "current_price";
            case "sales":
            case "sales_count":
                return "sales_count";
            case "rating":
            case "average_rating":
                return "average_rating";
            case "publish_time":
                return "publish_time";
            default:
                return "publish_time";
        }
    }
    
    /**
     * 获取排序方向
     * @return 升序还是降序
     */
    public String getSortOrder() {
        // 返回排序方向
        return this.orderDirection;
    }
} 