package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.ReturnStatusEnum;

import java.time.LocalDateTime;

/**
 * 退货申请实体类，对应数据库 return_request 表
 */
@Data
@TableName("return_request")
public class ReturnRequest {
    
    // 退货申请ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 订单ID (外键, 针对整个订单退货)
    private Long orderId;
    
    // 申请用户ID (买家)
    private Long userId;
    
    // 对应商家ID
    private Long merchantId;
    
    // 退货原因
    private String reason;
    
    // 退货状态: PENDING_APPROVAL-待商家审核, APPROVED_PENDING_RETURN-同意待退货, GOODS_RECEIVED_PENDING_REFUND-收到货待退款, COMPLETED_REFUNDED-已完成退款, REJECTED-已拒绝
    private ReturnStatusEnum status;
    
    // 拒绝原因 (若被拒绝)
    private String rejectionReason;
    
    // 申请时间
    private LocalDateTime applicationTime;
    
    // 商家审核时间
    private LocalDateTime auditTime;
    
    // 买家退回商品时间 (物流信息或线下确认)
    private LocalDateTime goodsReturnedTime;
    
    // 退款完成时间
    private LocalDateTime refundCompletedTime;
    
    // 更新时间
    private LocalDateTime updateTime;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public ReturnStatusEnum getStatus() {
        return status;
    }
    
    public void setStatus(ReturnStatusEnum status) {
        this.status = status;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public LocalDateTime getApplicationTime() {
        return applicationTime;
    }
    
    public void setApplicationTime(LocalDateTime applicationTime) {
        this.applicationTime = applicationTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    @Override
    public String toString() {
        return "ReturnRequest{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", userId=" + userId +
                ", merchantId=" + merchantId +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", applicationTime=" + applicationTime +
                ", updateTime=" + updateTime +
                '}';
    }
} 