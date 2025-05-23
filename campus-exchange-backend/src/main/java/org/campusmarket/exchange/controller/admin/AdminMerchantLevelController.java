package org.campusmarket.exchange.controller.admin;

import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.entity.MerchantLevel;
import org.campusmarket.exchange.service.IMerchantLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理员商家等级管理控制器
 */
@RestController
@RequestMapping("/api/admin/merchant-levels")
public class AdminMerchantLevelController {
    
    @Autowired
    private IMerchantLevelService merchantLevelService;
    
    /**
     * 获取所有商家等级
     * @return 商家等级列表
     */
    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<MerchantLevel>> getAllLevels() {
        List<MerchantLevel> levels = merchantLevelService.getAllLevels();
        return Result.success(levels);
    }
    
    /**
     * 获取特定商家等级
     * @param id 等级ID
     * @return 商家等级信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<MerchantLevel> getLevelById(@PathVariable Long id) {
        MerchantLevel level = merchantLevelService.getLevelById(id);
        if (level != null) {
            return Result.success(level);
        }
        return Result.error("未找到该等级信息");
    }
    
    /**
     * 添加新的商家等级
     * @param merchantLevel 商家等级信息
     * @return 添加结果
     */
    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> addLevel(@RequestBody MerchantLevel merchantLevel) {
        if (merchantLevel == null) {
            return Result.error("提交的数据不能为空");
        }
        
        // 验证必要字段
        if (merchantLevel.getLevelName() == null || merchantLevel.getLevelName().trim().isEmpty()) {
            return Result.error("等级名称不能为空");
        }
        
        if (merchantLevel.getCommissionRate() == null) {
            return Result.error("手续费率不能为空");
        }
        
        // 检验费率范围
        if (merchantLevel.getCommissionRate().compareTo(BigDecimal.ZERO) < 0 || 
            merchantLevel.getCommissionRate().compareTo(new BigDecimal("1")) > 0) {
            return Result.error("手续费率必须在0到1之间");
        }
        
        boolean success = merchantLevelService.addLevel(merchantLevel);
        if (success) {
            return Result.success("添加商家等级成功");
        }
        return Result.error("添加商家等级失败");
    }
    
    /**
     * 更新商家等级信息
     * @param id 等级ID
     * @param merchantLevel 更新的商家等级信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateLevel(@PathVariable Long id, @RequestBody MerchantLevel merchantLevel) {
        if (merchantLevel == null) {
            return Result.error("提交的数据不能为空");
        }
        
        // 确保ID匹配
        merchantLevel.setId(id);
        
        // 验证必要字段
        if (merchantLevel.getLevelName() != null && merchantLevel.getLevelName().trim().isEmpty()) {
            return Result.error("等级名称不能为空");
        }
        
        // 检验费率范围
        if (merchantLevel.getCommissionRate() != null &&
            (merchantLevel.getCommissionRate().compareTo(BigDecimal.ZERO) < 0 || 
             merchantLevel.getCommissionRate().compareTo(new BigDecimal("1")) > 0)) {
            return Result.error("手续费率必须在0到1之间");
        }
        
        boolean success = merchantLevelService.updateLevel(merchantLevel);
        if (success) {
            return Result.success("更新商家等级成功");
        }
        return Result.error("更新商家等级失败，请检查等级ID是否存在");
    }
    
    /**
     * 仅更新商家等级的手续费率
     * @param id 等级ID
     * @param rate 手续费率
     * @return 更新结果
     */
    @PutMapping("/{id}/commission-rate")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateCommissionRate(@PathVariable Long id, @RequestParam BigDecimal rate) {
        if (rate == null) {
            return Result.error("手续费率不能为空");
        }
        
        // 检验费率范围
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(new BigDecimal("1")) > 0) {
            return Result.error("手续费率必须在0到1之间");
        }
        
        boolean success = merchantLevelService.updateCommissionRate(id, rate);
        if (success) {
            return Result.success("更新手续费率成功");
        }
        return Result.error("更新手续费率失败，请检查等级ID是否存在");
    }
    
    /**
     * 删除商家等级
     * @param id 等级ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteLevel(@PathVariable Long id) {
        boolean success = merchantLevelService.deleteLevel(id);
        if (success) {
            return Result.success("删除商家等级成功");
        }
        return Result.error("删除商家等级失败，可能该等级不存在或已被商家使用");
    }
} 