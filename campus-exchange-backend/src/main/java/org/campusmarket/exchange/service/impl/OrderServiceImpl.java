package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.dto.*;
import org.campusmarket.exchange.entity.*;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.enums.TradeTypeEnum;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.mapper.*;
import org.campusmarket.exchange.service.IOrderService;
import org.campusmarket.exchange.service.IProductService;
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
    private OrderAddressMapper orderAddressMapper;
    
    @Resource
    private OrderLogMapper orderLogMapper;
    
    @Resource
    private ProductMapper productMapper;
    
    @Resource
    private MerchantMapper merchantMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private IProductService productService;

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
            
            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(null);
            orderItem.setPrice(product.getCurrentPrice());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setSubtotal(subtotal);
            orderItem.setSpecifications("尺寸:" + product.getSize() + "; 新旧程度:" + (product.getProductCondition() != null ? product.getProductCondition().name() : "未知"));
            orderItem.setCreateTime(LocalDateTime.now());
            orderItem.setUpdateTime(LocalDateTime.now());
            
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
        
        // 7. 计算平台佣金 (假设为订单金额的5%)
        BigDecimal platformCommissionRate = new BigDecimal("0.05");
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
        order.setTradeType(orderDTO.getTradeType().getCode());
        
        // 处理线下交易信息
        if (orderDTO.getTradeType() == TradeTypeEnum.OFFLINE) {
            order.setOfflineMeetingLocation(orderDTO.getOfflineMeetingLocation());
            if (orderDTO.getOfflineMeetingTime() != null) {
                order.setOfflineMeetingTime(LocalDateTime.parse(
                        orderDTO.getOfflineMeetingTime(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ));
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
        
        // 11. 保存收货地址 (仅快递交易需要)
        if (orderDTO.getTradeType() == TradeTypeEnum.EXPRESS) {
            OrderAddress orderAddress = new OrderAddress();
            orderAddress.setOrderId(order.getId());
            orderAddress.setOrderNo(orderNo);
            orderAddress.setUserId(userId);
            
            // 根据地址ID获取收货地址信息
            if (orderDTO.getAddressId() != null) {
                // 此处需要从用户地址表读取地址信息
                // 由于没有实现用户地址管理功能，暂时使用直接提交的地址信息
                if (orderDTO.getAddress() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "收货地址不能为空");
                }
                
                OrderCreateDTO.OrderAddressDTO addrDTO = orderDTO.getAddress();
                orderAddress.setReceiverName(addrDTO.getReceiverName());
                orderAddress.setReceiverPhone(addrDTO.getReceiverPhone());
                orderAddress.setProvince(addrDTO.getProvince());
                orderAddress.setCity(addrDTO.getCity());
                orderAddress.setDistrict(addrDTO.getDistrict());
                orderAddress.setDetailAddress(addrDTO.getDetailAddress());
                orderAddress.setPostalCode(addrDTO.getPostalCode());
                orderAddress.setIsDefault(addrDTO.getIsDefault());
            } else {
                if (orderDTO.getAddress() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "收货地址不能为空");
                }
                
                OrderCreateDTO.OrderAddressDTO addrDTO = orderDTO.getAddress();
                orderAddress.setReceiverName(addrDTO.getReceiverName());
                orderAddress.setReceiverPhone(addrDTO.getReceiverPhone());
                orderAddress.setProvince(addrDTO.getProvince());
                orderAddress.setCity(addrDTO.getCity());
                orderAddress.setDistrict(addrDTO.getDistrict());
                orderAddress.setDetailAddress(addrDTO.getDetailAddress());
                orderAddress.setPostalCode(addrDTO.getPostalCode());
                orderAddress.setIsDefault(addrDTO.getIsDefault());
            }
            
            orderAddress.setCreateTime(LocalDateTime.now());
            orderAddress.setUpdateTime(LocalDateTime.now());
            
            orderAddressMapper.insert(orderAddress);
        }
        
        // 12. 创建订单日志
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(order.getId());
        orderLog.setOrderNo(orderNo);
        orderLog.setCurrentStatus(OrderStatusEnum.PENDING_PAYMENT);
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType("USER");
        orderLog.setOperatorName(user.getUsername());
        orderLog.setOperationNote("创建订单");
        orderLog.setCreateTime(LocalDateTime.now());
        
        orderLogMapper.insert(orderLog);
        
        log.info("订单创建成功，订单号: {}", orderNo);
        return orderNo;
    }

    /**
     * 获取订单详情
     */
    @Override
    public OrderDetailVO getOrderDetail(String orderNo) {
        log.info("获取订单详情: {}", orderNo);
        
        // 1. 查询订单
        LambdaQueryWrapper<Order> orderWrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(orderWrapper);
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 2. 查询订单项
        List<OrderItem> orderItems = orderItemMapper.selectByOrderNo(orderNo);
        
        // 3. 查询收货地址
        OrderAddress orderAddress = orderAddressMapper.selectByOrderNo(orderNo);
        
        // 4. 查询订单日志
        List<OrderLog> orderLogs = orderLogMapper.selectByOrderNo(orderNo);
        
        // 5. 获取买家信息
        User user = userMapper.selectById(order.getUserId());
        
        // 6. 获取商家信息
        Merchant merchant = merchantMapper.selectById(order.getMerchantId());
        
        // 7. 构建订单详情VO
        OrderDetailVO detailVO = new OrderDetailVO();
        BeanUtils.copyProperties(order, detailVO);
        
        if (user != null) {
            detailVO.setUsername(user.getUsername());
        }
        
        if (merchant != null) {
            detailVO.setStoreName(merchant.getStoreName());
        }
        
        detailVO.setOrderItems(orderItems);
        detailVO.setOrderAddress(orderAddress);
        detailVO.setOrderLogs(orderLogs);
        
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
        
        // 4. 记录订单日志
        User user = userMapper.selectById(userId);
        
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(order.getId());
        orderLog.setOrderNo(orderNo);
        orderLog.setPreviousStatus(order.getStatus());
        orderLog.setCurrentStatus(OrderStatusEnum.CANCELLED);
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType("USER");
        orderLog.setOperatorName(user != null ? user.getUsername() : "");
        orderLog.setOperationNote("用户取消订单");
        orderLog.setCreateTime(LocalDateTime.now());
        
        orderLogMapper.insert(orderLog);
        
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
                    "订单状态不正确，当前状态: " + order.getStatus().getDesc());
        }
        
        // 3. 更新订单状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.PENDING_SHIPMENT);
        updateOrder.setPaymentTime(LocalDateTime.now());
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(updateOrder);
        
        // 4. 记录订单日志
        User user = userMapper.selectById(userId);
        
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(order.getId());
        orderLog.setOrderNo(orderNo);
        orderLog.setPreviousStatus(order.getStatus());
        orderLog.setCurrentStatus(OrderStatusEnum.PENDING_SHIPMENT);
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType("USER");
        orderLog.setOperatorName(user != null ? user.getUsername() : "");
        orderLog.setOperationNote("用户支付订单");
        orderLog.setCreateTime(LocalDateTime.now());
        
        orderLogMapper.insert(orderLog);
        
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
        
        // 4. 记录订单日志
        User user = userMapper.selectById(userId);
        
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(order.getId());
        orderLog.setOrderNo(orderNo);
        orderLog.setPreviousStatus(order.getStatus());
        orderLog.setCurrentStatus(OrderStatusEnum.COMPLETED);
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType("USER");
        orderLog.setOperatorName(user != null ? user.getUsername() : "");
        orderLog.setOperationNote("用户确认收货");
        orderLog.setCreateTime(LocalDateTime.now());
        
        orderLogMapper.insert(orderLog);
        
        log.info("确认收货成功");
        return true;
    }

    /**
     * 商家发货
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(Long merchantId, String orderNo, String trackingNo, String expressCompany) {
        log.info("商家[{}]发货: {}, 快递单号: {}, 快递公司: {}", merchantId, orderNo, trackingNo, expressCompany);
        
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
                    "只能发货待发货状态的订单，当前状态: " + order.getStatus().getDesc());
        }
        
        // 3. 校验交易方式
        if (!TradeTypeEnum.EXPRESS.getCode().equals(order.getTradeType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "只有快递交易方式才能发货");
        }
        
        // 4. 更新订单状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.SHIPPED);
        updateOrder.setShippingTime(LocalDateTime.now());
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        // 在这里可以添加快递单号和快递公司字段，但当前Order实体类中没有这些字段
        // 如果需要，可以为Order类添加这两个字段
        
        orderMapper.updateById(updateOrder);
        
        // 5. 记录订单日志
        Merchant merchant = merchantMapper.selectById(merchantId);
        
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(order.getId());
        orderLog.setOrderNo(orderNo);
        orderLog.setPreviousStatus(order.getStatus());
        orderLog.setCurrentStatus(OrderStatusEnum.SHIPPED);
        orderLog.setOperatorId(merchantId);
        orderLog.setOperatorType("MERCHANT");
        orderLog.setOperatorName(merchant != null ? merchant.getStoreName() : "");
        orderLog.setOperationNote("商家发货，快递单号: " + trackingNo + ", 快递公司: " + expressCompany);
        orderLog.setCreateTime(LocalDateTime.now());
        
        orderLogMapper.insert(orderLog);
        
        log.info("发货成功");
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
        
        // 4. 记录订单日志
        User user = userMapper.selectById(userId);
        
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(order.getId());
        orderLog.setOrderNo(orderNo);
        orderLog.setPreviousStatus(order.getStatus());
        orderLog.setCurrentStatus(OrderStatusEnum.RETURN_REQUESTED);
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType("USER");
        orderLog.setOperatorName(user != null ? user.getUsername() : "");
        orderLog.setOperationNote("用户申请退款/退货，原因: " + reason);
        orderLog.setCreateTime(LocalDateTime.now());
        
        orderLogMapper.insert(orderLog);
        
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
        String operationNote;
        
        if (approve) {
            // 同意退款
            if (TradeTypeEnum.EXPRESS.getCode().equals(order.getTradeType()) && 
                order.getStatus() == OrderStatusEnum.SHIPPED) {
                // 如果是快递交易且已发货，则需要退货
                newStatus = OrderStatusEnum.RETURN_APPROVED;
                operationNote = "商家同意退款/退货申请，等待买家退货";
            } else {
                // 否则直接退款
                newStatus = OrderStatusEnum.RETURNED;
                operationNote = "商家同意退款申请，已退款";
            }
        } else {
            // 拒绝退款
            newStatus = OrderStatusEnum.RETURN_REJECTED;
            operationNote = "商家拒绝退款/退货申请，原因: " + remark;
        }
        
        updateOrder.setStatus(newStatus);
        updateOrder.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(updateOrder);
        
        // 4. 记录订单日志
        Merchant merchant = merchantMapper.selectById(merchantId);
        
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(order.getId());
        orderLog.setOrderNo(orderNo);
        orderLog.setPreviousStatus(order.getStatus());
        orderLog.setCurrentStatus(newStatus);
        orderLog.setOperatorId(merchantId);
        orderLog.setOperatorType("MERCHANT");
        orderLog.setOperatorName(merchant != null ? merchant.getStoreName() : "");
        orderLog.setOperationNote(operationNote);
        orderLog.setCreateTime(LocalDateTime.now());
        
        orderLogMapper.insert(orderLog);
        
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
                vo.setUsername(user.getUsername());
            }
        }
        
        // 获取商家信息
        if (order.getMerchantId() != null) {
            Merchant merchant = merchantMapper.selectById(order.getMerchantId());
            if (merchant != null) {
                vo.setStoreName(merchant.getStoreName());
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