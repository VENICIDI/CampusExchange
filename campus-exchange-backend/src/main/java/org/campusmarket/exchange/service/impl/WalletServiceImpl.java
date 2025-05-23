package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.campusmarket.exchange.entity.Order;
import org.campusmarket.exchange.entity.PointsAccount;
import org.campusmarket.exchange.entity.Wallet;
import org.campusmarket.exchange.entity.WalletTransaction;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.enums.PointsTransactionTypeEnum;
import org.campusmarket.exchange.enums.WalletTransactionTypeEnum;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.mapper.OrderMapper;
import org.campusmarket.exchange.mapper.WalletMapper;
import org.campusmarket.exchange.mapper.WalletTransactionMapper;
import org.campusmarket.exchange.service.IPointsService;
import org.campusmarket.exchange.service.IWalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 钱包服务实现类
 */
@Service
public class WalletServiceImpl implements IWalletService {
    
    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);
    
    @Resource
    private WalletMapper walletMapper;
    
    @Resource
    private WalletTransactionMapper walletTransactionMapper;
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private IPointsService pointsService;
    
    @Override
    public Wallet getWalletByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        LambdaQueryWrapper<Wallet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Wallet::getUserId, userId);
        
        Wallet wallet = walletMapper.selectOne(queryWrapper);
        
        // 如果用户钱包不存在，则创建一个新的钱包
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setFrozenAmount(BigDecimal.ZERO);
            wallet.setCreateTime(LocalDateTime.now());
            wallet.setUpdateTime(LocalDateTime.now());
            
            walletMapper.insert(wallet);
        }
        
        return wallet;
    }
    
    @Override
    @Transactional
    public Wallet recharge(Long userId, BigDecimal amount) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "充值金额必须大于0");
        }
        
        // 获取用户钱包
        Wallet wallet = getWalletByUserId(userId);
        
        // 更新钱包余额
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        wallet.setUpdateTime(LocalDateTime.now());
        
        walletMapper.updateById(wallet);
        
        // 记录交易
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(amount);
        transaction.setType(WalletTransactionTypeEnum.RECHARGE.getCode());
        transaction.setDescription("钱包充值");
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setCreateTime(LocalDateTime.now());
        
        walletTransactionMapper.insert(transaction);
        
        return wallet;
    }
    
    @Override
    @Transactional
    public Wallet withdraw(Long userId, BigDecimal amount) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "提现金额必须大于0");
        }
        
        // 获取用户钱包
        Wallet wallet = getWalletByUserId(userId);
        
        // 检查余额是否足够
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "钱包余额不足");
        }
        
        // 更新钱包余额
        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        wallet.setUpdateTime(LocalDateTime.now());
        
        walletMapper.updateById(wallet);
        
        // 记录交易
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(amount.negate()); // 提现是负数
        transaction.setType(WalletTransactionTypeEnum.CONSUMPTION.getCode());
        transaction.setDescription("钱包提现");
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setCreateTime(LocalDateTime.now());
        
        walletTransactionMapper.insert(transaction);
        
        return wallet;
    }
    
    @Override
    public List<WalletTransaction> getTransactionsByUserId(Long userId, Integer page, Integer size) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        // 获取用户钱包
        Wallet wallet = getWalletByUserId(userId);
        
        // 查询交易记录
        LambdaQueryWrapper<WalletTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WalletTransaction::getWalletId, wallet.getId());
        queryWrapper.orderByDesc(WalletTransaction::getCreateTime);
        
        Page<WalletTransaction> pageParam = new Page<>(page, size);
        Page<WalletTransaction> pageResult = walletTransactionMapper.selectPage(pageParam, queryWrapper);
        
        return pageResult.getRecords();
    }
    
    @Override
    public int countTransactionsByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        // 获取用户钱包
        Wallet wallet = getWalletByUserId(userId);
        
        // 统计交易记录
        LambdaQueryWrapper<WalletTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WalletTransaction::getWalletId, wallet.getId());
        
        return walletTransactionMapper.selectCount(queryWrapper).intValue();
    }
    
    @Override
    @Transactional
    public boolean payOrder(Long userId, String orderNo) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "订单号不能为空");
        }
        
        // 获取订单信息
        LambdaQueryWrapper<Order> orderQueryWrapper = new LambdaQueryWrapper<>();
        orderQueryWrapper.eq(Order::getOrderNo, orderNo);
        orderQueryWrapper.eq(Order::getUserId, userId);
        
        Order order = orderMapper.selectOne(orderQueryWrapper);
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "订单不存在");
        }
        
        // 检查订单状态
        if (order.getStatus() != OrderStatusEnum.PENDING_PAYMENT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "该订单不是待支付状态");
        }
        
        // 获取用户钱包
        Wallet wallet = getWalletByUserId(userId);
        
        // 从订单中获取积分抵扣信息
        Integer pointsUsed = order.getPointsUsed() != null ? order.getPointsUsed() : 0;
        BigDecimal pointsDeduction = order.getPointsDeductionAmount() != null ? order.getPointsDeductionAmount() : BigDecimal.ZERO;
        
        // 计算实际支付金额，已包含积分抵扣
        BigDecimal actualPayAmount = order.getActualPaymentAmount();
        
        // 检查钱包余额是否足够
        if (wallet.getBalance().compareTo(actualPayAmount) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "钱包余额不足");
        }
        
        // 如果使用了积分，检查积分是否足够并扣减
        if (pointsUsed > 0) {
            PointsAccount pointsAccount = pointsService.getPointsAccountByUserId(userId);
            if (pointsAccount.getTotalPoints() < pointsUsed) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "积分不足");
            }
            
            // 使用积分
            pointsService.usePoints(
                userId, 
                pointsUsed, 
                PointsTransactionTypeEnum.ORDER_DEDUCTION_USED, 
                "订单抵扣: " + orderNo, 
                order.getId()
            );
        }
        
        // 更新钱包余额
        BigDecimal newBalance = wallet.getBalance().subtract(actualPayAmount);
        wallet.setBalance(newBalance);
        wallet.setUpdateTime(LocalDateTime.now());
        
        walletMapper.updateById(wallet);
        
        // 记录交易
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setRelatedOrderId(order.getId());
        transaction.setAmount(actualPayAmount.negate()); // 支付是负数
        transaction.setType(WalletTransactionTypeEnum.CONSUMPTION.getCode());
        transaction.setDescription("订单支付: " + orderNo);
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setCreateTime(LocalDateTime.now());
        
        walletTransactionMapper.insert(transaction);
        
        // 更新订单状态
        order.setStatus(OrderStatusEnum.PENDING_SHIPMENT); // 修改为待发货状态
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        // 处理积分抵扣
        if (pointsUsed > 0) {
            order.setPointsUsed(pointsUsed);
            order.setPointsDeductionAmount(pointsDeduction);
        }
        
        orderMapper.updateById(order);
        
        return true;
    }
    
    @Override
    @Transactional
    public Wallet addPoints(Long userId, Integer points) {
        // 获取钱包信息（为了保持接口兼容性）
        Wallet wallet = getWalletByUserId(userId);
        
        // 使用积分服务添加积分
        pointsService.addPoints(
            userId, 
            points, 
            PointsTransactionTypeEnum.SYSTEM_REWARD, 
            "系统奖励积分", 
            null
        );
        
        return wallet;
    }
    
    @Override
    @Transactional
    public Wallet usePoints(Long userId, Integer points) {
        // 获取钱包信息（为了保持接口兼容性）
        Wallet wallet = getWalletByUserId(userId);
        
        // 使用积分服务扣减积分
        pointsService.usePoints(
            userId, 
            points, 
            PointsTransactionTypeEnum.SYSTEM_DEDUCTION, 
            "系统扣减积分", 
            null
        );
        
        return wallet;
    }
    
    @Override
    @Transactional
    public boolean addOrderIncome(Long userId, Long orderId, String orderNo, BigDecimal amount) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "收入金额必须大于0");
        }
        
        // 获取商家钱包（实际是用户钱包）
        Wallet wallet = getWalletByUserId(userId);
        
        // 更新钱包余额
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        wallet.setUpdateTime(LocalDateTime.now());
        
        walletMapper.updateById(wallet);
        
        // 记录交易
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setRelatedOrderId(orderId);
        transaction.setAmount(amount); // 收入是正数
        transaction.setType(WalletTransactionTypeEnum.MERCHANT_INCOME.getCode());
        transaction.setDescription("订单收入: " + orderNo);
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setCreateTime(LocalDateTime.now());
        
        walletTransactionMapper.insert(transaction);
        
        log.info("商家(用户ID:{})收到订单[{}]收入: {}", userId, orderNo, amount);
        return true;
    }
    
    @Override
    @Transactional
    public boolean refundToBuyer(Long userId, Long orderId, String orderNo, BigDecimal amount) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "退款金额必须大于0");
        }
        
        // 获取用户钱包
        Wallet wallet = getWalletByUserId(userId);
        
        // 更新钱包余额
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        wallet.setUpdateTime(LocalDateTime.now());
        
        walletMapper.updateById(wallet);
        
        // 记录交易
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setRelatedOrderId(orderId);
        transaction.setAmount(amount); // 退款是正数
        transaction.setType(WalletTransactionTypeEnum.REFUND.getCode());
        transaction.setDescription("订单退款: " + orderNo);
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setCreateTime(LocalDateTime.now());
        
        walletTransactionMapper.insert(transaction);
        
        log.info("用户[{}]收到订单[{}]退款: {}", userId, orderNo, amount);
        return true;
    }
    
    @Override
    @Transactional
    public boolean deductFromMerchant(Long merchantUserId, Long orderId, String orderNo, BigDecimal amount) {
        if (merchantUserId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "商家用户ID不能为空");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "扣款金额必须大于0");
        }
        
        // 获取商家钱包
        Wallet wallet = getWalletByUserId(merchantUserId);
        
        // 检查余额是否足够
        if (wallet.getBalance().compareTo(amount) < 0) {
            log.error("商家[{}]钱包余额不足，无法扣款，当前余额: {}, 需扣款: {}", 
                    merchantUserId, wallet.getBalance(), amount);
            // 依然继续处理，允许商家余额为负，平台承担风险
            // 实际业务中可能需要更严格的处理，此处简化处理
        }
        
        // 更新钱包余额
        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        wallet.setUpdateTime(LocalDateTime.now());
        
        walletMapper.updateById(wallet);
        
        // 记录交易
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setRelatedOrderId(orderId);
        transaction.setAmount(amount.negate()); // 扣款是负数
        transaction.setType(WalletTransactionTypeEnum.REFUND.getCode());
        transaction.setDescription("订单退款支出: " + orderNo);
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setCreateTime(LocalDateTime.now());
        
        walletTransactionMapper.insert(transaction);
        
        log.info("商家[{}]订单[{}]退款支出: {}", merchantUserId, orderNo, amount);
        return true;
    }
} 