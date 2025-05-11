package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.entity.Category;
import org.campusmarket.exchange.service.ICategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    
    @Resource
    private ICategoryService categoryService;
    
    /**
     * 获取所有分类
     * @return 所有分类列表
     */
    @GetMapping
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
    
    /**
     * 获取顶级分类
     * @return 顶级分类列表
     */
    @GetMapping("/top")
    public Result<List<Category>> getTopCategories() {
        List<Category> categories = categoryService.getTopCategories();
        return Result.success(categories);
    }
    
    /**
     * 获取子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @GetMapping("/children")
    public Result<List<Category>> getChildCategories(@RequestParam Long parentId) {
        List<Category> categories = categoryService.getChildCategories(parentId);
        return Result.success(categories);
    }
    
    /**
     * 获取分类详情
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return Result.success(category);
    }
} 