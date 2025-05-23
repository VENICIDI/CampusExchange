package org.campusmarket.exchange.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.campusmarket.exchange.dto.ProductQueryDTO;
import org.campusmarket.exchange.dto.ProductVO;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.enums.ProductStatusEnum;
import org.campusmarket.exchange.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员商品审核控制器
 */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    
    @Autowired
    private IProductService productService;
    
    /**
     * 获取待审核商品列表
     */
    @GetMapping("/pending")
    public Result<?> getPendingProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ProductVO> pendingProducts = productService.getPendingProducts(pageNum, pageSize);
        return Result.success(pendingProducts);
    }
    
    /**
     * 获取所有商品(分页查询)
     */
    @GetMapping("")
    public Result<?> getAllProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductStatusEnum status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        
        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setPageNum(page);
        queryDTO.setPageSize(size);
        queryDTO.setCategoryId(categoryId);
        queryDTO.setKeyword(keyword);
        queryDTO.setMinPrice(minPrice);
        queryDTO.setMaxPrice(maxPrice);
        
        // 使用管理员专用的查询方法，可以查询所有状态的商品
        Page<ProductVO> pageResult = productService.adminQueryProducts(queryDTO, status);
        
        Map<String, Object> resultMap = Map.of(
            "list", pageResult.getRecords(),
            "total", pageResult.getTotal(),
            "pages", pageResult.getPages()
        );
        return Result.success(resultMap);
    }
    
    /**
     * 审核通过商品
     */
    @PutMapping("/{productId}/approve")
    public Result<?> approveProduct(@PathVariable Long productId) {
        boolean success = productService.updateProductStatus(productId, ProductStatusEnum.ON_SALE);
        if (success) {
            return Result.success("商品审核通过");
        }
        return Result.error("操作失败，请检查商品ID是否存在");
    }
    
    /**
     * 拒绝商品
     */
    @PutMapping("/{productId}/reject")
    public Result<?> rejectProduct(
            @PathVariable Long productId, 
            @RequestParam(required = false) String reason) {
        boolean success = productService.updateProductStatus(productId, ProductStatusEnum.REJECTED_RESUBMIT);
        if (success) {
            // 如果有实现保存拒绝原因的需求，可以在这里处理
            return Result.success("已拒绝商品");
        }
        return Result.error("操作失败，请检查商品ID是否存在");
    }
    
    /**
     * 下架商品
     */
    @PutMapping("/{productId}/off-shelf")
    public Result<?> offShelfProduct(@PathVariable Long productId) {
        boolean success = productService.updateProductStatus(productId, ProductStatusEnum.LOCKED);
        if (success) {
            return Result.success("已下架商品");
        }
        return Result.error("操作失败，请检查商品ID是否存在");
    }
    
    /**
     * 获取商品详情
     */
    @GetMapping("/{productId}")
    public Result<ProductVO> getProductDetail(@PathVariable Long productId) {
        ProductVO product = productService.getProductDetail(productId);
        if (product != null) {
            return Result.success(product);
        }
        return Result.error("商品不存在");
    }
    
    /**
     * 获取商品统计数据
     * @return 商品统计数据
     */
    @GetMapping("/stats")
    public Result<?> getProductStats() {
        long totalProducts = productService.countAllProducts();
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("totalProducts", totalProducts);
        return Result.success(statsMap);
    }
} 