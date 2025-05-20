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
    public boolean payOrder(Long userId, String orderNo, Integer pointsUsed) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "订单号不能为空");
        }
        
        // 默认使用0积分
        int points = pointsUsed != null ? pointsUsed : 0;
        
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
        
        // 计算积分抵扣金额（假设100积分=1元）
        BigDecimal pointsDiscount = BigDecimal.valueOf(points / 100.0);
        
        // 计算实际支付金额
        BigDecimal actualPayAmount = order.getActualPaymentAmount().subtract(pointsDiscount);
        if (actualPayAmount.compareTo(BigDecimal.ZERO) < 0) {
            actualPayAmount = BigDecimal.ZERO;
        }
        
        // 检查钱包余额是否足够
        if (wallet.getBalance().compareTo(actualPayAmount) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "钱包余额不足");
        }
        
        // 如果使用了积分，检查积分是否足够并扣减
        if (points > 0) {
            PointsAccount pointsAccount = pointsService.getPointsAccountByUserId(userId);
            if (pointsAccount.getTotalPoints() < points) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "积分不足");
            }
            
            // 使用积分
            pointsService.usePoints(
                userId, 
                points, 
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
        if (points > 0) {
            order.setPointsUsed(points);
            order.setPointsDeductionAmount(pointsDiscount);
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
} 