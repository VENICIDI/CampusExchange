package org.campusmarket.exchange.service;

import org.campusmarket.exchange.entity.Category;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface ICategoryService {

    /**
     * 获取所有分类列表
     * @return 分类列表
     */
    List<Category> getAllCategories();
    
    /**
     * 获取顶级分类列表
     * @return 顶级分类列表
     */
    List<Category> getTopCategories();
    
    /**
     * 获取子分类列表
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> getChildCategories(Long parentId);
    
    /**
     * 获取分类详情
     * @param id 分类ID
     * @return 分类详情
     */
    Category getCategoryById(Long id);
    
    /**
     * 获取分类名称
     * @param id 分类ID
     * @return 分类名称
     */
    String getCategoryName(Long id);
} 