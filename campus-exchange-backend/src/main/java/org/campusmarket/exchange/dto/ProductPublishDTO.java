package org.campusmarket.exchange.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.campusmarket.exchange.enums.ProductConditionEnum;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品发布DTO
 */
@Data
public class ProductPublishDTO {
    
    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String name;
    
    /**
     * 分类ID
     */
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    
    /**
     * 原价
     */
    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;
    
    /**
     * 现价/折扣价
     */
    @NotNull(message = "现价不能为空")
    @DecimalMin(value = "0.01", message = "现价必须大于0")
    private BigDecimal currentPrice;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 新旧程度
     */
    @NotNull(message = "新旧程度不能为空")
    private ProductConditionEnum condition;
    
    /**
     * 是否可议价
     */
    private Boolean negotiable = false;
    
    /**
     * 库存数量
     */
    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量必须大于0")
    private Integer stock;
    
    /**
     * 商品尺寸
     */
    private String size;
    
    /**
     * 使用说明
     */
    private String usageInstructions;
    
    /**
     * 商品图片URL列表
     */
    private List<String> imageUrls;
} 