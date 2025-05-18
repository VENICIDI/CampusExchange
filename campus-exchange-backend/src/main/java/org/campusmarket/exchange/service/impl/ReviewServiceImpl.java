package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.dto.BuyerReviewDTO;
import org.campusmarket.exchange.dto.MerchantServiceReviewDTO;
import org.campusmarket.exchange.dto.OrderReviewDTO;
import org.campusmarket.exchange.dto.ProductReviewDTO;
import org.campusmarket.exchange.entity.*;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.enums.ReviewStatusEnum;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.mapper.*;
import org.campusmarket.exchange.service.IReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价服务实现类
 */
@Slf4j
@Service
public class ReviewServiceImpl implements IReviewService {

    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private OrderItemMapper orderItemMapper;
    
    @Resource
    private ProductReviewMapper productReviewMapper;
    
    @Resource
    private MerchantServiceReviewMapper merchantServiceReviewMapper;
    
    @Resource
    private BuyerReviewByMerchantMapper buyerReviewByMerchantMapper;
    
    @Resource
    private ProductMapper productMapper;
    
    @Resource
    private MerchantMapper merchantMapper;

    /**
     * 提交商品评价和商家服务评价（买家评价）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitOrderReview(Long userId, OrderReviewDTO orderReviewDTO) {
        log.info("用户[{}]提交订单[{}]评价", userId, orderReviewDTO.getOrderNo());
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> orderQuery = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderReviewDTO.getOrderNo())
                .eq(Order::getUserId, userId);
        Order order = orderMapper.selectOne(orderQuery);
        
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 2. 校验订单状态
        if (order.getStatus() != OrderStatusEnum.RECEIVED && order.getStatus() != OrderStatusEnum.COMPLETED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "只能评价已收货或已完成的订单，当前状态: " + order.getStatus().getDesc());
        }
        
        // 3. 查询订单项
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
        if (orderItems == null || orderItems.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "订单项不存在");
        }
        
        // 4. 校验订单项是否已评价
        for (OrderItem item : orderItems) {
            if (item.getReviewStatus() == ReviewStatusEnum.REVIEWED) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "该订单已评价");
            }
        }
        
        // 5. 保存商品评价
        for (OrderReviewDTO.OrderItemReview itemReview : orderReviewDTO.getProductReviews()) {
            // 5.1 校验订单项是否属于该订单
            OrderItem orderItem = orderItems.stream()
                    .filter(item -> item.getId().equals(itemReview.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                            "订单项不存在或不属于该订单"));
            
            // 5.2 创建商品评价
            ProductReview productReview = new ProductReview();
            productReview.setOrderItemId(itemReview.getOrderItemId());
            productReview.setUserId(userId);
            productReview.setProductId(orderItem.getProductId());
            productReview.setMerchantId(orderItem.getMerchantId());
            productReview.setRatingScore(itemReview.getProductReview().getRatingScore());
            productReview.setContent(itemReview.getProductReview().getContent());
            productReview.setCreateTime(LocalDateTime.now());
            
            productReviewMapper.insert(productReview);
            
            // 5.3 更新订单项评价状态
            OrderItem updateItem = new OrderItem();
            updateItem.setId(orderItem.getId());
            updateItem.setReviewStatus(ReviewStatusEnum.REVIEWED);
            orderItemMapper.updateById(updateItem);
            
            // 5.4 更新商品平均评分
            updateProductAverageRating(orderItem.getProductId());
        }
        
        // 6. 保存商家服务评价
        MerchantServiceReview merchantServiceReview = new MerchantServiceReview();
        merchantServiceReview.setOrderId(order.getId());
        merchantServiceReview.setUserId(userId);
        merchantServiceReview.setMerchantId(order.getMerchantId());
        merchantServiceReview.setServiceAttitudeRating(orderReviewDTO.getMerchantServiceReview().getServiceAttitudeRating());
        merchantServiceReview.setContent(orderReviewDTO.getMerchantServiceReview().getContent());
        merchantServiceReview.setCreateTime(LocalDateTime.now());
        
        merchantServiceReviewMapper.insert(merchantServiceReview);
        
        // 7. 更新商家好评率
        updateMerchantPositiveRate(order.getMerchantId());
        
        // 8. 更新订单状态为已完成
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.COMPLETED);
        updateOrder.setCompletionTime(LocalDateTime.now());
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(updateOrder);
        
        log.info("用户[{}]评价订单[{}]成功", userId, orderReviewDTO.getOrderNo());
        return true;
    }

    /**
     * 获取商品评价列表
     */
    @Override
    public List<ProductReview> getProductReviews(Long productId) {
        return productReviewMapper.selectByProductId(productId, null);
    }

