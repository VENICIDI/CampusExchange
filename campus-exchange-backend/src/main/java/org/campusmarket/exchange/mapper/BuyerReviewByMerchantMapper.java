package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.campusmarket.exchange.entity.BuyerReviewByMerchant;

import java.util.List;

/**
 * 买家评价 Mapper 接口
 */
@Mapper
public interface BuyerReviewByMerchantMapper extends BaseMapper<BuyerReviewByMerchant> {
    
    /**
     * 根据买家用户ID查询评价列表
     * @param buyerUserId 买家用户ID
     * @param limit 数量限制
     * @return 评价列表
     */
    List<BuyerReviewByMerchant> selectByBuyerUserId(@Param("buyerUserId") Long buyerUserId, @Param("limit") Integer limit);
} 