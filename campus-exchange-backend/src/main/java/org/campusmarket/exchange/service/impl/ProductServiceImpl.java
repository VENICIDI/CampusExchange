package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.dto.ProductPublishDTO;
import org.campusmarket.exchange.dto.ProductQueryDTO;
import org.campusmarket.exchange.dto.ProductVO;
import org.campusmarket.exchange.entity.Product;
import org.campusmarket.exchange.entity.ProductImage;
import org.campusmarket.exchange.entity.Merchant;
import org.campusmarket.exchange.enums.ProductStatusEnum;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.mapper.ProductImageMapper;
import org.campusmarket.exchange.mapper.ProductMapper;
import org.campusmarket.exchange.mapper.MerchantMapper;
import org.campusmarket.exchange.service.IProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

/**
 * 商品服务实现类
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductImageMapper productImageMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishProduct(Long merchantId, ProductPublishDTO publishDTO) {
        log.info("商家[{}]发布商品: {}", merchantId, publishDTO.getName());
        
        // 1. 创建商品对象并设置属性
        Product product = new Product();
        BeanUtils.copyProperties(publishDTO, product);
        
        // 2. 设置商家ID和初始状态
        product.setMerchantId(merchantId);
        product.setStatus(ProductStatusEnum.PENDING_APPROVAL); // 默认为待审核状态
        product.setSales(0); // 初始销量为0
        product.setRating(BigDecimal.ZERO); // 初始评分为0
        
        // 设置尺寸信息，确保从DTO的size字段映射到实体的sizeInfo字段
        if (publishDTO.getSize() != null) {
            product.setSize(publishDTO.getSize());
        }
        
        // 确保从DTO的productCondition字段映射到实体的productCondition字段
        if (publishDTO.getProductCondition() != null) {
            product.setProductCondition(publishDTO.getProductCondition());
        } else if (publishDTO.getCondition() != null) {
            // 兼容旧代码
            product.setProductCondition(publishDTO.getCondition());
        }
        
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        
        // 3. 保存商品基本信息
        boolean saved = save(product);
        if (!saved) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "商品发布失败");
        }
        
        // 4. 保存商品图片
        if (publishDTO.getImageUrls() != null && !publishDTO.getImageUrls().isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            int index = 0;
            
            for (String url : publishDTO.getImageUrls()) {
                ProductImage image = new ProductImage();
                image.setProductId(product.getId());
                image.setImageUrl(url);
                image.setCreateTime(LocalDateTime.now());
                image.setSort(index);
                image.setIsMain(index == 0);
                images.add(image);
                index++;
            }
            
            for (ProductImage image : images) {
                productImageMapper.insert(image);
            }
        }
        
        log.info("商品发布成功，ID: {}", product.getId());
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProduct(Long productId, ProductPublishDTO publishDTO) {
        log.info("更新商品信息: {}", productId);
        
        // 1. 查询商品是否存在
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "商品不存在");
        }
        
        // 2. 检查商品状态是否允许更新
        if (product.getStatus() == ProductStatusEnum.SOLD_OUT || product.getStatus() == ProductStatusEnum.LOCKED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "商品当前状态(" + product.getStatus().getDesc() + ")不允许修改");
        }
        
        // 3. 更新商品信息
        BeanUtils.copyProperties(publishDTO, product);
        
        // 确保从DTO的size字段映射到实体的sizeInfo字段
        if (publishDTO.getSize() != null) {
            product.setSize(publishDTO.getSize());
        }
        
        // 确保从DTO的productCondition字段映射到实体的productCondition字段
        if (publishDTO.getProductCondition() != null) {
            product.setProductCondition(publishDTO.getProductCondition());
        } else if (publishDTO.getCondition() != null) {
            // 兼容旧代码
            product.setProductCondition(publishDTO.getCondition());
        }
        
        product.setUpdateTime(LocalDateTime.now());
        product.setStatus(ProductStatusEnum.PENDING_APPROVAL); // 修改后重新进入审核状态
        
        boolean updated = updateById(product);
        if (!updated) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "商品更新失败");
        }
        
        // 4. 更新商品图片（先删除，再新增）
        if (publishDTO.getImageUrls() != null) {
            // 删除原有图片
            LambdaQueryWrapper<ProductImage> queryWrapper = Wrappers.<ProductImage>lambdaQuery()
                    .eq(ProductImage::getProductId, productId);
            productImageMapper.delete(queryWrapper);
            
            // 添加新图片
            if (!publishDTO.getImageUrls().isEmpty()) {
                List<ProductImage> images = new ArrayList<>();
                for (int i = 0; i < publishDTO.getImageUrls().size(); i++) {
                    String url = publishDTO.getImageUrls().get(i);
                    ProductImage image = new ProductImage();
                    image.setProductId(productId);
                    image.setImageUrl(url);
                    image.setCreateTime(LocalDateTime.now());
                    image.setSort(i);
                    image.setIsMain(i == 0);
                    images.add(image);
                }
                
                for (ProductImage image : images) {
                    productImageMapper.insert(image);
                }
            }
        }
        
        log.info("商品更新成功");
        return true;
    }

    @Override
    public boolean updateProductStatus(Long productId, ProductStatusEnum status) {
        log.info("更新商品状态: {} -> {}", productId, status);
        
        // 1. 查询商品是否存在
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "商品不存在");
        }
        
        // 2. 更新状态
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        
        // 3. 如果是上架状态，设置发布时间
        if (status == ProductStatusEnum.ON_SALE) {
            product.setPublishTime(LocalDateTime.now());
        }
        
        boolean updated = updateById(product);
        log.info("商品状态更新{}", updated ? "成功" : "失败");
        return updated;
    }

    @Override
    public ProductVO getProductDetail(Long productId) {
        log.info("获取商品详情: {}", productId);
        
        // 1. 查询商品基本信息
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "商品不存在");
        }
        
        // 2. 查询商品图片
        LambdaQueryWrapper<ProductImage> imageQueryWrapper = Wrappers.<ProductImage>lambdaQuery()
                .eq(ProductImage::getProductId, productId)
                .orderByAsc(ProductImage::getSort);
        List<ProductImage> images = productImageMapper.selectList(imageQueryWrapper);
        
        // 3. 构建返回对象
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(product, productVO);
        
        // 确保正确映射字段
        productVO.setProductCondition(product.getProductCondition());
        productVO.setSalesCount(product.getSales());
        productVO.setSizeInfo(product.getSize());
        productVO.setAverageRating(product.getRating());
        
        // 4. 设置图片URL列表
        List<String> imageUrls = images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        productVO.setImages(imageUrls);
        productVO.setImageUrls(imageUrls);
        
        // 设置主图
        ProductImage mainImage = images.stream()
                .filter(ProductImage::getIsMain)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
                
        if (mainImage != null) {
            productVO.setMainImage(mainImage.getImageUrl());
        }
        
        // 获取商家店铺名称
        Merchant merchant = merchantMapper.selectById(product.getMerchantId());
        if (merchant != null) {
            productVO.setStoreName(merchant.getStoreName());
        }
        
        return productVO;
    }

    @Override
    public Page<ProductVO> queryProducts(ProductQueryDTO queryDTO) {
        log.info("分页查询商品列表: page={}, size={}, categoryId={}", 
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getCategoryId());
        
        // 1. 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = Wrappers.<Product>lambdaQuery()
                .eq(queryDTO.getCategoryId() != null, Product::getCategoryId, queryDTO.getCategoryId())
                .eq(queryDTO.getMerchantId() != null, Product::getMerchantId, queryDTO.getMerchantId())
                .ge(queryDTO.getMinPrice() != null, Product::getCurrentPrice, queryDTO.getMinPrice())
                .le(queryDTO.getMaxPrice() != null, Product::getCurrentPrice, queryDTO.getMaxPrice())
                .like(StringUtils.hasText(queryDTO.getKeyword()), Product::getName, queryDTO.getKeyword())
                .eq(Product::getStatus, ProductStatusEnum.ON_SALE)
                .orderByDesc(Product::getPublishTime);

        // 2. 设置排序方式
        if ("price".equals(queryDTO.getOrderBy())) {
            queryWrapper.orderBy(true, "asc".equals(queryDTO.getOrderDirection()), Product::getCurrentPrice);
        } else if ("sales".equals(queryDTO.getOrderBy()) || "sales_count".equals(queryDTO.getOrderBy())) {
            queryWrapper.last("ORDER BY sales_count " + ("asc".equals(queryDTO.getOrderDirection()) ? "ASC" : "DESC"));
        } else if ("rating".equals(queryDTO.getOrderBy()) || "average_rating".equals(queryDTO.getOrderBy())) {
            queryWrapper.last("ORDER BY average_rating " + ("asc".equals(queryDTO.getOrderDirection()) ? "ASC" : "DESC"));
        } else if ("publish_time".equals(queryDTO.getOrderBy())) {
            queryWrapper.orderBy(true, "desc".equals(queryDTO.getOrderDirection()), Product::getPublishTime);
        } else {
            // 默认按发布时间降序排列
            queryWrapper.orderByDesc(Product::getPublishTime);
        }
        
        // 3. 执行分页查询
        Page<Product> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Product> productPage = page(page, queryWrapper);
        
        // 4. 转换为VO对象
        Page<ProductVO> resultPage = new Page<>();
        BeanUtils.copyProperties(productPage, resultPage, "records");
        
        // 获取所有商品对应的商家ID
        List<Long> merchantIds = productPage.getRecords().stream()
                .map(Product::getMerchantId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询商家信息
        final Map<Long, String> merchantStoreNames = new HashMap<>();
        if (!merchantIds.isEmpty()) {
            List<Merchant> merchants = merchantMapper.selectBatchIds(merchantIds);
            merchants.forEach(merchant -> merchantStoreNames.put(merchant.getId(), merchant.getStoreName()));
        }
        
        // 转换商品记录
        List<ProductVO> productVOList = productPage.getRecords().stream().map(product -> {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(product, vo);
            vo.setStoreName(merchantStoreNames.getOrDefault(product.getMerchantId(), "未知卖家"));

            // 设置主图
            LambdaQueryWrapper<ProductImage> imageWrapper = Wrappers.<ProductImage>lambdaQuery()
                .eq(ProductImage::getProductId, product.getId())
                .orderByAsc(ProductImage::getSort);
            List<ProductImage> images = productImageMapper.selectList(imageWrapper);
            ProductImage mainImage = images.stream()
                .filter(ProductImage::getIsMain)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
            if (mainImage != null) {
                vo.setMainImage(mainImage.getImageUrl());
            }

            // 设置新旧程度
            vo.setProductCondition(product.getProductCondition());

            return vo;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(productVOList);
        return resultPage;
    }

    @Override
    public List<ProductVO> getMerchantProducts(Long merchantId, ProductStatusEnum status) {
        log.info("获取商家[{}]商品列表, 状态: {}", merchantId, status);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = Wrappers.<Product>lambdaQuery()
                .eq(Product::getMerchantId, merchantId)
                .eq(status != null, Product::getStatus, status)
                .orderByDesc(Product::getUpdateTime);
        
        List<Product> products = list(queryWrapper);
        List<ProductVO> result = new ArrayList<>();
        
        for (Product product : products) {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(product, vo);
            
            // 确保正确映射字段
            vo.setProductCondition(product.getProductCondition());
            vo.setSalesCount(product.getSales());
            vo.setSizeInfo(product.getSize());
            vo.setAverageRating(product.getRating());
            
            // 查询商品图片
            LambdaQueryWrapper<ProductImage> imageWrapper = Wrappers.<ProductImage>lambdaQuery()
                    .eq(ProductImage::getProductId, product.getId())
                    .orderByAsc(ProductImage::getSort);
            List<ProductImage> images = productImageMapper.selectList(imageWrapper);
            
            List<String> imageUrls = images.stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
            
            vo.setImages(imageUrls);
            vo.setImageUrls(imageUrls);
            
            // 设置主图
            ProductImage mainImage = images.stream()
                    .filter(ProductImage::getIsMain)
                    .findFirst()
                    .orElse(images.isEmpty() ? null : images.get(0));
                    
            if (mainImage != null) {
                vo.setMainImage(mainImage.getImageUrl());
            }
            
            result.add(vo);
        }
        
        return result;
    }

    @Override
    public Page<ProductVO> searchProducts(String keyword, Integer pageNum, Integer pageSize) {
        log.info("搜索商品: keyword={}, page={}, size={}", keyword, pageNum, pageSize);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = Wrappers.<Product>lambdaQuery()
                // 只搜索在售商品
                .eq(Product::getStatus, ProductStatusEnum.ON_SALE)
                .and(StringUtils.hasText(keyword), q -> {
                    q.like(Product::getName, keyword)
                            .or()
                            .like(Product::getDescription, keyword);
                })
                .orderByDesc(Product::getPublishTime);
        
        // 2. 执行分页查询
        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> productPage = page(page, queryWrapper);
        
        // 3. 转换为VO对象
        Page<ProductVO> resultPage = new Page<>();
        BeanUtils.copyProperties(productPage, resultPage, "records");
        
        List<ProductVO> productVOList = new ArrayList<>();
        for (Product product : productPage.getRecords()) {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(product, vo);
            
            // 确保正确映射字段
            vo.setProductCondition(product.getProductCondition());
            vo.setSalesCount(product.getSales());
            vo.setSizeInfo(product.getSize());
            vo.setAverageRating(product.getRating());
            
            // 查询商品图片
            LambdaQueryWrapper<ProductImage> imageWrapper = Wrappers.<ProductImage>lambdaQuery()
                    .eq(ProductImage::getProductId, product.getId())
                    .orderByAsc(ProductImage::getSort);
            List<ProductImage> images = productImageMapper.selectList(imageWrapper);
            
            List<String> imageUrls = images.stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
            
            vo.setImages(imageUrls);
            vo.setImageUrls(imageUrls);
            
            // 设置主图
            ProductImage mainImage = images.stream()
                    .filter(ProductImage::getIsMain)
                    .findFirst()
                    .orElse(images.isEmpty() ? null : images.get(0));
                    
            if (mainImage != null) {
                vo.setMainImage(mainImage.getImageUrl());
            }
            
            productVOList.add(vo);
        }
        
        resultPage.setRecords(productVOList);
        return resultPage;
    }

    @Override
    public Page<ProductVO> getPendingProducts(Integer pageNum, Integer pageSize) {
        log.info("获取待审核商品列表: page={}, size={}", pageNum, pageSize);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = Wrappers.<Product>lambdaQuery()
                .eq(Product::getStatus, ProductStatusEnum.PENDING_APPROVAL)
                .orderByAsc(Product::getCreateTime);
        
        // 2. 执行分页查询
        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> productPage = page(page, queryWrapper);
        
        // 3. 转换为VO对象
        Page<ProductVO> resultPage = new Page<>();
        BeanUtils.copyProperties(productPage, resultPage, "records");
        
        List<ProductVO> productVOList = new ArrayList<>();
        for (Product product : productPage.getRecords()) {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(product, vo);
            
            // 查询所有商品图片 
            LambdaQueryWrapper<ProductImage> imageListWrapper = Wrappers.<ProductImage>lambdaQuery()
                    .eq(ProductImage::getProductId, product.getId())
                    .last("ORDER BY sort_order ASC");
            List<ProductImage> images = productImageMapper.selectList(imageListWrapper);
            
            List<String> imageUrls = images.stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
            vo.setImages(imageUrls);
            vo.setImageUrls(imageUrls);
            
            if (!imageUrls.isEmpty()) {
                vo.setMainImage(imageUrls.get(0));
            }
            
            productVOList.add(vo);
        }
        
        resultPage.setRecords(productVOList);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(Long productId) {
        log.info("删除商品: {}", productId);
        
        // 1. 查询商品是否存在
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "商品不存在");
        }
        
        // 2. 检查商品状态是否允许删除
        if (product.getStatus() == ProductStatusEnum.SOLD_OUT || product.getStatus() == ProductStatusEnum.LOCKED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                    "商品当前状态(" + product.getStatus().getDesc() + ")不允许删除");
        }
        
        // 3. 删除商品图片
        LambdaQueryWrapper<ProductImage> queryWrapper = Wrappers.<ProductImage>lambdaQuery()
                .eq(ProductImage::getProductId, productId);
        productImageMapper.delete(queryWrapper);
        
        // 4. 删除商品
        boolean removed = removeById(productId);
        log.info("商品删除{}", removed ? "成功" : "失败");
        return removed;
    }
} 