package org.campusmarket.exchange.service;

import org.campusmarket.exchange.entity.Wallet;
import org.campusmarket.exchange.entity.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包服务接口
 */
public interface IWalletService {
    
    /**
     * 根据用户ID获取钱包信息
     * @param userId 用户ID
     * @return 钱包信息
     */
    Wallet getWalletByUserId(Long userId);
    
    /**
     * 钱包充值
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 更新后的钱包信息
     */
    Wallet recharge(Long userId, BigDecimal amount);
    
    /**
     * 钱包提现
     * @param userId 用户ID
     * @param amount 提现金额
     * @return 更新后的钱包信息
     */
    Wallet withdraw(Long userId, BigDecimal amount);
    
    /**
     * 获取用户交易记录
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页记录数
     * @return 交易记录列表
     */
    List<WalletTransaction> getTransactionsByUserId(Long userId, Integer page, Integer size);
    
    /**
     * 统计用户交易记录总数
     * @param userId 用户ID
     * @return 交易记录总数
     */
    int countTransactionsByUserId(Long userId);
    
    /**
     * 使用钱包支付订单
     * @param userId 用户ID
     * @param orderNo 订单号
     * @param pointsUsed 使用的积分数量
     * @return 支付是否成功
     */
    boolean payOrder(Long userId, String orderNo, Integer pointsUsed);
    
    /**
     * 增加用户积分
     * @param userId 用户ID
     * @param points 积分数量
     * @return 更新后的钱包信息
     */
    Wallet addPoints(Long userId, Integer points);
    
    /**
     * 使用积分
     * @param userId 用户ID
     * @param points 积分数量
     * @return 更新后的钱包信息
     */
    Wallet usePoints(Long userId, Integer points);
} 