package org.campusmarket.exchange.dto;

import lombok.Data;
import org.campusmarket.exchange.entity.WalletTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易记录DTO，用于前端显示
 */
@Data
public class WalletTransactionDTO {
    
    private Long id;
    private Long walletId;
    private Long relatedOrderId;
    private BigDecimal amount;
    private String type;
    private String description;
    // 映射实体类的balanceAfterTransaction为前端需要的balanceAfter
    private BigDecimal balanceAfter;
    private LocalDateTime createTime;
    
    /**
     * 将实体类转换为DTO
     * @param transaction 钱包交易实体
     * @return 钱包交易DTO
     */
    public static WalletTransactionDTO fromEntity(WalletTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        WalletTransactionDTO dto = new WalletTransactionDTO();
        dto.setId(transaction.getId());
        dto.setWalletId(transaction.getWalletId());
        dto.setRelatedOrderId(transaction.getRelatedOrderId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setDescription(transaction.getDescription());
        dto.setBalanceAfter(transaction.getBalanceAfterTransaction());
        dto.setCreateTime(transaction.getCreateTime());
        
        return dto;
    }
} 