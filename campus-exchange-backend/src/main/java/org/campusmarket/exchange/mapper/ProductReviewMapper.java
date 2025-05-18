package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.campusmarket.exchange.entity.ProductReview;

import java.util.List;

/**
 * 商品评价 Mapper 接口
 */
@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {
    
    /**
     * 根据商品ID查询评价列表
     * @param productId 商品ID
     * @param limit 数量限制
     * @return 评价列表
     */
    List<ProductReview> selectByProductId(@Param("productId") Long productId, @Param("limit") Integer limit);
    
    /**
     * 查询商品的平均评分
     * @param productId 商品ID
     * @return 平均评分
     */
    Double selectAvgRatingByProductId(@Param("productId") Long productId);
} 