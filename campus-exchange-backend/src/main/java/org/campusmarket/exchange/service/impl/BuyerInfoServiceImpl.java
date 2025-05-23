package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.dto.BuyerInfoVO;
import org.campusmarket.exchange.entity.BuyerReviewByMerchant;
import org.campusmarket.exchange.entity.Merchant;
import org.campusmarket.exchange.entity.Order;
import org.campusmarket.exchange.entity.User;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.mapper.BuyerReviewByMerchantMapper;
import org.campusmarket.exchange.mapper.MerchantMapper;
import org.campusmarket.exchange.mapper.OrderMapper;
import org.campusmarket.exchange.mapper.UserMapper;
import org.campusmarket.exchange.service.IBuyerInfoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 买家信息服务实现类
 */
@Slf4j
@Service
public class BuyerInfoServiceImpl implements IBuyerInfoService {

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private BuyerReviewByMerchantMapper buyerReviewByMerchantMapper;
    
    @Resource
    private MerchantMapper merchantMapper;

    @Override
    public BuyerInfoVO getBuyerInfo(Long buyerId) {
        log.info("获取买家[{}]信息和评价", buyerId);
        
        // 查询用户基本信息
        User user = userMapper.selectById(buyerId);
        if (user == null) {
            log.warn("用户[{}]不存在", buyerId);
            return null;
        }
        
        BuyerInfoVO buyerInfoVO = new BuyerInfoVO();
        buyerInfoVO.setId(user.getId());
        buyerInfoVO.setUsername(user.getUsername());
        buyerInfoVO.setAvatar(user.getAvatar());
        
        // 格式化注册时间
        if (user.getCreateTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            buyerInfoVO.setRegisterTime(user.getCreateTime().format(formatter));
        }
        
        // 查询用户交易次数（已完成订单）
        LambdaQueryWrapper<Order> orderWrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getUserId, buyerId)
                .eq(Order::getStatus, OrderStatusEnum.COMPLETED);
        int completedOrderCount = orderMapper.selectCount(orderWrapper).intValue();
        buyerInfoVO.setTotalTransactionCount(completedOrderCount);
        
        // 计算买家好评率
        buyerInfoVO.setPositiveRate(calculateBuyerPositiveRate(buyerId));
        
        // 获取买家的评价列表
        List<BuyerInfoVO.BuyerReviewInfo> reviewInfoList = getBuyerReviews(buyerId);
        buyerInfoVO.setReviews(reviewInfoList);
        
        return buyerInfoVO;
    }

    @Override
    public BigDecimal calculateBuyerPositiveRate(Long buyerId) {
        try {
            // 查询买家的所有评价
            LambdaQueryWrapper<BuyerReviewByMerchant> reviewWrapper = Wrappers.<BuyerReviewByMerchant>lambdaQuery()
                    .eq(BuyerReviewByMerchant::getBuyerUserId, buyerId);
            List<BuyerReviewByMerchant> reviews = buyerReviewByMerchantMapper.selectList(reviewWrapper);
            
            if (reviews == null || reviews.isEmpty()) {
                return BigDecimal.ONE; // 默认好评率100%
            }
            
            // 计算好评率（4分及以上为好评）
            long positiveCount = reviews.stream()
                    .filter(review -> review.getRatingScore() >= 4)
                    .count();
            
            // 计算好评率（值为0-1之间的小数）
            BigDecimal positiveRate = new BigDecimal((double) positiveCount / reviews.size())
                    .setScale(2, RoundingMode.HALF_UP);
            
            return positiveRate;
        } catch (Exception e) {
            log.error("计算买家好评率出错: {}", e.getMessage(), e);
            return BigDecimal.ONE; // 出错时默认好评率100%
        }
    }

    /**
     * 获取买家评价列表
     * @param buyerId 买家用户ID
     * @return 评价列表
     */
    private List<BuyerInfoVO.BuyerReviewInfo> getBuyerReviews(Long buyerId) {
        List<BuyerInfoVO.BuyerReviewInfo> result = new ArrayList<>();
        
        // 查询买家所有评价
        LambdaQueryWrapper<BuyerReviewByMerchant> reviewWrapper = Wrappers.<BuyerReviewByMerchant>lambdaQuery()
                .eq(BuyerReviewByMerchant::getBuyerUserId, buyerId)
                .orderByDesc(BuyerReviewByMerchant::getCreateTime);
        List<BuyerReviewByMerchant> reviews = buyerReviewByMerchantMapper.selectList(reviewWrapper);
        
        if (reviews == null || reviews.isEmpty()) {
            return result;
        }
        
        // 获取所有商家ID
        List<Long> merchantIds = new ArrayList<>();
        List<Long> orderIds = new ArrayList<>();
        for (BuyerReviewByMerchant review : reviews) {
            merchantIds.add(review.getMerchantId());
            orderIds.add(review.getOrderId());
        }
        
        // 批量查询商家信息
        Map<Long, String> merchantNameMap = new HashMap<>();
        if (!merchantIds.isEmpty()) {
            List<Merchant> merchants = merchantMapper.selectBatchIds(merchantIds);
            for (Merchant merchant : merchants) {
                merchantNameMap.put(merchant.getId(), merchant.getStoreName());
            }
        }
        
        // 批量查询订单信息
        Map<Long, String> orderNoMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<Order> orders = orderMapper.selectBatchIds(orderIds);
            for (Order order : orders) {
                orderNoMap.put(order.getId(), order.getOrderNo());
            }
        }
        
        // 构建评价列表
        for (BuyerReviewByMerchant review : reviews) {
            BuyerInfoVO.BuyerReviewInfo reviewInfo = new BuyerInfoVO.BuyerReviewInfo();
            reviewInfo.setId(review.getId());
            reviewInfo.setRatingScore(review.getRatingScore());
            reviewInfo.setContent(review.getContent());
            
            // 格式化评价时间
            if (review.getCreateTime() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                reviewInfo.setCreateTime(review.getCreateTime().format(formatter));
            }
            
            // 设置商家名称
            reviewInfo.setMerchantName(merchantNameMap.getOrDefault(review.getMerchantId(), "未知商家"));
            
            // 设置订单号
            reviewInfo.setOrderNo(orderNoMap.getOrDefault(review.getOrderId(), "未知订单"));
            
            result.add(reviewInfo);
        }
        
        return result;
    }
} 