package org.campusmarket.exchange.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家服务评价数据传输对象
 */
@Data
public class MerchantServiceReviewDTO {
    
    @NotNull(message = "服务态度评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer serviceAttitudeRating;
    
    @Size(max = 1000, message = "评价内容不能超过1000个字符")
    private String content;
} 