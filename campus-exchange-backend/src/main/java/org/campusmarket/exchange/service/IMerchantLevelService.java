package org.campusmarket.exchange.service;

import org.campusmarket.exchange.entity.MerchantLevel;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商家等级服务接口
 */
public interface IMerchantLevelService {
    
    /**
     * 获取所有商家等级列表
     * @return 商家等级列表
     */
    List<MerchantLevel> getAllLevels();
    
    /**
     * 根据ID获取商家等级
     * @param id 商家等级ID
     * @return 商家等级信息
     */
    MerchantLevel getLevelById(Long id);
    
    /**
     * 添加商家等级
     * @param merchantLevel 商家等级信息
     * @return 是否添加成功
     */
    boolean addLevel(MerchantLevel merchantLevel);
    
    /**
     * 更新商家等级信息
     * @param merchantLevel 商家等级信息
     * @return 是否更新成功
     */
    boolean updateLevel(MerchantLevel merchantLevel);
    
    /**
     * 根据ID删除商家等级
     * @param id 商家等级ID
     * @return 是否删除成功
     */
    boolean deleteLevel(Long id);
    
    /**
     * 更新商家等级的手续费率
     * @param id 商家等级ID
     * @param rate 手续费率
     * @return 是否更新成功
     */
    boolean updateCommissionRate(Long id, BigDecimal rate);
} 