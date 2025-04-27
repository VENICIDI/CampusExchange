// 文件路径: ...\src\main\java\org\campusmarket\exchange\mapper\MerchantMapper.java
package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.campusmarket.exchange.entity.Merchant;

/**
 * 商家信息表 Mapper 接口
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
    // 自定义查询方法可以在此添加
}