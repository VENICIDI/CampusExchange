package org.campusmarket.exchange.dto;

import lombok.Data;
import org.campusmarket.exchange.entity.PointsTransaction;

import java.time.LocalDateTime;

/**
 * 积分交易记录DTO，用于前端显示
 */
@Data
public class PointsTransactionDTO {
    
    private Long id;
    private Long pointsAccountId;
    private Long relatedOrderId;
    private Integer pointsChange;
    // 我们将此命名为amount以与钱包交易记录保持一致，便于前端处理
    private Integer amount;
    private String type;
    private String description;
    // 映射实体类的balanceAfterTransaction为前端需要的balanceAfter
    private Integer balanceAfter;
    private LocalDateTime createTime;
    
    /**
     * 将实体类转换为DTO
     * @param transaction 积分交易实体
     * @return 积分交易DTO
     */
    public static PointsTransactionDTO fromEntity(PointsTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        PointsTransactionDTO dto = new PointsTransactionDTO();
        dto.setId(transaction.getId());
        dto.setPointsAccountId(transaction.getPointsAccountId());
        dto.setRelatedOrderId(transaction.getRelatedOrderId());
        dto.setPointsChange(transaction.getPointsChange());
        dto.setAmount(transaction.getPointsChange()); // 将pointsChange同时映射到amount字段
        dto.setType(transaction.getType());
        dto.setDescription(transaction.getDescription());
        dto.setBalanceAfter(transaction.getBalanceAfterTransaction());
        dto.setCreateTime(transaction.getCreateTime());
        
        return dto;
    }
} 