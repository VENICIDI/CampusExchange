package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import org.campusmarket.exchange.dto.MerchantProfileVO;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.entity.Merchant;
import org.campusmarket.exchange.service.IMerchantService;
import org.springframework.web.bind.annotation.*;

/**
 * 商家控制器
 */
@RestController
@RequestMapping("/api/merchants")
public class MerchantController {
    
    @Resource
    private IMerchantService merchantService;
    
    /**
     * 通过用户ID获取商家信息
     * @param userId 用户ID
     * @return 商家信息
     */
    @GetMapping("/user/{userId}")
    public Result<Merchant> getMerchantByUserId(@PathVariable Long userId) {
        Merchant merchant = merchantService.getMerchantByUserId(userId);
        return Result.success(merchant);
    }
    
    /**
     * 获取商家详情
     * @param id 商家ID
     * @return 商家详情
     */
    @GetMapping("/{id}")
    public Result<Merchant> getMerchantById(@PathVariable Long id) {
        // 这里需要添加实际的商家获取逻辑
        Merchant merchant = merchantService.getMerchantById(id);
        return Result.success(merchant);
    }
    
    /**
     * 获取商家主页聚合信息（包含基础信息、等级、头像、联系方式、商品列表、评价等）
     * @param id 商家ID
     * @return 商家主页聚合信息
     */
    @GetMapping("/{id}/profile")
    public Result<MerchantProfileVO> getMerchantProfile(@PathVariable Long id) {
        MerchantProfileVO profileVO = merchantService.getMerchantProfile(id);
        if (profileVO == null) {
            return Result.error(404, "商家不存在");
        }
        return Result.success(profileVO);
    }
    
    /**
     * 更新商家信息
     * @param id 商家ID
     * @param merchant 商家信息
     * @return 是否成功
     */
    @PutMapping("/{id}")
    public Result<?> updateMerchant(@PathVariable Long id, @RequestBody Merchant merchant) {
        merchant.setId(id);
        boolean updated = merchantService.updateMerchant(merchant);
        return updated ? Result.success(null, "更新成功") : Result.error(400, "更新失败");
    }
} 