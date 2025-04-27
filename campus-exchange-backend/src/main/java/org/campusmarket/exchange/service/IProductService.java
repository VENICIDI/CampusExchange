package org.campusmarket.exchange.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.campusmarket.exchange.dto.ProductPublishDTO;
import org.campusmarket.exchange.dto.ProductQueryDTO;
import org.campusmarket.exchange.dto.ProductVO;
import org.campusmarket.exchange.entity.Product;
import org.campusmarket.exchange.enums.ProductStatusEnum;

import java.util.List;

/**
 * 商品服务接口
 */
public interface IProductService {

    /**
     * 发布商品
     * @param merchantId 商家ID
     * @param publishDTO 发布信息
     * @return 商品ID
     */
    Long publishProduct(Long merchantId, ProductPublishDTO publishDTO);
    
    /**
     * 更新商品信息
     * @param productId 商品ID
     * @param publishDTO 更新信息
     * @return 是否成功
     */
    boolean updateProduct(Long productId, ProductPublishDTO publishDTO);
    
    /**
     * 更新商品状态
     * @param productId 商品ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateProductStatus(Long productId, ProductStatusEnum status);
    
    /**
     * 获取商品详情
     * @param productId 商品ID
     * @return 商品详情
     */
    ProductVO getProductDetail(Long productId);
    
    /**
     * 分页查询商品
     * @param queryDTO 查询条件
     * @return 分页商品列表
     */
    Page<ProductVO> queryProducts(ProductQueryDTO queryDTO);
    
    /**
     * 获取商家的商品列表
     * @param merchantId 商家ID
     * @param status 状态（可选）
     * @return 商品列表
     */
    List<ProductVO> getMerchantProducts(Long merchantId, ProductStatusEnum status);
    
    /**
     * 搜索商品
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页商品列表
     */
    Page<ProductVO> searchProducts(String keyword, Integer pageNum, Integer pageSize);
    
    /**
     * 获取待审核商品列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页商品列表
     */
    Page<ProductVO> getPendingProducts(Integer pageNum, Integer pageSize);
    
    /**
     * 删除商品
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean deleteProduct(Long productId);
} 