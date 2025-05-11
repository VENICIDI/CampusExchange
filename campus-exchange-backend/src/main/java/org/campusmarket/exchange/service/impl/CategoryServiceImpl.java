package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.entity.Category;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.mapper.CategoryMapper;
import org.campusmarket.exchange.service.ICategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类服务实现类
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> getAllCategories() {
        log.info("获取所有商品分类");
        LambdaQueryWrapper<Category> queryWrapper = Wrappers.<Category>lambdaQuery()
                .orderByAsc(Category::getLevel)
                .orderByAsc(Category::getSort);
        return list(queryWrapper);
    }

    @Override
    public List<Category> getTopCategories() {
        log.info("获取顶级商品分类");
        LambdaQueryWrapper<Category> queryWrapper = Wrappers.<Category>lambdaQuery()
                .isNull(Category::getParentId)
                .orderByAsc(Category::getSort);
        return list(queryWrapper);
    }

    @Override
    public List<Category> getChildCategories(Long parentId) {
        log.info("获取商品子分类, parentId={}", parentId);
        
        // 判断父分类是否存在
        if (parentId != null) {
            Category parent = getById(parentId);
            if (parent == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND.value(), "父分类不存在");
            }
        }
        
        LambdaQueryWrapper<Category> queryWrapper = Wrappers.<Category>lambdaQuery()
                .eq(Category::getParentId, parentId)
                .orderByAsc(Category::getSort);
        return list(queryWrapper);
    }

    @Override
    public Category getCategoryById(Long id) {
        log.info("获取商品分类详情, id={}", id);
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "分类不存在");
        }
        return category;
    }

    @Override
    public String getCategoryName(Long id) {
        if (id == null) {
            return null;
        }
        
        Category category = getById(id);
        return category != null ? category.getName() : null;
    }
} 