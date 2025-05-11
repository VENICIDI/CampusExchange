package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.campusmarket.exchange.entity.MerchantServiceReview;

import java.util.List;

/**
 * 商家服务评价 Mapper 接口
 */
@Mapper
public interface MerchantServiceReviewMapper extends BaseMapper<MerchantServiceReview> {
    /**
     * 根据商家ID查询评价列表
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @return 评价列表
     */
    List<MerchantServiceReview> selectByMerchantId(@Param("merchantId") Long merchantId, @Param("limit") Integer limit);
} 