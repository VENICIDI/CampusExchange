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
import org.campusmarket.exchange.enums.ProductConditionEnum;
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
                image.setSortOrder(index);
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
        
        // 2. 检查商品状态是否允许更新 - 只锁定状态不允许修改，已售罄允许修改
        if (product.getStatus() == ProductStatusEnum.LOCKED) {
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
                    image.setSortOrder(i);
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
                .orderByAsc(ProductImage::getSortOrder);
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
        log.info("分页查询商品列表: page={}, size={}, categoryId={}, orderBy={}, orderDirection={}", 
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getCategoryId(), 
                queryDTO.getOrderBy(), queryDTO.getOrderDirection());
        
        // 1. 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = Wrappers.<Product>lambdaQuery()
                .eq(queryDTO.getCategoryId() != null, Product::getCategoryId, queryDTO.getCategoryId())
                .eq(queryDTO.getMerchantId() != null, Product::getMerchantId, queryDTO.getMerchantId())
                .ge(queryDTO.getMinPrice() != null, Product::getCurrentPrice, queryDTO.getMinPrice())
                .le(queryDTO.getMaxPrice() != null, Product::getCurrentPrice, queryDTO.getMaxPrice())
                .like(StringUtils.hasText(queryDTO.getKeyword()), Product::getName, queryDTO.getKeyword())
                .eq(Product::getStatus, ProductStatusEnum.ON_SALE);
        
        // 处理商品条件筛选
        List<ProductConditionEnum> conditions = queryDTO.getConditionList();
        if (conditions != null && !conditions.isEmpty()) {
            log.info("添加商品条件筛选: {}", conditions);
            queryWrapper.in(Product::getProductCondition, conditions);
        }

        // 2. 设置排序方式
        String orderBy = queryDTO.getOrderBy();
        String orderDirection = queryDTO.getOrderDirection();
        log.info("应用排序: 字段={}, 方向={}", orderBy, orderDirection);

        boolean isAsc = "asc".equalsIgnoreCase(orderDirection);

        if ("price".equals(orderBy)) {
            // 价格排序: asc-从低到高, desc-从高到低
            log.info("使用价格排序: {}", isAsc ? "从低到高" : "从高到低");
            queryWrapper.orderBy(true, isAsc, Product::getCurrentPrice);
        } else if ("sales".equals(orderBy) || "sales_count".equals(orderBy)) {
            // 销量排序: 强制从高到低(降序)
            log.info("使用销量排序: 从高到低");
            queryWrapper.orderByDesc(Product::getSales);
        } else if ("rating".equals(orderBy) || "average_rating".equals(orderBy)) {
            // 好评度排序: 强制从高到低(降序)
            log.info("使用好评度排序: 从高到低");
            queryWrapper.orderByDesc(Product::getRating);
        } else if ("publish_time".equals(orderBy)) {
            // 发布时间排序: 强制从新到旧(降序)
            log.info("使用发布时间排序: 从新到旧");
            queryWrapper.orderByDesc(Product::getPublishTime);
        } else {
            // 默认按发布时间降序排列
            log.info("使用默认排序: 发布时间从新到旧");
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
                .orderByAsc(ProductImage::getSortOrder);
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
        
        try {
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
                
                try {
                    // 查询商品图片
                    LambdaQueryWrapper<ProductImage> imageWrapper = Wrappers.<ProductImage>lambdaQuery()
                            .eq(ProductImage::getProductId, product.getId())
                            .orderByAsc(ProductImage::getSortOrder);
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
                } catch (Exception e) {
                    log.error("获取商品[{}]图片失败: {}", product.getId(), e.getMessage());
                    // 设置一个空列表，不影响整体返回
                    vo.setImages(new ArrayList<>());
                    vo.setImageUrls(new ArrayList<>());
                }
                
                result.add(vo);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取商家[{}]商品列表失败: {}", merchantId, e.getMessage(), e);
            // 发生异常时返回空列表，避免整个接口崩溃
            return new ArrayList<>();
        }
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
                    .orderByAsc(ProductImage::getSortOrder);
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
                    .orderByAsc(ProductImage::getSortOrder);
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
        
        // 2. 检查商品状态是否允许删除 - 只有锁定状态不允许删除，已售罄允许删除
        if (product.getStatus() == ProductStatusEnum.LOCKED) {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long productId, Integer quantity) {
        log.info("减少商品库存: {} - {}", productId, quantity);
        
        // 查询商品
        Product product = getById(productId);
        if (product == null) {
            log.warn("减少库存失败: 商品不存在");
            return false;
        }
        
        // 检查库存是否足够
        if (product.getStock() < quantity) {
            log.warn("减少库存失败: 库存不足, 当前: {}, 需要: {}", product.getStock(), quantity);
            return false;
        }
        
        // 更新库存
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setStock(product.getStock() - quantity);
        updateProduct.setUpdateTime(LocalDateTime.now());
        
        // 如果库存减少到0，标记为售罄
        if (updateProduct.getStock() == 0) {
            updateProduct.setStatus(ProductStatusEnum.SOLD_OUT);
        }
        
        boolean updated = updateById(updateProduct);
        
        if (updated) {
            log.info("商品库存更新成功, 新库存: {}", updateProduct.getStock());
        } else {
            log.warn("商品库存更新失败");
        }
        
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(Long productId, Integer quantity) {
        log.info("增加商品库存: {} + {}", productId, quantity);
        
        // 查询商品
        Product product = getById(productId);
        if (product == null) {
            log.warn("增加库存失败: 商品不存在");
            return false;
        }
        
        // 更新库存
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setStock(product.getStock() + quantity);
        updateProduct.setUpdateTime(LocalDateTime.now());
        
        // 如果商品当前是售罄状态且有库存了，改为在售状态
        if (product.getStatus() == ProductStatusEnum.SOLD_OUT && updateProduct.getStock() > 0) {
            updateProduct.setStatus(ProductStatusEnum.ON_SALE);
        }
        
        boolean updated = updateById(updateProduct);
        
        if (updated) {
            log.info("商品库存更新成功, 新库存: {}", updateProduct.getStock());
        } else {
            log.warn("商品库存更新失败");
        }
        
        return updated;
    }

    /**
     * 更新商品销量
     * @param productId 商品ID
     * @param quantity 销售数量
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSalesCount(Long productId, Integer quantity) {
        log.info("更新商品销量: {} + {}", productId, quantity);
        
        // 查询商品
        Product product = getById(productId);
        if (product == null) {
            log.warn("更新销量失败: 商品不存在");
            return false;
        }
        
        // 计算新销量
        Integer currentSales = product.getSales() == null ? 0 : product.getSales();
        Integer newSales = currentSales + quantity;
        
        // 更新销量
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setSales(newSales);
        updateProduct.setUpdateTime(LocalDateTime.now());
        
        boolean updated = updateById(updateProduct);
        
        if (updated) {
            log.info("商品销量更新成功, 新销量: {}", newSales);
            
            // 更新商家总销量和销售额
            updateMerchantTotalSales(product.getMerchantId(), quantity, product.getCurrentPrice().multiply(new BigDecimal(quantity)));
        } else {
            log.warn("商品销量更新失败");
        }
        
        return updated;
    }

    /**
     * 减少商品销量（用于退款）
     * @param productId 商品ID
     * @param quantity 减少的销售数量
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseSalesCount(Long productId, Integer quantity) {
        log.info("减少商品销量: {} - {}", productId, quantity);
        
        // 查询商品
        Product product = getById(productId);
        if (product == null) {
            log.warn("减少销量失败: 商品不存在");
            return false;
        }
        
        // 计算新销量，确保不小于0
        Integer currentSales = product.getSales() == null ? 0 : product.getSales();
        Integer newSales = Math.max(0, currentSales - quantity);
        
        // 更新销量
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setSales(newSales);
        updateProduct.setUpdateTime(LocalDateTime.now());
        
        boolean updated = updateById(updateProduct);
        
        if (updated) {
            log.info("商品销量减少成功, 新销量: {}", newSales);
            
            // 更新商家总销量和销售额（负值表示减少）
            updateMerchantTotalSales(product.getMerchantId(), -quantity, 
                product.getCurrentPrice().multiply(new BigDecimal(quantity)).negate());
        } else {
            log.warn("商品销量减少失败");
        }
        
        return updated;
    }

    /**
     * 更新商家总销量和销售额
     * @param merchantId 商家ID
     * @param quantityDelta 销量变化值（正值增加，负值减少）
     * @param amountDelta 销售额变化值（正值增加，负值减少）
     * @return 是否成功
     */
    private boolean updateMerchantTotalSales(Long merchantId, Integer quantityDelta, BigDecimal amountDelta) {
        try {
            if (merchantId == null || quantityDelta == null || amountDelta == null) {
                log.warn("更新商家销量参数不完整: merchantId={}, quantityDelta={}, amountDelta={}", 
                        merchantId, quantityDelta, amountDelta);
                return false;
            }
            
            log.info("更新商家[{}]总销量(+{})和销售额(+{})", merchantId, quantityDelta, amountDelta);
            
            // 查询商家当前信息
            LambdaQueryWrapper<Merchant> queryWrapper = Wrappers.<Merchant>lambdaQuery()
                    .eq(Merchant::getId, merchantId);
            Merchant merchant = merchantMapper.selectOne(queryWrapper);
            
            if (merchant == null) {
                log.warn("更新商家销量失败: 商家不存在 ID={}", merchantId);
                return false;
            }
            
            // 计算新的总销量和销售额
            Integer currentSalesCount = merchant.getTotalSalesCount() == null ? 0 : merchant.getTotalSalesCount();
            BigDecimal currentSalesAmount = merchant.getTotalSalesAmount() == null ? BigDecimal.ZERO : merchant.getTotalSalesAmount();
            
            Integer newSalesCount = currentSalesCount + quantityDelta;
            BigDecimal newSalesAmount = currentSalesAmount.add(amountDelta);
            
            // 更新商家信息
            Merchant updateMerchant = new Merchant();
            updateMerchant.setId(merchantId);
            updateMerchant.setTotalSalesCount(newSalesCount);
            updateMerchant.setTotalSalesAmount(newSalesAmount);
            updateMerchant.setUpdateTime(LocalDateTime.now());
            
            int updated = merchantMapper.updateById(updateMerchant);
            
            if (updated > 0) {
                log.info("商家[{}]总销量和销售额更新成功, 新销量: {}, 新销售额: {}", 
                        merchantId, newSalesCount, newSalesAmount);
                return true;
            } else {
                log.warn("商家[{}]总销量和销售额更新失败", merchantId);
                return false;
            }
        } catch (Exception e) {
            log.error("更新商家总销量和销售额出错: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 管理员查询所有商品(不限状态)
     * @param queryDTO 查询条件
     * @param status 指定状态(可选)
     * @return 分页商品列表
     */
    @Override
    public Page<ProductVO> adminQueryProducts(ProductQueryDTO queryDTO, ProductStatusEnum status) {
        log.info("管理员分页查询商品列表: page={}, size={}, status={}", 
                queryDTO.getPageNum(), queryDTO.getPageSize(), status);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = Wrappers.<Product>lambdaQuery()
                .eq(queryDTO.getCategoryId() != null, Product::getCategoryId, queryDTO.getCategoryId())
                .eq(queryDTO.getMerchantId() != null, Product::getMerchantId, queryDTO.getMerchantId())
                .ge(queryDTO.getMinPrice() != null, Product::getCurrentPrice, queryDTO.getMinPrice())
                .le(queryDTO.getMaxPrice() != null, Product::getCurrentPrice, queryDTO.getMaxPrice())
                .like(StringUtils.hasText(queryDTO.getKeyword()), Product::getName, queryDTO.getKeyword());
        
        // 如果指定了状态，则按状态筛选
        if (status != null) {
            queryWrapper.eq(Product::getStatus, status);
        }
        
        // 2. 设置排序方式
        String orderBy = queryDTO.getOrderBy();
        String orderDirection = queryDTO.getOrderDirection();
        boolean isAsc = "asc".equalsIgnoreCase(orderDirection);

        if ("price".equals(orderBy)) {
            queryWrapper.orderBy(true, isAsc, Product::getCurrentPrice);
        } else if ("sales".equals(orderBy) || "sales_count".equals(orderBy)) {
            queryWrapper.orderByDesc(Product::getSales);
        } else if ("rating".equals(orderBy) || "average_rating".equals(orderBy)) {
            queryWrapper.orderByDesc(Product::getRating);
        } else if ("publish_time".equals(orderBy)) {
            queryWrapper.orderByDesc(Product::getPublishTime);
        } else {
            // 默认按更新时间降序排列
            queryWrapper.orderByDesc(Product::getUpdateTime);
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
                .orderByAsc(ProductImage::getSortOrder);
            List<ProductImage> images = productImageMapper.selectList(imageWrapper);
            
            // 设置图片列表
            List<String> imageUrls = images.stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
            vo.setImages(imageUrls);
            vo.setImageUrls(imageUrls);
            
            ProductImage mainImage = images.stream()
                .filter(ProductImage::getIsMain)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
            if (mainImage != null) {
                vo.setMainImage(mainImage.getImageUrl());
            }

            // 确保正确映射字段
            vo.setProductCondition(product.getProductCondition());
            vo.setSalesCount(product.getSales());
            vo.setSizeInfo(product.getSize());
            vo.setAverageRating(product.getRating());

            return vo;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(productVOList);
        return resultPage;
    }

    @Override
    public long countAllProducts() {
        // 使用通用mapper方法统计所有商品
        return productMapper.selectCount(null);
    }
} 