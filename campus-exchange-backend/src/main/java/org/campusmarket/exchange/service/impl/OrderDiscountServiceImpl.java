package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.campusmarket.exchange.entity.Order;
import org.campusmarket.exchange.entity.OrderDiscount;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.mapper.OrderDiscountMapper;
import org.campusmarket.exchange.mapper.OrderMapper;
import org.campusmarket.exchange.service.IOrderDiscountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单折扣服务实现类
 */
@Service
@RequiredArgsConstructor
public class OrderDiscountServiceImpl extends ServiceImpl<OrderDiscountMapper, OrderDiscount> implements IOrderDiscountService {
    
    private final OrderMapper orderMapper;

    /**
     * 为订单创建折扣
     *
     * @param orderId 订单ID
     * @param discountAmount 折扣金额
     * @param adminId 管理员ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDiscount(Long orderId, BigDecimal discountAmount, Long adminId) {
        // 检查订单是否存在且为待支付状态
        Order order = orderMapper.selectById(orderId);
        if (order == null || !OrderStatusEnum.PENDING_PAYMENT.equals(order.getStatus())) {
            return false;
        }
        
        // 检查折扣金额是否有效（大于0且小于等于订单总金额）
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0 || 
                discountAmount.compareTo(order.getTotalProductAmount()) > 0) {
            return false;
        }
        
        // 检查是否已存在折扣
        OrderDiscount existingDiscount = getByOrderId(orderId);
        if (existingDiscount != null) {
            // 更新现有折扣
            existingDiscount.setDiscountAmount(discountAmount);
            baseMapper.updateById(existingDiscount);
        } else {
            // 创建新折扣
            OrderDiscount discount = new OrderDiscount();
            discount.setOrderId(orderId);
            discount.setDiscountAmount(discountAmount);
            discount.setAdminId(adminId);
            discount.setCreateTime(LocalDateTime.now());
            baseMapper.insert(discount);
        }
        
        // 重新计算实付金额 = 商品总金额 - 积分抵扣 - 折扣金额
        BigDecimal actualAmount = order.getTotalProductAmount()
                .subtract(order.getPointsDeductionAmount() != null ? order.getPointsDeductionAmount() : BigDecimal.ZERO)
                .subtract(discountAmount);
        
        // 设置订单折扣金额
        order.setDiscountAmount(discountAmount);
        
        // 更新订单实际支付金额
        order.setActualPaymentAmount(actualAmount);
        
        // 更新订单
        orderMapper.updateById(order);
        
        return true;
    }
    
    /**
     * 根据订单ID获取折扣
     *
     * @param orderId 订单ID
     * @return 订单折扣
     */
    @Override
    public OrderDiscount getByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderDiscount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDiscount::getOrderId, orderId);
        return baseMapper.selectOne(wrapper);
    }
} 