    /**
     * 商家对买家进行评价
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitBuyerReview(Long merchantId, String orderNo, BuyerReviewDTO buyerReviewDTO) {
        log.info("商家[{}]评价订单[{}]买家", merchantId, orderNo);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> orderQuery = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getMerchantId, merchantId);
        Order order = orderMapper.selectOne(orderQuery);
        
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 2. 校验订单状态
        if (order.getStatus() != OrderStatusEnum.COMPLETED && order.getStatus() != OrderStatusEnum.RECEIVED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "只能评价已收货或已完成的订单，当前状态: " + order.getStatus().getDesc());
        }
        
        // 3. 检查是否已经评价
        LambdaQueryWrapper<BuyerReviewByMerchant> reviewQuery = Wrappers.<BuyerReviewByMerchant>lambdaQuery()
                .eq(BuyerReviewByMerchant::getOrderId, order.getId())
                .eq(BuyerReviewByMerchant::getMerchantId, merchantId);
        
        BuyerReviewByMerchant existingReview = buyerReviewByMerchantMapper.selectOne(reviewQuery);
        if (existingReview != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "已经评价过该买家");
        }
        
        // 4. 保存买家评价
        BuyerReviewByMerchant buyerReview = new BuyerReviewByMerchant();
        buyerReview.setOrderId(order.getId());
        buyerReview.setBuyerUserId(order.getUserId());
        buyerReview.setMerchantId(merchantId);
        buyerReview.setRatingScore(buyerReviewDTO.getRatingScore());
        buyerReview.setContent(buyerReviewDTO.getContent());
        buyerReview.setCreateTime(LocalDateTime.now());
        
        buyerReviewByMerchantMapper.insert(buyerReview);
        
        log.info("商家[{}]评价订单[{}]买家成功", merchantId, orderNo);
        return true;
    }

    /**
     * 检查用户是否已评价订单
     */
    @Override
    public boolean hasReviewedOrder(Long userId, String orderNo) {
        // 1. 查询订单
        LambdaQueryWrapper<Order> orderQuery = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId);
        Order order = orderMapper.selectOne(orderQuery);
        
        if (order == null) {
            return false;
        }
        
