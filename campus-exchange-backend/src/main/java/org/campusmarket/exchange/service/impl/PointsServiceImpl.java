package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.campusmarket.exchange.entity.PointsAccount;
import org.campusmarket.exchange.entity.PointsTransaction;
import org.campusmarket.exchange.enums.PointsTransactionTypeEnum;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.mapper.PointsAccountMapper;
import org.campusmarket.exchange.mapper.PointsTransactionMapper;
import org.campusmarket.exchange.service.IPointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分服务实现类
 */
@Service
public class PointsServiceImpl implements IPointsService {
    
    private static final Logger log = LoggerFactory.getLogger(PointsServiceImpl.class);
    
    @Resource
    private PointsAccountMapper pointsAccountMapper;
    
    @Resource
    private PointsTransactionMapper pointsTransactionMapper;
    
    @Override
    public PointsAccount getPointsAccountByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        LambdaQueryWrapper<PointsAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsAccount::getUserId, userId);
        
        PointsAccount pointsAccount = pointsAccountMapper.selectOne(queryWrapper);
        
        // 如果用户积分账户不存在，则创建一个新的积分账户
        if (pointsAccount == null) {
            pointsAccount = new PointsAccount();
            pointsAccount.setUserId(userId);
            pointsAccount.setTotalPoints(0);
            pointsAccount.setCreateTime(LocalDateTime.now());
            pointsAccount.setUpdateTime(LocalDateTime.now());
            
            pointsAccountMapper.insert(pointsAccount);
        }
        
        return pointsAccount;
    }
    
    @Override
    @Transactional
    public PointsAccount addPoints(Long userId, Integer points, PointsTransactionTypeEnum type, String description, Long relatedOrderId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        if (points == null || points <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "积分数量必须大于0");
        }
        
        // 获取用户积分账户
        PointsAccount pointsAccount = getPointsAccountByUserId(userId);
        
        // 更新积分
        Integer newPoints = pointsAccount.getTotalPoints() + points;
        pointsAccount.setTotalPoints(newPoints);
        pointsAccount.setUpdateTime(LocalDateTime.now());
        
        pointsAccountMapper.updateById(pointsAccount);
        
        // 记录交易
        PointsTransaction transaction = new PointsTransaction();
        transaction.setPointsAccountId(pointsAccount.getId());
        transaction.setRelatedOrderId(relatedOrderId);
        transaction.setPointsChange(points);
        transaction.setType(type.getCode());
        transaction.setDescription(description);
        transaction.setBalanceAfterTransaction(newPoints);
        transaction.setCreateTime(LocalDateTime.now());
        
        pointsTransactionMapper.insert(transaction);
        
        return pointsAccount;
    }
    
    @Override
    @Transactional
    public PointsAccount usePoints(Long userId, Integer points, PointsTransactionTypeEnum type, String description, Long relatedOrderId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        if (points == null || points <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "积分数量必须大于0");
        }
        
        // 获取用户积分账户
        PointsAccount pointsAccount = getPointsAccountByUserId(userId);
        
        // 检查积分是否足够
        if (pointsAccount.getTotalPoints() < points) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "积分不足");
        }
        
        // 更新积分
        Integer newPoints = pointsAccount.getTotalPoints() - points;
        pointsAccount.setTotalPoints(newPoints);
        pointsAccount.setUpdateTime(LocalDateTime.now());
        
        pointsAccountMapper.updateById(pointsAccount);
        
        // 记录交易
        PointsTransaction transaction = new PointsTransaction();
        transaction.setPointsAccountId(pointsAccount.getId());
        transaction.setRelatedOrderId(relatedOrderId);
        transaction.setPointsChange(-points);  // 使用积分是负数
        transaction.setType(type.getCode());
        transaction.setDescription(description);
        transaction.setBalanceAfterTransaction(newPoints);
        transaction.setCreateTime(LocalDateTime.now());
        
        pointsTransactionMapper.insert(transaction);
        
        return pointsAccount;
    }
    
    @Override
    public List<PointsTransaction> getPointsTransactionsByUserId(Long userId, Integer page, Integer size) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        // 获取用户积分账户
        PointsAccount pointsAccount = getPointsAccountByUserId(userId);
        
        // 查询交易记录
        LambdaQueryWrapper<PointsTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsTransaction::getPointsAccountId, pointsAccount.getId());
        queryWrapper.orderByDesc(PointsTransaction::getCreateTime);
        
        Page<PointsTransaction> pageParam = new Page<>(page, size);
        Page<PointsTransaction> pageResult = pointsTransactionMapper.selectPage(pageParam, queryWrapper);
        
        return pageResult.getRecords();
    }
    
    @Override
    public int countPointsTransactionsByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空");
        }
        
        // 获取用户积分账户
        PointsAccount pointsAccount = getPointsAccountByUserId(userId);
        
        // 统计交易记录
        LambdaQueryWrapper<PointsTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsTransaction::getPointsAccountId, pointsAccount.getId());
        
        return pointsTransactionMapper.selectCount(queryWrapper).intValue();
    }
} 