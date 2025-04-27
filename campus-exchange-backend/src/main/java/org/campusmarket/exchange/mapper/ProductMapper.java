package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.campusmarket.exchange.entity.Product;
import org.campusmarket.exchange.dto.ProductVO;

/**
 * 商品表 Mapper 接口
 */
public interface ProductMapper extends BaseMapper<Product> {
    // 自定义查询方法可以在此添加
} 