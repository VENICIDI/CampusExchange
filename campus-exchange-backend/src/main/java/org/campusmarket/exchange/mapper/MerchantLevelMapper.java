package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.campusmarket.exchange.entity.MerchantLevel;

/**
 * 商家等级 Mapper 接口
 */
@Mapper
public interface MerchantLevelMapper extends BaseMapper<MerchantLevel> {
    // 自定义查询方法可以在此添加
} 