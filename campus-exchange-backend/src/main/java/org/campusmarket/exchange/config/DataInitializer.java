package org.campusmarket.exchange.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.entity.MerchantLevel;
import org.campusmarket.exchange.mapper.MerchantLevelMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据初始化类
 * 用于初始化系统所需的基础数据
 */
@Slf4j
@Component
public class DataInitializer {

    @Resource
    private MerchantLevelMapper merchantLevelMapper;
    
    /**
     * 应用启动时初始化数据
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化基础数据...");
        initMerchantLevels();
        log.info("基础数据初始化完成");
    }
    
    /**
     * 初始化商家等级数据
     */
    private void initMerchantLevels() {
        log.info("开始初始化商家等级数据...");
        // 商家等级数据: 等级名称 -> (手续费率, 描述)
        Map<String, Object[]> levels = new LinkedHashMap<>();
        levels.put("一级商家", new Object[]{new BigDecimal("0.0050"), "特级商家，享受最低手续费率"}); // 0.5%
        levels.put("二级商家", new Object[]{new BigDecimal("0.0075"), "高级商家，享受优惠手续费率"}); // 0.75%
        levels.put("三级商家", new Object[]{new BigDecimal("0.0100"), "中级商家，享受标准手续费率"}); // 1%
        levels.put("四级商家", new Object[]{new BigDecimal("0.0125"), "普通商家，标准手续费率"}); // 1.25%
        levels.put("五级商家", new Object[]{new BigDecimal("0.0150"), "入门商家，基础手续费率"}); // 1.5%
        
        // 遍历并初始化
        long id = 1;
        for (Map.Entry<String, Object[]> entry : levels.entrySet()) {
            String levelName = entry.getKey();
            Object[] data = entry.getValue();
            
            // 检查是否已存在
            MerchantLevel existingLevel = merchantLevelMapper.selectById(id);
            
            if (existingLevel == null) {
                log.info("创建商家等级: {}", levelName);
                MerchantLevel level = new MerchantLevel();
                level.setId(id);
                level.setLevelName(levelName);
                level.setCommissionRate((BigDecimal) data[0]);
                level.setDescription((String) data[1]);
                level.setCreateTime(LocalDateTime.now());
                level.setUpdateTime(LocalDateTime.now());
                
                merchantLevelMapper.insert(level);
            }
            id++;
        }
        
        log.info("商家等级数据初始化完成");
    }
} 