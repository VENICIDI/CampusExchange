package org.campusmarket.exchange.controller.admin;

import org.campusmarket.exchange.dto.PageResult;
import org.campusmarket.exchange.dto.ProductVO;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.entity.Merchant;
import org.campusmarket.exchange.entity.MerchantLevel;
import org.campusmarket.exchange.enums.ProductStatusEnum;
import org.campusmarket.exchange.service.IMerchantService;
import org.campusmarket.exchange.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 管理员商家管理控制器
 */
@RestController
@RequestMapping("/api/admin/merchants")
public class AdminMerchantController {

    @Autowired
    private IMerchantService merchantService;
    
    @Autowired
    private IProductService productService;
    
    /**
     * 获取商家等级列表
     */
    @GetMapping("/levels")
    public Result<?> getMerchantLevels() {
        List<MerchantLevel> levels = merchantService.getAllMerchantLevels();
        return Result.success(levels);
    }
    
    /**
     * 获取商家列表(分页)
     */
    @GetMapping("")
    public Result<?> getMerchantList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        
        PageResult<Merchant> pageResult = merchantService.getMerchantList(page, size, keyword);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", pageResult.getRecords());
        resultMap.put("total", pageResult.getTotal());
        resultMap.put("pages", pageResult.getPages());
        
        return Result.success(resultMap);
    }
    
    /**
     * 获取商家详情
     */
    @GetMapping("/{merchantId}")
    public Result<?> getMerchantDetail(@PathVariable Long merchantId) {
        Merchant merchant = merchantService.getMerchantById(merchantId);
        if (merchant != null) {
            return Result.success(merchant);
        }
        return Result.error("商家不存在");
    }
    
    /**
     * 通过用户ID获取商家信息
     */
    @GetMapping("/user/{userId}")
    public Result<?> getMerchantByUserId(@PathVariable Long userId) {
        Merchant merchant = merchantService.getMerchantByUserId(userId);
        if (merchant != null) {
            return Result.success(merchant);
        }
        return Result.error("该用户不是商家或商家信息不存在");
    }
    
    /**
     * 更新商家等级
     */
    @PutMapping("/{merchantId}/level")
    public Result<?> updateMerchantLevel(
            @PathVariable Long merchantId,
            @RequestParam Long levelId) {
        
        boolean success = merchantService.updateMerchantLevel(merchantId, levelId);
        if (success) {
            return Result.success("商家等级更新成功");
        }
        return Result.error("更新失败，请检查商家ID和等级ID是否存在");
    }
    
    /**
     * 获取商家的商品列表
     */
    @GetMapping("/{merchantId}/products")
    public Result<?> getMerchantProducts(
            @PathVariable Long merchantId,
            @RequestParam(required = false) ProductStatusEnum status) {
        
        List<ProductVO> products = productService.getMerchantProducts(merchantId, status);
        return Result.success(products);
    }
    
    /**
     * 批量下架商家的所有在售商品
     */
    @PutMapping("/{merchantId}/products/take-down")
    public Result<?> takeDownAllProducts(@PathVariable Long merchantId) {
        int count = merchantService.takeDownAllProducts(merchantId);
        return Result.success("成功下架" + count + "件商品");
    }
} 