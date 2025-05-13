package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.dto.MerchantProfileVO;
import org.campusmarket.exchange.dto.ProductVO;
import org.campusmarket.exchange.entity.Merchant;
import org.campusmarket.exchange.entity.MerchantLevel;
import org.campusmarket.exchange.entity.MerchantServiceReview;
import org.campusmarket.exchange.entity.User;
import org.campusmarket.exchange.enums.ProductStatusEnum;
import org.campusmarket.exchange.mapper.MerchantLevelMapper;
import org.campusmarket.exchange.mapper.MerchantMapper;
import org.campusmarket.exchange.mapper.MerchantServiceReviewMapper;
import org.campusmarket.exchange.mapper.UserMapper;
import org.campusmarket.exchange.service.IMerchantService;
import org.campusmarket.exchange.service.IProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商家服务实现类
 */
@Slf4j
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements IMerchantService {

    @Resource
    private MerchantMapper merchantMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private MerchantLevelMapper merchantLevelMapper;
    
    @Resource
    private MerchantServiceReviewMapper merchantServiceReviewMapper;
    
    @Resource
    private IProductService productService;

    @Override
    public Merchant getMerchantByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        log.info("根据用户ID查询商家信息: {}", userId);
        LambdaQueryWrapper<Merchant> queryWrapper = Wrappers.<Merchant>lambdaQuery()
                .eq(Merchant::getUserId, userId);
        
        return merchantMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean createMerchant(Merchant merchant) {
        if (merchant == null) {
            return false;
        }
        
        // 设置创建和更新时间
        if (merchant.getCreateTime() == null) {
            merchant.setCreateTime(LocalDateTime.now());
        }
        if (merchant.getUpdateTime() == null) {
            merchant.setUpdateTime(LocalDateTime.now());
        }
        
        log.info("创建商家记录: {}", merchant.getStoreName());
        return merchantMapper.insert(merchant) > 0;
    }

    @Override
    public boolean updateMerchant(Merchant merchant) {
        if (merchant == null || merchant.getId() == null) {
            return false;
        }
        
        // 更新时间
        merchant.setUpdateTime(LocalDateTime.now());
        
        log.info("更新商家信息: {}", merchant.getId());
        return merchantMapper.updateById(merchant) > 0;
    }

    @Override
    public Merchant getMerchantById(Long id) {
        if (id == null) {
            return null;
        }
        
        log.info("根据ID查询商家信息: {}", id);
        return merchantMapper.selectById(id);
    }
    
    @Override
    public MerchantProfileVO getMerchantProfile(Long merchantId) {
        log.info("获取商家[{}]主页聚合信息", merchantId);
        
        try {
            // 1. 查询商家基础信息
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null) {
                log.error("商家不存在: {}", merchantId);
                return null;
            }
            
            MerchantProfileVO profileVO = new MerchantProfileVO();
            profileVO.setMerchantId(merchant.getId());
            profileVO.setStoreName(merchant.getStoreName());
            profileVO.setDescription(merchant.getDescription());
            profileVO.setStorePositiveRate(merchant.getStorePositiveRate());
            profileVO.setTotalSalesCount(merchant.getTotalSalesCount());
            profileVO.setTotalSalesAmount(merchant.getTotalSalesAmount());
            profileVO.setLevelId(merchant.getLevelId());
            profileVO.setCreateTime(merchant.getCreateTime());
            
            // 2. 查询用户信息（头像、联系方式）
            try {
                User user = userMapper.selectById(merchant.getUserId());
                if (user != null) {
                    profileVO.setAvatar(user.getAvatar());
                    profileVO.setPhone(user.getPhone());
                    profileVO.setEmail(user.getEmail());
                    profileVO.setWechat(user.getWechat());
                }
            } catch (Exception e) {
                log.error("获取商家用户信息失败: {}", e.getMessage(), e);
            }
            
            // 3. 查询商家等级信息
            try {
                MerchantLevel level = merchantLevelMapper.selectById(merchant.getLevelId());
                if (level != null) {
                    profileVO.setLevelName(level.getLevelName());
                    profileVO.setLevelDesc(level.getDescription());
                    profileVO.setCommissionRate(level.getCommissionRate());
                }
            } catch (Exception e) {
                log.error("获取商家等级信息失败: {}", e.getMessage(), e);
            }
            
            // 4. 为避免循环依赖，初始化一个空的商品列表
            // 前端可以在需要时单独调用获取商品列表的接口
            profileVO.setProducts(new ArrayList<>());
            
            // 5. 查询商家服务评价（限制展示5条）
            try {
                log.info("开始查询商家[{}]服务评价", merchantId);
                List<MerchantServiceReview> reviews = merchantServiceReviewMapper.selectByMerchantId(merchantId, 5);
                log.info("商家[{}]服务评价查询结果: {}", merchantId, reviews != null ? reviews.size() : 0);
                
                if (reviews != null && !reviews.isEmpty()) {
                    List<MerchantProfileVO.MerchantServiceReviewVO> reviewVOs = new ArrayList<>();
                    
                    for (MerchantServiceReview review : reviews) {
                        MerchantProfileVO.MerchantServiceReviewVO reviewVO = new MerchantProfileVO.MerchantServiceReviewVO();
                        reviewVO.setId(review.getId());
                        reviewVO.setUserId(review.getUserId());
                        reviewVO.setServiceAttitudeRating(review.getServiceAttitudeRating());
                        reviewVO.setContent(review.getContent());
                        reviewVO.setCreateTime(review.getCreateTime());
                        
                        // 查询评价用户名称
                        User reviewer = userMapper.selectById(review.getUserId());
                        if (reviewer != null) {
                            reviewVO.setUsername(reviewer.getUsername());
                        }
                        
                        reviewVOs.add(reviewVO);
                    }
                    
                    profileVO.setReviews(reviewVOs);
                } else {
                    profileVO.setReviews(new ArrayList<>());
                }
            } catch (Exception e) {
                log.error("获取商家评价失败: {}", e.getMessage(), e);
                // 出错时设置空列表，避免整个接口失败
                profileVO.setReviews(new ArrayList<>());
            }
            
            return profileVO;
        } catch (Exception e) {
            log.error("获取商家主页聚合信息失败: {}", e.getMessage(), e);
            return null;
        }
    }
} 