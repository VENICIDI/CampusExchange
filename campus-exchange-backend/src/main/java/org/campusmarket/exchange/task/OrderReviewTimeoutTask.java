package org.campusmarket.exchange.task;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.entity.Order;
import org.campusmarket.exchange.enums.OrderStatusEnum;
import org.campusmarket.exchange.mapper.OrderMapper;
import org.campusmarket.exchange.service.IReviewService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单评价超时任务
 * 自动完成超过7天未评价的已收货订单
 */
@Slf4j
@Component
public class OrderReviewTimeoutTask {
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private IReviewService reviewService;
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 每天凌晨2点执行，自动完成超时未评价的订单
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void completeTimeoutOrders() {
        log.info("开始执行订单评价超时任务");
        
        try {
            // 查询超过7天未评价的已收货订单
            LocalDateTime deadline = LocalDateTime.now().minusDays(7);
            String sql = "SELECT id, order_no FROM `order` WHERE status = ? AND receipt_confirmation_time < ? LIMIT 100";
            
            List<Order> timeoutOrders = jdbcTemplate.query(sql, 
                    (rs, rowNum) -> {
                        Order order = new Order();
                        order.setId(rs.getLong("id"));
                        order.setOrderNo(rs.getString("order_no"));
                        return order;
                    }, 
                    OrderStatusEnum.RECEIVED.getCode(), deadline);
            
            if (timeoutOrders == null || timeoutOrders.isEmpty()) {
                log.info("没有超时未评价的订单");
                return;
            }
            
            log.info("发现{}个超时未评价的订单", timeoutOrders.size());
            
            // 逐个处理超时订单
            for (Order order : timeoutOrders) {
                try {
                    boolean success = reviewService.autoCompleteOrderByTimeout(order.getOrderNo());
                    if (success) {
                        log.info("自动完成超时订单成功: {}", order.getOrderNo());
                    } else {
                        log.warn("自动完成超时订单失败: {}", order.getOrderNo());
                    }
                } catch (Exception e) {
                    log.error("处理超时订单异常: {}, 错误: {}", order.getOrderNo(), e.getMessage(), e);
                }
            }
            
            log.info("订单评价超时任务执行完成，处理了{}个订单", timeoutOrders.size());
        } catch (Exception e) {
            log.error("订单评价超时任务执行异常: {}", e.getMessage(), e);
        }
    }
} 