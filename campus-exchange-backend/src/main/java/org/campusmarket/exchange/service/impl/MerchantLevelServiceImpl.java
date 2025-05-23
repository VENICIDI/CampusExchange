package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.campusmarket.exchange.entity.MerchantLevel;
import org.campusmarket.exchange.mapper.MerchantLevelMapper;
import org.campusmarket.exchange.service.IMerchantLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家等级服务实现类
 */
@Service
public class MerchantLevelServiceImpl extends ServiceImpl<MerchantLevelMapper, MerchantLevel> implements IMerchantLevelService {

    @Autowired
    private MerchantLevelMapper merchantLevelMapper;

    @Override
    public List<MerchantLevel> getAllLevels() {
        LambdaQueryWrapper<MerchantLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(MerchantLevel::getId);
        return merchantLevelMapper.selectList(queryWrapper);
    }

    @Override
    public MerchantLevel getLevelById(Long id) {
        return merchantLevelMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean addLevel(MerchantLevel merchantLevel) {
        if (merchantLevel == null) {
            return false;
        }
        
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        merchantLevel.setCreateTime(now);
        merchantLevel.setUpdateTime(now);
        
        // 确保费率在合理范围内
        if (merchantLevel.getCommissionRate() == null || 
            merchantLevel.getCommissionRate().compareTo(BigDecimal.ZERO) < 0 ||
            merchantLevel.getCommissionRate().compareTo(new BigDecimal("1")) > 0) {
            return false;
        }
        
        int rows = merchantLevelMapper.insert(merchantLevel);
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean updateLevel(MerchantLevel merchantLevel) {
        if (merchantLevel == null || merchantLevel.getId() == null) {
            return false;
        }
        
        // 检查是否存在
        MerchantLevel existing = merchantLevelMapper.selectById(merchantLevel.getId());
        if (existing == null) {
            return false;
        }
        
        // 设置更新时间
        merchantLevel.setUpdateTime(LocalDateTime.now());
        
        // 确保费率在合理范围内
        if (merchantLevel.getCommissionRate() != null && 
            (merchantLevel.getCommissionRate().compareTo(BigDecimal.ZERO) < 0 ||
            merchantLevel.getCommissionRate().compareTo(new BigDecimal("1")) > 0)) {
            return false;
        }
        
        int rows = merchantLevelMapper.updateById(merchantLevel);
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean deleteLevel(Long id) {
        if (id == null) {
            return false;
        }
        
        // 检查等级是否存在
        MerchantLevel level = merchantLevelMapper.selectById(id);
        if (level == null) {
            return false;
        }
        
        // TODO: 可能需要检查是否有商家正在使用这个等级，如果有则不允许删除
        
        int rows = merchantLevelMapper.deleteById(id);
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean updateCommissionRate(Long id, BigDecimal rate) {
        if (id == null || rate == null) {
            return false;
        }
        
        // 检查等级是否存在
        MerchantLevel level = merchantLevelMapper.selectById(id);
        if (level == null) {
            return false;
        }
        
        // 确保费率在合理范围内
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(new BigDecimal("1")) > 0) {
            return false;
        }
        
        // 更新费率和更新时间
        level.setCommissionRate(rate);
        level.setUpdateTime(LocalDateTime.now());
        
        int rows = merchantLevelMapper.updateById(level);
        return rows > 0;
    }
} 