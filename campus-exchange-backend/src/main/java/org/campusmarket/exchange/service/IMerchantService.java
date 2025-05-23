package org.campusmarket.exchange.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.campusmarket.exchange.dto.MerchantProfileVO;
import org.campusmarket.exchange.entity.Merchant;
import org.campusmarket.exchange.entity.MerchantLevel;
import org.campusmarket.exchange.dto.PageResult;

import java.util.List;

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
    
    /**
     * 分页获取所有商家列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词(可选)
     * @return 商家分页列表
     */
    Page<Merchant> getAllMerchants(Integer pageNum, Integer pageSize, String keyword);
    
    /**
     * 更新商家等级
     * @param merchantId 商家ID
     * @param levelId 等级ID
     * @return 是否更新成功
     */
    boolean updateMerchantLevel(Long merchantId, Long levelId);
    
    /**
     * 批量下架商家的所有商品
     * @param merchantId 商家ID
     * @return 受影响的商品数量
     */
    int takeDownAllProducts(Long merchantId);
    
    /**
     * 获取所有商家等级
     * @return 商家等级列表
     */
    List<MerchantLevel> getAllMerchantLevels();
    
    /**
     * 获取商家等级信息
     * @param levelId 等级ID
     * @return 等级信息
     */
    MerchantLevel getMerchantLevelById(Long levelId);
    
    /**
     * 分页获取商家列表
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词（店铺名称）
     * @return 分页商家列表
     */
    PageResult<Merchant> getMerchantList(Integer page, Integer size, String keyword);
} 