        // 2. 查询订单项是否都已评价
        LambdaQueryWrapper<OrderItem> itemQuery = Wrappers.<OrderItem>lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);
        
        if (orderItems == null || orderItems.isEmpty()) {
            return false;
        }
        
        // 所有订单项都已评价，才算订单已评价
        return orderItems.stream().allMatch(item -> item.getReviewStatus() == ReviewStatusEnum.REVIEWED);
    }

    /**
     * 获取未评价订单列表
     */
    @Override
    public List<String> getUnreviewedOrders(Long userId) {
        // 1. 查询用户的已收货未完成订单和已完成未评价订单
        LambdaQueryWrapper<Order> orderQuery = Wrappers.<Order>lambdaQuery()
                .eq(Order::getUserId, userId)
                .and(wrapper -> wrapper.eq(Order::getStatus, OrderStatusEnum.RECEIVED)
                     .or()
                     .eq(Order::getStatus, OrderStatusEnum.COMPLETED));
        List<Order> orders = orderMapper.selectList(orderQuery);
        
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 过滤出未评价的订单
        List<String> result = new ArrayList<>();
        for (Order order : orders) {
            LambdaQueryWrapper<OrderItem> itemQuery = Wrappers.<OrderItem>lambdaQuery()
                    .eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);
            
            // 检查是否有未评价的订单项
            boolean hasUnreviewedItem = orderItems.stream()
                    .anyMatch(item -> item.getReviewStatus() != ReviewStatusEnum.REVIEWED);
            
            if (hasUnreviewedItem) {
                result.add(order.getOrderNo());
            }
        }
        
        // 2. 返回订单号列表
        return result;
    }

    /**
     * 根据订单自动完成交易（未评价的情况）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoCompleteOrderByTimeout(String orderNo) {
        log.info("自动完成订单[{}]（未评价超时）", orderNo);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> orderQuery = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(orderQuery);
        
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 2. 校验订单状态
        if (order.getStatus() != OrderStatusEnum.RECEIVED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "只能自动完成已收货的订单，当前状态: " + order.getStatus().getDesc());
        }
        
        // 3. 更新订单状态为已完成
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.COMPLETED);
        updateOrder.setCompletionTime(LocalDateTime.now());
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(updateOrder);
        
        log.info("自动完成订单[{}]成功", orderNo);
        return true;
    }
    
    /**
     * 更新商品平均评分
     */
    private void updateProductAverageRating(Long productId) {
        // 1. 查询商品的所有评价
        Double avgRating = productReviewMapper.selectAvgRatingByProductId(productId);
        
        if (avgRating == null) {
            return;
        }
        
        // 2. 更新商品平均评分
        Product product = new Product();
        product.setId(productId);
        
        // 四舍五入保留一位小数
        BigDecimal rating = new BigDecimal(avgRating).setScale(1, RoundingMode.HALF_UP);
        product.setAverageRating(rating);
        
        productMapper.updateById(product);
    }
    
    /**
     * 更新商家好评率
     */
    private void updateMerchantPositiveRate(Long merchantId) {
        // 该方法需要根据实际业务逻辑实现
        // 这里提供一个简单的实现例子
        
        // 1. 查询商家的所有评价
        LambdaQueryWrapper<MerchantServiceReview> reviewQuery = Wrappers.<MerchantServiceReview>lambdaQuery()
                .eq(MerchantServiceReview::getMerchantId, merchantId);
        List<MerchantServiceReview> reviews = merchantServiceReviewMapper.selectList(reviewQuery);
        
        if (reviews == null || reviews.isEmpty()) {
            return;
        }
        
        // 2. 计算好评率（4分及以上为好评）
        long positiveCount = reviews.stream()
                .filter(review -> review.getServiceAttitudeRating() >= 4)
                .count();
        
        // 计算好评率并转换为BigDecimal格式
        BigDecimal positiveRate = new BigDecimal((double) positiveCount / reviews.size() * 100)
                .setScale(2, RoundingMode.HALF_UP);
        
        // 3. 更新商家信息
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStorePositiveRate(positiveRate);
        
        merchantMapper.updateById(merchant);
    }

    /**
     * 获取商家服务评分
     */
    @Override
    public Double getMerchantServiceRating(Long merchantId) {
        log.info("获取商家[{}]服务评分", merchantId);
        
        try {
            // 查询商家的所有服务评价
            LambdaQueryWrapper<MerchantServiceReview> reviewQuery = Wrappers.<MerchantServiceReview>lambdaQuery()
                    .eq(MerchantServiceReview::getMerchantId, merchantId);
            List<MerchantServiceReview> reviews = merchantServiceReviewMapper.selectList(reviewQuery);
            
            if (reviews == null || reviews.isEmpty()) {
                log.info("商家[{}]暂无服务评价，返回默认评分5.0", merchantId);
                return 5.0; // 默认5分
            }
            
            // 计算服务态度平均分
            double sum = reviews.stream()
                    .mapToDouble(MerchantServiceReview::getServiceAttitudeRating)
                    .sum();
            
            double averageRating = BigDecimal.valueOf(sum / reviews.size())
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
            
            log.info("商家[{}]服务评分: {}", merchantId, averageRating);
            return averageRating;
        } catch (Exception e) {
            log.error("获取商家服务评分出错: {}", e.getMessage(), e);
            return 5.0; // 出错时返回默认评分
        }
    }
    
    /**
     * 获取订单商品评价列表
     */
    @Override
    public List<ProductReview> getProductReviewsByOrderNo(String orderNo) {
        log.info("获取订单[{}]的商品评价列表", orderNo);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> orderQuery = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(orderQuery);
        
        if (order == null) {
            return new ArrayList<>();
        }
        
        // 2. 查询订单项
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
        if (orderItems == null || orderItems.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 3. 获取所有订单项ID
        List<Long> orderItemIds = orderItems.stream()
                .map(OrderItem::getId)
                .collect(Collectors.toList());
        
        // 4. 查询商品评价
        LambdaQueryWrapper<ProductReview> reviewQuery = Wrappers.<ProductReview>lambdaQuery()
                .in(ProductReview::getOrderItemId, orderItemIds);
        
        return productReviewMapper.selectList(reviewQuery);
    }
    
    /**
     * 获取订单商家服务评价
     */
    @Override
    public MerchantServiceReview getMerchantServiceReviewByOrderNo(String orderNo) {
        log.info("获取订单[{}]的商家服务评价", orderNo);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> orderQuery = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(orderQuery);
        
        if (order == null) {
            return null;
        }
        
        // 2. 查询商家服务评价
        LambdaQueryWrapper<MerchantServiceReview> reviewQuery = Wrappers.<MerchantServiceReview>lambdaQuery()
                .eq(MerchantServiceReview::getOrderId, order.getId());
        
        return merchantServiceReviewMapper.selectOne(reviewQuery);
    }
    
    /**
     * 获取订单买家评价（由商家评价）
     */
    @Override
    public BuyerReviewByMerchant getBuyerReviewByOrderNo(Long merchantId, String orderNo) {
        log.info("获取订单[{}]的买家评价，商家[{}]", orderNo, merchantId);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> orderQuery = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getMerchantId, merchantId);
        Order order = orderMapper.selectOne(orderQuery);
        
        if (order == null) {
            return null;
        }
        
        // 2. 查询买家评价
        LambdaQueryWrapper<BuyerReviewByMerchant> reviewQuery = Wrappers.<BuyerReviewByMerchant>lambdaQuery()
                .eq(BuyerReviewByMerchant::getOrderId, order.getId())
                .eq(BuyerReviewByMerchant::getMerchantId, merchantId);
        
        return buyerReviewByMerchantMapper.selectOne(reviewQuery);
    }
} 