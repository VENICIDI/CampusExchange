package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.controller.OrderController;
import org.campusmarket.exchange.dto.*;
import org.campusmarket.exchange.entity.*;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.enums.ReviewStatusEnum;
import org.campusmarket.exchange.enums.TradeTypeEnum;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.mapper.*;
import org.campusmarket.exchange.service.IOrderService;
import org.campusmarket.exchange.service.IProductService;
import org.campusmarket.exchange.service.ICartService;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private OrderItemMapper orderItemMapper;
    
    @Resource
    private ProductMapper productMapper;
    
    @Resource
    private MerchantMapper merchantMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private IProductService productService;
    
    @Resource
    private ICartService cartService;

    /**
     * 创建订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, OrderCreateDTO orderDTO) {
        log.info("用户[{}]创建订单", userId);
        
        // 1. 校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "用户不存在");
        }
        
        // 2. 校验商家是否存在
        Merchant merchant = merchantMapper.selectById(orderDTO.getMerchantId());
        if (merchant == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "商家不存在");
        }
        
        // 3. 生成订单号
        String orderNo = generateOrderNo();
        
        // 4. 计算订单金额
        BigDecimal totalProductAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        // 7. 计算平台佣金率 (假设为订单金额的5%)
        BigDecimal platformCommissionRate = new BigDecimal("0.05");
        
        for (OrderCreateDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            // 查询商品
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND.value(), "商品不存在");
            }
            
            // 校验商品是否属于该商家
            if (!product.getMerchantId().equals(orderDTO.getMerchantId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "商品不属于该商家");
            }
            
            // 校验商品库存
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                        "商品[" + product.getName() + "]库存不足，当前库存:" + product.getStock());
            }
            
            // 计算商品小计
            BigDecimal subtotal = product.getCurrentPrice().multiply(new BigDecimal(itemDTO.getQuantity()));
            totalProductAmount = totalProductAmount.add(subtotal);
            
            // 创建订单项（先不设置orderId，等订单创建后再设置）
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setMerchantId(product.getMerchantId());
            orderItem.setProductNameSnapshot(product.getName());
            orderItem.setProductImageSnapshot(null);
            orderItem.setPriceAtPurchase(product.getCurrentPrice());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setItemTotalAmount(subtotal);
            orderItem.setCommissionRateSnapshot(platformCommissionRate);
            orderItem.setCommissionAmountSnapshot(subtotal.multiply(platformCommissionRate));
            orderItem.setReviewStatus(ReviewStatusEnum.NOT_REVIEWED);
            orderItem.setCreateTime(LocalDateTime.now());
            
            orderItems.add(orderItem);
        }
        
        // 5. 计算积分抵扣金额 (按1积分=0.01元计算，上限为订单总金额的10%)
        BigDecimal pointsDeductionAmount = BigDecimal.ZERO;
        if (orderDTO.getPointsUsed() != null && orderDTO.getPointsUsed() > 0) {
            // 每1积分抵扣0.01元
            BigDecimal pointRate = new BigDecimal("0.01");
            pointsDeductionAmount = new BigDecimal(orderDTO.getPointsUsed()).multiply(pointRate);
            
            // 积分抵扣上限为订单金额的10%
            BigDecimal maxDeduction = totalProductAmount.multiply(new BigDecimal("0.1"));
            if (pointsDeductionAmount.compareTo(maxDeduction) > 0) {
                pointsDeductionAmount = maxDeduction;
            }
        }
        
        // 6. 计算实际支付金额
        BigDecimal actualPaymentAmount = totalProductAmount.subtract(pointsDeductionAmount);
        if (actualPaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            actualPaymentAmount = BigDecimal.ZERO;
        }
        
        BigDecimal platformCommissionAmount = totalProductAmount.multiply(platformCommissionRate);
        
        // 8. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(orderDTO.getMerchantId());
        order.setTotalProductAmount(totalProductAmount);
        order.setPointsUsed(orderDTO.getPointsUsed() != null ? orderDTO.getPointsUsed() : 0);
        order.setPointsDeductionAmount(pointsDeductionAmount);
        order.setActualPaymentAmount(actualPaymentAmount);
        order.setPlatformCommissionAmount(platformCommissionAmount);
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT);
        order.setTradeType(orderDTO.getTradeType());
        
        // 处理线下交易信息
        if (orderDTO.getTradeType() == TradeTypeEnum.OFFLINE) {
            order.setOfflineMeetingLocation(orderDTO.getOfflineMeetingLocation());
            if (orderDTO.getOfflineMeetingTime() != null) {
                order.setOfflineMeetingTime(LocalDateTime.parse(
                        orderDTO.getOfflineMeetingTime(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ));
            }
        } else {
            // 处理快递信息 - 注意：数据库中不存在收货人相关字段
            if (orderDTO.getAddress() != null) {
                // 使用用户提交的地址
                OrderCreateDTO.OrderAddressDTO addressDTO = orderDTO.getAddress();
                // 注释掉不存在的字段设置
                // order.setReceiverName(addressDTO.getReceiverName());
                // order.setReceiverPhone(addressDTO.getReceiverPhone());
                // order.setReceiverAddress(addressDTO.getFullAddress());
                
                // 如果设置为默认地址，则更新用户的默认地址
                if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
                    String defaultAddress = addressDTO.getReceiverName() + "," + 
                            addressDTO.getReceiverPhone() + "," + 
                            addressDTO.getFullAddress();
                    
                    User updateUser = new User();
                    updateUser.setId(userId);
                    updateUser.setDefaultAddress(defaultAddress);
                    userMapper.updateById(updateUser);
                }
            } else if (user.getDefaultAddress() != null && !user.getDefaultAddress().isEmpty()) {
                // 使用用户默认地址
                String[] addressParts = user.getDefaultAddress().split(",");
                // 注释掉不存在的字段设置
                // if (addressParts.length >= 3) {
                //     order.setReceiverName(addressParts[0]);
                //     order.setReceiverPhone(addressParts[1]);
                //     order.setReceiverAddress(addressParts[2]);
                // }
            } else {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "快递交易必须提供收货地址");
            }
        }
        
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        // 9. 保存订单
        orderMapper.insert(order);
        
        // 10. 保存订单项
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        
        // 11. 减少商品库存
        for (OrderCreateDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            productService.decreaseStock(itemDTO.getProductId(), itemDTO.getQuantity());
        }
        
        log.info("订单创建成功，订单号: {}", orderNo);
        return orderNo;
    }

    /**
     * 从购物车创建订单 (按商家拆分)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> createOrderFromCart(Long userId, Integer pointsUsed, TradeTypeEnum tradeType, 
                                           String offlineMeetingLocation, String offlineMeetingTime) {
        log.info("用户[{}]从购物车创建订单, 使用积分:{}, 交易方式:{}", userId, pointsUsed, tradeType);
        
        // 1. 校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "用户不存在");
        }
        
        // 2. 获取购物车中已选中的商品，按商家分组
        Map<Long, List<CartItem>> merchantItemsMap = cartService.getSelectedCartItemsByMerchant(userId);
        if (merchantItemsMap.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "购物车中没有选中的商品");
        }
        
        // 3. 校验线下交易信息
        if (tradeType == TradeTypeEnum.OFFLINE) {
            if (offlineMeetingLocation == null || offlineMeetingLocation.isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "线下交易地点不能为空");
            }
            if (offlineMeetingTime == null || offlineMeetingTime.isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "线下交易时间不能为空");
            }
        }
        
        // 4. 校验快递交易是否有默认地址
        if (tradeType == TradeTypeEnum.EXPRESS) {
            if (user.getDefaultAddress() == null || user.getDefaultAddress().isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "快递交易必须设置默认收货地址");
            }
        }

        return createOrderFromCart(userId, pointsUsed, tradeType, offlineMeetingLocation, offlineMeetingTime, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> createOrderFromCart(Long userId, Integer pointsUsed, TradeTypeEnum tradeType, 
                                           String offlineMeetingLocation, String offlineMeetingTime,
                                           Object addressObj) {
        log.info("用户[{}]从购物车创建订单, 使用积分:{}, 交易方式:{}", userId, pointsUsed, tradeType);
        
        // 1. 校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "用户不存在");
        }
        
        // 2. 获取购物车中已选中的商品，按商家分组
        Map<Long, List<CartItem>> merchantItemsMap = cartService.getSelectedCartItemsByMerchant(userId);
        if (merchantItemsMap.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "购物车中没有选中的商品");
        }
        
        // 3. 校验线下交易信息
        if (tradeType == TradeTypeEnum.OFFLINE) {
            if (offlineMeetingLocation == null || offlineMeetingLocation.isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "线下交易地点不能为空");
            }
            if (offlineMeetingTime == null || offlineMeetingTime.isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "线下交易时间不能为空");
            }
        }
        
        // 4. 校验快递交易是否有地址信息
        if (tradeType == TradeTypeEnum.EXPRESS && addressObj == null) {
            if (user.getDefaultAddress() == null || user.getDefaultAddress().isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "快递交易必须设置收货地址");
            }
        }
        
        // 5. 计算总金额，检查积分是否足够
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (List<CartItem> items : merchantItemsMap.values()) {
            for (CartItem item : items) {
                Product product = item.getProduct();
                if (product == null) {
                    continue;
                }
                
                totalAmount = totalAmount.add(product.getCurrentPrice().multiply(new BigDecimal(item.getQuantity())));
            }
        }
        
        // 6. 计算积分抵扣金额 (按100积分=1元计算，无上限)
        BigDecimal pointsDeductionAmount = BigDecimal.ZERO;
        if (pointsUsed != null && pointsUsed > 0) {
            // 每100积分抵扣1元
            BigDecimal pointRate = new BigDecimal("0.01");
            pointsDeductionAmount = new BigDecimal(pointsUsed).multiply(pointRate);
            
            // 积分抵扣不能超过订单总金额
            if (pointsDeductionAmount.compareTo(totalAmount) > 0) {
                pointsDeductionAmount = totalAmount;
            }
        }
        
        // 7. 为每个商家创建订单
        List<String> orderNos = new ArrayList<>();
        
        // 分配积分抵扣金额到各个订单
        // 按每个订单金额占比分配积分抵扣
        Map<Long, BigDecimal> merchantOrderAmounts = new HashMap<>();
        for (Long merchantId : merchantItemsMap.keySet()) {
            BigDecimal merchantTotal = BigDecimal.ZERO;
            List<CartItem> items = merchantItemsMap.get(merchantId);
            for (CartItem item : items) {
                Product product = item.getProduct();
                if (product == null) {
                    continue;
                }
                
                merchantTotal = merchantTotal.add(product.getCurrentPrice().multiply(new BigDecimal(item.getQuantity())));
            }
            merchantOrderAmounts.put(merchantId, merchantTotal);
        }
        
        // 分配积分抵扣
        Map<Long, BigDecimal> merchantPointsDeductions = new HashMap<>();
        for (Long merchantId : merchantOrderAmounts.keySet()) {
            BigDecimal orderAmount = merchantOrderAmounts.get(merchantId);
            BigDecimal ratio = orderAmount.divide(totalAmount, 10, BigDecimal.ROUND_HALF_UP);
            BigDecimal deduction = pointsDeductionAmount.multiply(ratio).setScale(2, BigDecimal.ROUND_HALF_UP);
            merchantPointsDeductions.put(merchantId, deduction);
        }
        
        // 处理可能的舍入误差，确保总抵扣金额等于计算的积分抵扣金额
        BigDecimal totalDeductions = merchantPointsDeductions.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalDeductions.compareTo(pointsDeductionAmount) != 0) {
            BigDecimal diff = pointsDeductionAmount.subtract(totalDeductions);
            // 将差额加到第一个订单
            if (!merchantPointsDeductions.isEmpty()) {
                Long firstMerchantId = merchantPointsDeductions.keySet().iterator().next();
                BigDecimal currentDeduction = merchantPointsDeductions.get(firstMerchantId);
                merchantPointsDeductions.put(firstMerchantId, currentDeduction.add(diff));
            }
        }
        
        // 计算积分使用数量到各个订单的分配
        // 每100积分抵扣1元，按抵扣金额反推
        Map<Long, Integer> merchantPointsUsed = new HashMap<>();
        for (Long merchantId : merchantPointsDeductions.keySet()) {
            BigDecimal deduction = merchantPointsDeductions.get(merchantId);
            // 每0.01元等于1积分
            Integer points = deduction.multiply(new BigDecimal(100)).intValue();
            merchantPointsUsed.put(merchantId, points);
        }
        
        // 提取地址信息
        String receiverName = null;
        String receiverPhone = null;
        String receiverAddress = null;
        
        // 解析自定义地址信息
        if (addressObj != null && addressObj instanceof OrderController.CartOrderDTO.OrderAddressDTO) {
            OrderController.CartOrderDTO.OrderAddressDTO address = (OrderController.CartOrderDTO.OrderAddressDTO) addressObj;
            receiverName = address.getReceiverName();
            receiverPhone = address.getReceiverPhone();
            receiverAddress = address.getFullAddress();
            
            // 如果设置为默认地址，更新用户默认地址
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                String defaultAddress = address.getReceiverName() + "," + 
                        address.getReceiverPhone() + "," + 
                        address.getFullAddress();
                
                User updateUser = new User();
                updateUser.setId(userId);
                updateUser.setDefaultAddress(defaultAddress);
                userMapper.updateById(updateUser);
            }
        } else if (user.getDefaultAddress() != null && !user.getDefaultAddress().isEmpty()) {
            // 使用用户默认地址
            String[] addressParts = user.getDefaultAddress().split(",");
            if (addressParts.length >= 3) {
                receiverName = addressParts[0];
                receiverPhone = addressParts[1];
                receiverAddress = addressParts[2];
            }
        }
        
        // 为每个商家创建订单
        for (Long merchantId : merchantItemsMap.keySet()) {
            List<CartItem> items = merchantItemsMap.get(merchantId);
            
            // 生成订单号
            String orderNo = generateOrderNo();
            orderNos.add(orderNo);
            
            // 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setMerchantId(merchantId);
            
            // 计算订单商品总金额
            BigDecimal totalProductAmount = merchantOrderAmounts.get(merchantId);
            order.setTotalProductAmount(totalProductAmount);
            
            // 设置积分抵扣信息
            Integer orderPointsUsed = merchantPointsUsed.getOrDefault(merchantId, 0);
            BigDecimal orderPointsDeduction = merchantPointsDeductions.getOrDefault(merchantId, BigDecimal.ZERO);
            order.setPointsUsed(orderPointsUsed);
            order.setPointsDeductionAmount(orderPointsDeduction);
            
            // 计算实际支付金额
            BigDecimal actualPaymentAmount = totalProductAmount.subtract(orderPointsDeduction);
            if (actualPaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
                actualPaymentAmount = BigDecimal.ZERO;
            }
            order.setActualPaymentAmount(actualPaymentAmount);
            
            // 设置平台手续费 (假设为商品总金额的5%)
            BigDecimal platformCommissionRate = new BigDecimal("0.05");
            BigDecimal platformCommissionAmount = totalProductAmount.multiply(platformCommissionRate);
            order.setPlatformCommissionAmount(platformCommissionAmount);
            
            // 设置订单状态
            order.setStatus(OrderStatusEnum.PENDING_PAYMENT);
            order.setTradeType(tradeType);
            
            // 处理线下交易信息或收货信息
            if (tradeType == TradeTypeEnum.OFFLINE) {
                order.setOfflineMeetingLocation(offlineMeetingLocation);
                order.setOfflineMeetingTime(LocalDateTime.parse(
                        offlineMeetingTime,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ));
            } else {
                // 快递交易，设置收货信息 - 注意：数据库中不存在收货人相关字段
                // if (receiverName != null && receiverPhone != null && receiverAddress != null) {
                //     order.setReceiverName(receiverName);
                //     order.setReceiverPhone(receiverPhone);
                //     order.setReceiverAddress(receiverAddress);
                // }
            }
            
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            
            // 保存订单
            orderMapper.insert(order);
            
            // 创建订单项
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : items) {
                Product product = cartItem.getProduct();
                if (product == null) {
                    continue;
                }
                
                // 检查库存
                if (product.getStock() < cartItem.getQuantity()) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                            "商品[" + product.getName() + "]库存不足，当前库存:" + product.getStock());
                }
                
                // 创建订单项
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setProductId(product.getId());
                orderItem.setMerchantId(product.getMerchantId());
                orderItem.setProductNameSnapshot(product.getName());
                // 获取商品主图 - 此处简化处理
                orderItem.setProductImageSnapshot(null);
                orderItem.setPriceAtPurchase(product.getCurrentPrice());
                orderItem.setQuantity(cartItem.getQuantity());
                
                // 计算商品小计
                BigDecimal subtotal = product.getCurrentPrice().multiply(new BigDecimal(cartItem.getQuantity()));
                orderItem.setItemTotalAmount(subtotal);
                
                // 设置手续费
                orderItem.setCommissionRateSnapshot(platformCommissionRate);
                orderItem.setCommissionAmountSnapshot(subtotal.multiply(platformCommissionRate));
                
                orderItem.setReviewStatus(ReviewStatusEnum.NOT_REVIEWED);
                orderItem.setCreateTime(LocalDateTime.now());
                
                orderItems.add(orderItem);
                orderItemMapper.insert(orderItem);
                
                // 减少商品库存
                productService.decreaseStock(product.getId(), cartItem.getQuantity());
            }
        }
        
        // 8. 清空购物车中已选中的商品
        cartService.clearSelectedCartItems(userId);
        
        log.info("用户[{}]从购物车创建订单成功，订单号: {}", userId, orderNos);
        return orderNos;
    }

    /**
     * 获取订单详情
     */
    @Override
    public OrderDetailVO getOrderDetail(String orderNo) {
        log.info("获取订单详情: {}", orderNo);
        
        // 1. 查询订单
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 2. 查询订单项
        List<OrderItem> orderItems = orderItemMapper.selectByOrderNo(orderNo);
        
        // 3. 获取买家信息
        User user = userMapper.selectById(order.getUserId());
        
        // 4. 获取商家信息
        Merchant merchant = merchantMapper.selectById(order.getMerchantId());
        
        // 5. 构建订单详情VO
        OrderDetailVO detailVO = new OrderDetailVO();
        BeanUtils.copyProperties(order, detailVO);
        
        detailVO.setUserName(user != null ? user.getUsername() : "未知用户");
        detailVO.setMerchantName(merchant != null ? merchant.getStoreName() : "未知商家");
        
        // 设置订单项
        detailVO.setOrderItems(orderItems);
        
        return detailVO;
    }

    /**
     * 获取用户订单列表
     */
    @Override
    public Page<OrderVO> getUserOrders(Long userId, OrderStatusEnum status, Integer pageNum, Integer pageSize) {
        log.info("获取用户[{}]的订单列表, 状态: {}, 页码: {}, 每页数量: {}", userId, status, pageNum, pageSize);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);
        
        // 2. 执行分页查询
        Page<Order> page = new Page<>(pageNum, pageSize);
        Page<Order> orderPage = orderMapper.selectPage(page, queryWrapper);
        
        // 3. 转换为VO
        Page<OrderVO> resultPage = new Page<>();
        BeanUtils.copyProperties(orderPage, resultPage, "records");
        
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            OrderVO vo = convertToOrderVO(order);
            orderVOList.add(vo);
        }
        
        resultPage.setRecords(orderVOList);
        return resultPage;
    }

    /**
     * 获取商家订单列表
     */
    @Override
    public Page<OrderVO> getMerchantOrders(Long merchantId, OrderStatusEnum status, Integer pageNum, Integer pageSize) {
        log.info("获取商家[{}]的订单列表, 状态: {}, 页码: {}, 每页数量: {}", merchantId, status, pageNum, pageSize);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getMerchantId, merchantId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);
        
        // 2. 执行分页查询
        Page<Order> page = new Page<>(pageNum, pageSize);
        Page<Order> orderPage = orderMapper.selectPage(page, queryWrapper);
        
        // 3. 转换为VO
        Page<OrderVO> resultPage = new Page<>();
        BeanUtils.copyProperties(orderPage, resultPage, "records");
        
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            OrderVO vo = convertToOrderVO(order);
            orderVOList.add(vo);
        }
        
        resultPage.setRecords(orderVOList);
        return resultPage;
    }

    /**
     * 取消订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long userId, String orderNo) {
        log.info("用户[{}]取消订单: {}", userId, orderNo);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId);
        Order order = orderMapper.selectOne(queryWrapper);
        
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 2. 校验订单状态
        if (order.getStatus() != OrderStatusEnum.PENDING_PAYMENT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "只能取消待付款订单，当前状态: " + order.getStatus().getDesc());
        }
        
        // 3. 更新订单状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.CANCELLED);
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(updateOrder);
        
        log.info("订单取消成功");
        return true;
    }

    /**
     * 支付订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long userId, String orderNo) {
        log.info("用户[{}]支付订单: {}", userId, orderNo);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId);
        Order order = orderMapper.selectOne(queryWrapper);
        
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 2. 校验订单状态
        if (order.getStatus() != OrderStatusEnum.PENDING_PAYMENT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "只有待付款订单才能支付，当前状态: " + order.getStatus().getDesc());
        }
        
        // 3. 更新订单状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.PENDING_SHIPMENT);
        updateOrder.setPaymentTime(LocalDateTime.now());
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(updateOrder);
        
        log.info("订单支付成功");
        return true;
    }

    /**
     * 确认收货
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceipt(Long userId, String orderNo) {
        log.info("用户[{}]确认收货: {}", userId, orderNo);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId);
        Order order = orderMapper.selectOne(queryWrapper);
        
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 2. 校验订单状态
        if (order.getStatus() != OrderStatusEnum.SHIPPED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "只能确认已发货的订单，当前状态: " + order.getStatus().getDesc());
        }
        
        // 3. 更新订单状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.COMPLETED);
        updateOrder.setReceiptConfirmationTime(LocalDateTime.now());
        updateOrder.setCompletionTime(LocalDateTime.now());
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(updateOrder);
        
        log.info("确认收货成功");
        return true;
    }

    /**
     * 商家发货
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(Long merchantId, String orderNo, String trackingNo, String expressCompany) {
        log.info("商家[{}]发货: {}", merchantId, orderNo);
        // 不再记录不存在的字段
        // log.info("商家[{}]发货: {}, 快递单号: {}, 快递公司: {}", merchantId, orderNo, trackingNo, expressCompany);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getMerchantId, merchantId);
        Order order = orderMapper.selectOne(queryWrapper);
        
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 2. 校验订单状态
        if (order.getStatus() != OrderStatusEnum.PENDING_SHIPMENT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "只有待发货订单才能发货，当前状态: " + order.getStatus().getDesc());
        }
        
        // 3. 更新订单状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.SHIPPED);
        updateOrder.setShippingTime(LocalDateTime.now());
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(updateOrder);
        
        log.info("订单发货成功");
        return true;
    }

    /**
     * 申请退款/退货
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean requestReturn(Long userId, String orderNo, String reason) {
        log.info("用户[{}]申请退款/退货: {}, 原因: {}", userId, orderNo, reason);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId);
        Order order = orderMapper.selectOne(queryWrapper);
        
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 2. 校验订单状态
        if (order.getStatus() != OrderStatusEnum.PENDING_SHIPMENT && 
            order.getStatus() != OrderStatusEnum.SHIPPED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "只能申请退款/退货已付款但未完成的订单，当前状态: " + order.getStatus().getDesc());
        }
        
        // 3. 更新订单状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.RETURN_REQUESTED);
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(updateOrder);
        
        log.info("申请退款/退货成功");
        return true;
    }

    /**
     * 商家处理退款申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processReturnRequest(Long merchantId, String orderNo, boolean approve, String remark) {
        log.info("商家[{}]处理退款申请: {}, 是否同意: {}, 备注: {}", merchantId, orderNo, approve, remark);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getMerchantId, merchantId);
        Order order = orderMapper.selectOne(queryWrapper);
        
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 2. 校验订单状态
        if (order.getStatus() != OrderStatusEnum.RETURN_REQUESTED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "只能处理退款/退货申请状态的订单，当前状态: " + order.getStatus().getDesc());
        }
        
        // 3. 更新订单状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        
        OrderStatusEnum newStatus;
        
        if (approve) {
            // 同意退款
            if (TradeTypeEnum.EXPRESS.getCode().equals(order.getTradeType()) && 
                order.getStatus() == OrderStatusEnum.SHIPPED) {
                // 如果是快递交易且已发货，则需要退货
                newStatus = OrderStatusEnum.RETURN_APPROVED;
            } else {
                // 否则直接退款
                newStatus = OrderStatusEnum.RETURNED;
            }
        } else {
            // 拒绝退款
            newStatus = OrderStatusEnum.RETURN_REJECTED;
        }
        
        updateOrder.setStatus(newStatus);
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(updateOrder);
        
        log.info("处理退款/退货申请成功");
        return true;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        // 生成订单号规则：年月日时分秒 + 3位随机数
        LocalDateTime now = LocalDateTime.now();
        String dateTime = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        // 3位随机数
        Random random = new Random();
        int randomNum = random.nextInt(1000);
        
        return dateTime + String.format("%03d", randomNum);
    }

    /**
     * 将Order实体转换为OrderVO
     */
    private OrderVO convertToOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        
        // 获取买家信息
        if (order.getUserId() != null) {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                vo.setUserName(user.getUsername());
            }
        }
        
        // 获取商家信息
        if (order.getMerchantId() != null) {
            Merchant merchant = merchantMapper.selectById(order.getMerchantId());
            if (merchant != null) {
                vo.setMerchantName(merchant.getStoreName());
            }
        }
        
        // 获取订单项
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        List<OrderVO.SimpleOrderItem> simpleItems = items.stream()
                .map(OrderVO.SimpleOrderItem::fromOrderItem)
                .collect(Collectors.toList());
        vo.setOrderItems(simpleItems);
        
        return vo;
    }
} 