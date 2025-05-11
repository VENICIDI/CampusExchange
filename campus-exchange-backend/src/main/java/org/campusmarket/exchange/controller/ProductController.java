package org.campusmarket.exchange.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.campusmarket.exchange.dto.ProductPublishDTO;
import org.campusmarket.exchange.dto.ProductQueryDTO;
import org.campusmarket.exchange.dto.ProductVO;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.entity.Category;
import org.campusmarket.exchange.entity.Merchant;
import org.campusmarket.exchange.enums.ProductStatusEnum;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.service.ICategoryService;
import org.campusmarket.exchange.service.IMerchantService;
import org.campusmarket.exchange.service.IProductService;
import org.campusmarket.exchange.util.UserContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Resource
    private IProductService productService;
    
    @Resource
    private ICategoryService categoryService;
    
    @Resource
    private IMerchantService merchantService;
    
    /**
     * 分页查询商品列表
     * @param queryDTO 查询条件
     * @return 分页商品列表
     */
    @GetMapping
    public Result<Page<ProductVO>> listProducts(@Valid ProductQueryDTO queryDTO) {
        Page<ProductVO> productPage = productService.queryProducts(queryDTO);
        return Result.success(productPage);
    }
    
    /**
     * 获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        ProductVO product = productService.getProductDetail(id);
        return Result.success(product);
    }
    
    /**
     * 搜索商品
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页商品列表
     */
    @GetMapping("/search")
    public Result<Page<ProductVO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ProductVO> productPage = productService.searchProducts(keyword, pageNum, pageSize);
        return Result.success(productPage);
    }
    
    /**
     * 获取商家的商品列表
     * @param merchantId 商家ID
     * @return 商品列表
     */
    @GetMapping("/merchant/{merchantId}")
    public Result<?> getMerchantProducts(@PathVariable Long merchantId) {
        return Result.success(productService.getMerchantProducts(merchantId, null));
    }
    
    /**
     * 获取商品分类列表（用于前端展示分类筛选）
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<List<Category>> listCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
    
    /**
     * 发布商品
     * @param publishDTO 商品发布信息
     * @return 商品ID
     */
    @PostMapping
    public Result<Long> publishProduct(@Valid @RequestBody ProductPublishDTO publishDTO,
                                      @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        // 获取当前登录的用户ID
        Long userId = UserContext.getCurrentUserId();
        
        // 如果UserContext中没有用户ID，则尝试从请求头获取
        if (userId == null && userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                userId = Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "用户未登录或无效的用户ID");
            }
        }
        
        // 如果仍然没有获取到用户ID，则抛出异常
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "用户未登录，请先登录");
        }
        
        // 根据用户ID查询商家信息
        Merchant merchant = merchantService.getMerchantByUserId(userId);
        if (merchant == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), 
                    "当前用户不是商家，无法发布商品，请先申请成为商家");
        }
        
        // 使用商家ID发布商品
        Long productId = productService.publishProduct(merchant.getId(), publishDTO);
        return Result.success(productId);
    }
    
    /**
     * 更新商品信息
     * @param id 商品ID
     * @param publishDTO 商品更新信息
     * @return 是否成功
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductPublishDTO publishDTO) {
        boolean updated = productService.updateProduct(id, publishDTO);
        return Result.success(updated);
    }
    
    /**
     * 删除商品
     * @param id 商品ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return Result.success(deleted);
    }
    
    /**
     * 更新商品状态
     * @param id 商品ID
     * @param status 状态
     * @return 是否成功
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateProductStatus(
            @PathVariable Long id, 
            @RequestParam ProductStatusEnum status) {
        boolean updated = productService.updateProductStatus(id, status);
        return Result.success(updated);
    }
} 