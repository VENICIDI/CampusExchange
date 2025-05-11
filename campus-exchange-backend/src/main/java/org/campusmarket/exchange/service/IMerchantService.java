package org.campusmarket.exchange.service;

import org.campusmarket.exchange.dto.MerchantProfileVO;
import org.campusmarket.exchange.entity.Merchant;

/**
 * 商家服务接口
 */
public interface IMerchantService {
    
    /**
     * 根据用户ID获取商家信息
     * @param userId 用户ID
     * @return 商家信息
     */
    Merchant getMerchantByUserId(Long userId);
    
    /**
     * 根据商家ID获取商家信息
     * @param id 商家ID
     * @return 商家信息
     */
    Merchant getMerchantById(Long id);
    
    /**
     * 创建商家记录
     * @param merchant 商家信息
     * @return 是否成功
     */
    boolean createMerchant(Merchant merchant);
    
    /**
     * 更新商家信息
     * @param merchant 商家信息
     * @return 是否成功
     */
    boolean updateMerchant(Merchant merchant);
    
    /**
     * 获取商家主页聚合信息（含基础信息、等级、商品、评价等）
     * @param merchantId 商家ID
     * @return 商家主页聚合信息VO
     */
    MerchantProfileVO getMerchantProfile(Long merchantId);
} 