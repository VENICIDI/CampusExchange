package org.campusmarket.exchange.service;

import org.campusmarket.exchange.entity.PointsAccount;
import org.campusmarket.exchange.entity.PointsTransaction;
import org.campusmarket.exchange.enums.PointsTransactionTypeEnum;

import java.util.List;

/**
 * 积分服务接口
 */
public interface IPointsService {
    
    /**
     * 根据用户ID获取积分账户
     * @param userId 用户ID
     * @return 积分账户信息
     */
    PointsAccount getPointsAccountByUserId(Long userId);
    
    /**
     * 增加用户积分
     * @param userId 用户ID
     * @param points 积分数量
     * @param type 交易类型
     * @param description 描述
     * @param relatedOrderId 关联订单ID（可选）
     * @return 更新后的积分账户
     */
    PointsAccount addPoints(Long userId, Integer points, PointsTransactionTypeEnum type, String description, Long relatedOrderId);
    
    /**
     * 使用用户积分
     * @param userId 用户ID
     * @param points 积分数量
     * @param type 交易类型
     * @param description 描述
     * @param relatedOrderId 关联订单ID（可选）
     * @return 更新后的积分账户
     */
    PointsAccount usePoints(Long userId, Integer points, PointsTransactionTypeEnum type, String description, Long relatedOrderId);
    
    /**
     * 获取用户积分交易记录
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页记录数
     * @return 交易记录列表
     */
    List<PointsTransaction> getPointsTransactionsByUserId(Long userId, Integer page, Integer size);
    
    /**
     * 获取用户积分交易记录总数
     * @param userId 用户ID
     * @return 交易记录总数
     */
    int countPointsTransactionsByUserId(Long userId);
} 