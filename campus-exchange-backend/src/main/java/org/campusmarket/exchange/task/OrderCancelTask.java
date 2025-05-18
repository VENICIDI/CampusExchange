package org.campusmarket.exchange.task;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.entity.Order;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.mapper.OrderMapper;
import org.campusmarket.exchange.service.IOrderService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单自动取消任务
 * 自动取消超过24小时未支付的订单
 */
@Slf4j
@Component
public class OrderCancelTask {
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private IOrderService orderService;
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 每小时执行一次，自动取消超时未支付的订单
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        log.info("开始执行订单自动取消任务");
        
        try {
            // 查询超过24小时未支付的订单
            LocalDateTime deadline = LocalDateTime.now().minusHours(24);
            String sql = "SELECT id, order_no, user_id FROM `order` WHERE status = ? AND create_time < ? LIMIT 100";
            
            List<Order> timeoutOrders = jdbcTemplate.query(sql, 
                    (rs, rowNum) -> {
                        Order order = new Order();
                        order.setId(rs.getLong("id"));
                        order.setOrderNo(rs.getString("order_no"));
                        order.setUserId(rs.getLong("user_id"));
                        return order;
                    }, 
                    OrderStatusEnum.PENDING_PAYMENT.getCode(), deadline);
            
            if (timeoutOrders == null || timeoutOrders.isEmpty()) {
                log.info("没有需要自动取消的超时订单");
                return;
            }
            
            log.info("发现{}个超时未支付订单需要自动取消", timeoutOrders.size());
            
            // 逐个处理超时订单
            for (Order order : timeoutOrders) {
                try {
                    boolean success = orderService.systemCancelOrder(order.getUserId(), order.getOrderNo());
                    if (success) {
                        log.info("自动取消超时订单成功: {}", order.getOrderNo());
                    } else {
                        log.warn("自动取消超时订单失败: {}", order.getOrderNo());
                    }
                } catch (Exception e) {
                    log.error("处理超时订单异常: {}, 错误: {}", order.getOrderNo(), e.getMessage(), e);
                }
            }
            
            log.info("订单自动取消任务执行完成，处理了{}个订单", timeoutOrders.size());
        } catch (Exception e) {
            log.error("订单自动取消任务执行异常: {}", e.getMessage(), e);
        }
    }
} 