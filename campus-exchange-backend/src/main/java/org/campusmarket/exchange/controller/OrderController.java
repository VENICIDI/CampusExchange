package org.campusmarket.exchange.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.campusmarket.exchange.dto.OrderCreateDTO;
import org.campusmarket.exchange.dto.OrderDetailVO;
import org.campusmarket.exchange.dto.OrderVO;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.dto.UserDTO;
import org.campusmarket.exchange.entity.Merchant;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.enums.RoleEnum;
import org.campusmarket.exchange.enums.TradeTypeEnum;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.service.IMerchantService;
import org.campusmarket.exchange.service.IOrderService;
import org.campusmarket.exchange.util.UserContext;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {
    
    @Resource
    private IOrderService orderService;
    
    @Resource
    private IMerchantService merchantService;
    
    /**
     * 创建订单
     * @param orderDTO 订单创建DTO
     * @return 订单编号
     */
    @PostMapping
    public Result<String> createOrder(@Valid @RequestBody OrderCreateDTO orderDTO) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        String orderNo = orderService.createOrder(userId, orderDTO);
        return Result.success(orderNo);
    }
    
    /**
     * 从购物车创建订单
     * @param cartOrderDTO 从购物车创建订单DTO
     * @return 订单编号列表
     */
    @PostMapping("/from-cart")
    public Result<List<String>> createOrderFromCart(@Valid @RequestBody CartOrderDTO cartOrderDTO) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        List<String> orderNos = orderService.createOrderFromCart(
                userId, 
                cartOrderDTO.getPointsUsed(), 
                cartOrderDTO.getTradeType(),
                cartOrderDTO.getOfflineMeetingLocation(),
                cartOrderDTO.getOfflineMeetingTime(),
                cartOrderDTO.getAddress()
        );
        return Result.success(orderNos);
    }
    
    /**
     * 获取订单详情
     * @param orderNo 订单编号
     * @return 订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable String orderNo) {
        OrderDetailVO orderDetail = orderService.getOrderDetail(orderNo);
        return Result.success(orderDetail);
    }
    
    /**
     * 获取当前用户的订单列表
     * @param status 订单状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    @GetMapping("/user")
    public Result<Page<OrderVO>> getUserOrders(
            @RequestParam(required = false) OrderStatusEnum status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        Page<OrderVO> orders = orderService.getUserOrders(userId, status, pageNum, pageSize);
        return Result.success(orders);
    }
    
    /**
     * 获取当前商家的订单列表
     * @param status 订单状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    @GetMapping("/merchant-orders")
    public Result<Page<OrderVO>> getMerchantOrders(
            @RequestParam(required = false) OrderStatusEnum status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        
        // 检查是否为商家
        Integer role = currentUser.getRole();
        if (role == null || role != RoleEnum.MERCHANT.ordinal()) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "只有商家可以查看商家订单");
        }
        
        // 根据用户ID获取商家ID
        Merchant merchant = merchantService.getMerchantByUserId(currentUser.getId());
        if (merchant == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "未找到商家信息");
        }
        
        Long merchantId = merchant.getId();
        Page<OrderVO> orders = orderService.getMerchantOrders(merchantId, status, pageNum, pageSize);
        return Result.success(orders);
    }
    
    /**
     * 取消订单
     * @param orderNo 订单编号
     * @return 是否成功
     */
    @PostMapping("/{orderNo}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable String orderNo) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        boolean canceled = orderService.cancelOrder(userId, orderNo);
        return Result.success(canceled);
    }
    
    /**
     * 支付订单（模拟支付）
     * @param orderNo 订单编号
     * @return 是否成功
     */
    @PostMapping("/{orderNo}/pay")
    public Result<Boolean> payOrder(@PathVariable String orderNo) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        boolean paid = orderService.payOrder(userId, orderNo);
        return Result.success(paid);
    }
    
    /**
     * 确认收货
     * @param orderNo 订单编号
     * @return 是否成功
     */
    @PostMapping("/{orderNo}/confirm")
    public Result<Boolean> confirmReceipt(@PathVariable String orderNo) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        boolean confirmed = orderService.confirmReceipt(userId, orderNo);
        return Result.success(confirmed);
    }
    
    /**
     * 商家发货
     * @param orderNo 订单编号
     * @return 是否成功
     */
    @PostMapping("/{orderNo}/ship")
    public Result<Boolean> shipOrder(@PathVariable String orderNo) {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        
        // 检查是否为商家
        Integer role = currentUser.getRole();
        if (role == null || role != RoleEnum.MERCHANT.ordinal()) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "只有商家可以发货");
        }
        
        // 根据用户ID获取商家ID
        Merchant merchant = merchantService.getMerchantByUserId(currentUser.getId());
        if (merchant == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "未找到商家信息");
        }
        
        Long merchantId = merchant.getId();
        // 传递空字符串作为不再使用的参数
        boolean shipped = orderService.shipOrder(merchantId, orderNo, "", "");
        return Result.success(shipped);
    }
    
    /**
     * 申请退款/退货
     * @param orderNo 订单编号
     * @param reason 原因
     * @return 是否成功
     */
    @PostMapping("/{orderNo}/return/request")
    public Result<Boolean> requestReturn(
            @PathVariable String orderNo,
            @RequestParam @NotBlank(message = "退货原因不能为空") String reason) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        boolean requested = orderService.requestReturn(userId, orderNo, reason);
        return Result.success(requested);
    }
    
    /**
     * 商家处理退款申请
     * @param orderNo 订单编号
     * @param approve 是否同意
     * @param remark 备注
     * @return 是否成功
     */
    @PostMapping("/{orderNo}/return/process")
    public Result<Boolean> processReturnRequest(
            @PathVariable String orderNo,
            @RequestParam Boolean approve,
            @RequestParam(required = false) String remark) {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        
        // 检查是否为商家
        Integer role = currentUser.getRole();
        if (role == null || role != RoleEnum.MERCHANT.ordinal()) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "只有商家可以处理退款申请");
        }
        
        // 根据用户ID获取商家ID
        Merchant merchant = merchantService.getMerchantByUserId(currentUser.getId());
        if (merchant == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "未找到商家信息");
        }
        
        Long merchantId = merchant.getId();
        boolean processed = orderService.processReturnRequest(merchantId, orderNo, approve, remark);
        return Result.success(processed);
    }
    
    /**
     * 从购物车创建订单DTO
     */
    @Data
    public static class CartOrderDTO {
        
        @NotNull(message = "交易方式不能为空")
        private TradeTypeEnum tradeType;
        
        private Integer pointsUsed;
        
        // 线下交易地点 (线下交易时必填)
        private String offlineMeetingLocation;
        
        // 线下交易时间 (线下交易时必填, 格式: yyyy-MM-dd HH:mm:ss)
        private String offlineMeetingTime;
        
        // 收货地址信息
        private OrderAddressDTO address;
        
        /**
         * 收货地址DTO
         */
        @Data
        public static class OrderAddressDTO {
            // 收货人姓名
            private String receiverName;
            
            // 收货人手机号
            private String receiverPhone;
            
            // 完整地址
            private String fullAddress;
            
            // 是否为默认地址
            private Boolean isDefault;
        }
    }
} 