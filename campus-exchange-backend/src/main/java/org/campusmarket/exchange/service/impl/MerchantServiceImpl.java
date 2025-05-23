package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.dto.MerchantProfileVO;
import org.campusmarket.exchange.dto.PageResult;
import org.campusmarket.exchange.dto.ProductVO;
import org.campusmarket.exchange.entity.Merchant;
import org.campusmarket.exchange.entity.MerchantLevel;
import org.campusmarket.exchange.entity.MerchantServiceReview;
import org.campusmarket.exchange.entity.Product;
import org.campusmarket.exchange.entity.User;
import org.campusmarket.exchange.enums.ProductStatusEnum;
import org.campusmarket.exchange.mapper.MerchantLevelMapper;
import org.campusmarket.exchange.mapper.MerchantMapper;
import org.campusmarket.exchange.mapper.MerchantServiceReviewMapper;
import org.campusmarket.exchange.mapper.ProductMapper;
import org.campusmarket.exchange.mapper.UserMapper;
import org.campusmarket.exchange.service.IMerchantService;
import org.campusmarket.exchange.service.IProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Resource
    private ProductMapper productMapper;

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

    @Override
    public Page<Merchant> getAllMerchants(Integer pageNum, Integer pageSize, String keyword) {
        log.info("分页查询商家列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);
        
        Page<Merchant> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Merchant> queryWrapper = Wrappers.<Merchant>lambdaQuery();
        
        // 如果有关键词，添加模糊查询条件
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(Merchant::getStoreName, keyword);
        }
        
        // 按照更新时间倒序排列
        queryWrapper.orderByDesc(Merchant::getUpdateTime);
        
        return merchantMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional
    public boolean updateMerchantLevel(Long merchantId, Long levelId) {
        log.info("更新商家等级: merchantId={}, levelId={}", merchantId, levelId);
        
        if (merchantId == null || levelId == null) {
            log.error("商家ID或等级ID为空");
            return false;
        }
        
        // 检查商家是否存在
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            log.error("商家不存在: {}", merchantId);
            return false;
        }
        
        // 检查等级是否存在
        MerchantLevel level = merchantLevelMapper.selectById(levelId);
        if (level == null) {
            log.error("商家等级不存在: {}", levelId);
            return false;
        }
        
        // 更新商家等级
        merchant.setLevelId(levelId);
        merchant.setUpdateTime(LocalDateTime.now());
        
        return merchantMapper.updateById(merchant) > 0;
    }

    @Override
    @Transactional
    public int takeDownAllProducts(Long merchantId) {
        log.info("批量下架商家所有商品: merchantId={}", merchantId);
        
        if (merchantId == null) {
            log.error("商家ID为空");
            return -1;
        }
        
        // 检查商家是否存在
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            log.error("商家不存在: {}", merchantId);
            return -1;
        }
        
        try {
            // 查询商家在售的商品
            LambdaQueryWrapper<Product> queryWrapper = Wrappers.<Product>lambdaQuery()
                    .eq(Product::getMerchantId, merchantId)
                    .eq(Product::getStatus, ProductStatusEnum.ON_SALE);
            
            List<Product> onSaleProducts = productMapper.selectList(queryWrapper);
            
            if (onSaleProducts.isEmpty()) {
                log.info("商家[{}]没有在售商品", merchantId);
                return 0;
            }
            
            // 批量更新商品状态为"已锁定"
            LambdaUpdateWrapper<Product> updateWrapper = Wrappers.<Product>lambdaUpdate()
                    .eq(Product::getMerchantId, merchantId)
                    .eq(Product::getStatus, ProductStatusEnum.ON_SALE)
                    .set(Product::getStatus, ProductStatusEnum.LOCKED)
                    .set(Product::getUpdateTime, LocalDateTime.now());
            
            int count = productMapper.update(null, updateWrapper);
            log.info("商家[{}]下架商品完成，共下架{}件商品", merchantId, count);
            
            return count;
        } catch (Exception e) {
            log.error("批量下架商家商品失败: {}", e.getMessage(), e);
            return -1;
        }
    }

    @Override
    public List<MerchantLevel> getAllMerchantLevels() {
        log.info("获取所有商家等级");
        return merchantLevelMapper.selectList(null);
    }
    
    @Override
    public MerchantLevel getMerchantLevelById(Long levelId) {
        if (levelId == null) {
            return null;
        }
        log.info("根据ID查询商家等级: {}", levelId);
        return merchantLevelMapper.selectById(levelId);
    }

    @Override
    public PageResult<Merchant> getMerchantList(Integer page, Integer size, String keyword) {
        log.info("分页查询商家列表: page={}, size={}, keyword={}", page, size, keyword);
        
        // 创建分页对象
        Page<Merchant> pageParam = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<Merchant> queryWrapper = Wrappers.<Merchant>lambdaQuery();
        
        // 如果关键词不为空，按店铺名称模糊查询
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(Merchant::getStoreName, keyword);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Merchant::getCreateTime);
        
        // 执行查询
        Page<Merchant> resultPage = merchantMapper.selectPage(pageParam, queryWrapper);
        
        // 转换为自定义分页结果
        PageResult<Merchant> pageResult = new PageResult<>();
        pageResult.setRecords(resultPage.getRecords());
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setCurrent((int) resultPage.getCurrent());
        pageResult.setSize((int) resultPage.getSize());
        pageResult.setPages(resultPage.getPages());
        
        return pageResult;
    }
} 