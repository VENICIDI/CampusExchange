package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.campusmarket.exchange.entity.PointsTransaction;

/**
 * 积分交易记录Mapper接口
 */
@Mapper
public interface PointsTransactionMapper extends BaseMapper<PointsTransaction> {
} 