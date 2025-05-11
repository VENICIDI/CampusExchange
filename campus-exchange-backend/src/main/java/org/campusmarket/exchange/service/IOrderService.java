package org.campusmarket.exchange.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.campusmarket.exchange.dto.OrderCreateDTO;
import org.campusmarket.exchange.dto.OrderDetailVO;
import org.campusmarket.exchange.dto.OrderVO;
import org.campusmarket.exchange.entity.Order;
import org.campusmarket.exchange.enums.OrderStatusEnum;

/**
 * 订单服务接口
 */
public interface IOrderService extends IService<Order> {
    
    /**
     * 创建订单
     * @param userId 用户ID
     * @param orderDTO 订单创建DTO
     * @return 订单编号
     */
    String createOrder(Long userId, OrderCreateDTO orderDTO);
    
    /**
     * 获取订单详情
     * @param orderNo 订单编号
     * @return 订单详情
     */
    OrderDetailVO getOrderDetail(String orderNo);
    
    /**
     * 获取用户的订单列表
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    Page<OrderVO> getUserOrders(Long userId, OrderStatusEnum status, Integer pageNum, Integer pageSize);
    
    /**
     * 获取商家的订单列表
     * @param merchantId 商家ID
     * @param status 订单状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    Page<OrderVO> getMerchantOrders(Long merchantId, OrderStatusEnum status, Integer pageNum, Integer pageSize);
    
    /**
     * 取消订单
     * @param userId 用户ID
     * @param orderNo 订单编号
     * @return 是否成功
     */
    boolean cancelOrder(Long userId, String orderNo);
    
    /**
     * 支付订单（模拟支付）
     * @param userId 用户ID
     * @param orderNo 订单编号
     * @return 是否成功
     */
    boolean payOrder(Long userId, String orderNo);
    
    /**
     * 确认收货
     * @param userId 用户ID
     * @param orderNo 订单编号
     * @return 是否成功
     */
    boolean confirmReceipt(Long userId, String orderNo);
    
    /**
     * 商家发货
     * @param merchantId 商家ID
     * @param orderNo 订单编号
     * @param trackingNo 物流单号
     * @param expressCompany 快递公司
     * @return 是否成功
     */
    boolean shipOrder(Long merchantId, String orderNo, String trackingNo, String expressCompany);
    
    /**
     * 申请退款/退货
     * @param userId 用户ID
     * @param orderNo 订单编号
     * @param reason 原因
     * @return 是否成功
     */
    boolean requestReturn(Long userId, String orderNo, String reason);
    
    /**
     * 商家处理退款申请
     * @param merchantId 商家ID
     * @param orderNo 订单编号
     * @param approve 是否同意
     * @param remark 备注
     * @return 是否成功
     */
    boolean processReturnRequest(Long merchantId, String orderNo, boolean approve, String remark);
} 