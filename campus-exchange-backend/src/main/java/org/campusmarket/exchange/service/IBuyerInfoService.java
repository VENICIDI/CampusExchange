package org.campusmarket.exchange.service;

import org.campusmarket.exchange.dto.BuyerInfoVO;

/**
 * 买家信息服务接口
 */
public interface IBuyerInfoService {

    /**
     * 获取买家详细信息，包括基本信息和评价
     * @param buyerId 买家用户ID
     * @return 买家信息VO
     */
    BuyerInfoVO getBuyerInfo(Long buyerId);
    
    /**
     * 计算买家好评率
     * @param buyerId 买家用户ID
     * @return 好评率（0-1之间的小数）
     */
    java.math.BigDecimal calculateBuyerPositiveRate(Long buyerId);
} 