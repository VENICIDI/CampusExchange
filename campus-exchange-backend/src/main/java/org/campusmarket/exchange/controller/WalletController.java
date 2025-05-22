package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import lombok.Data;
import org.campusmarket.exchange.dto.PointsTransactionDTO;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.dto.WalletTransactionDTO;
import org.campusmarket.exchange.entity.PointsAccount;
import org.campusmarket.exchange.entity.PointsTransaction;
import org.campusmarket.exchange.entity.Wallet;
import org.campusmarket.exchange.entity.WalletTransaction;
import org.campusmarket.exchange.enums.WalletTransactionTypeEnum;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.service.IPointsService;
import org.campusmarket.exchange.service.IWalletService;
import org.campusmarket.exchange.util.UserContext;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 钱包控制器
 */
@RestController
@RequestMapping("/api/wallet")
@Validated
public class WalletController {

    @Resource
    private IWalletService walletService;
    
    @Resource
    private IPointsService pointsService;

    /**
     * 获取用户钱包信息
     * @return 钱包信息（余额、积分等）
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getWalletInfo() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        // 获取钱包信息
        Wallet wallet = walletService.getWalletByUserId(userId);
        
        // 获取积分账户信息
        PointsAccount pointsAccount = pointsService.getPointsAccountByUserId(userId);
        
        // 构建钱包信息响应
        Map<String, Object> walletInfo = new HashMap<>();
        walletInfo.put("id", wallet.getId());
        walletInfo.put("userId", wallet.getUserId());
        walletInfo.put("balance", wallet.getBalance());
        walletInfo.put("frozenAmount", wallet.getFrozenAmount());
        walletInfo.put("points", pointsAccount.getTotalPoints());
        walletInfo.put("createTime", wallet.getCreateTime());
        walletInfo.put("updateTime", wallet.getUpdateTime());
        
        return Result.success(walletInfo);
    }

    /**
     * 钱包充值
     * @param rechargeDTO 充值参数
     * @return 充值结果
     */
    @PostMapping("/recharge")
    public Result<Map<String, Object>> rechargeWallet(@RequestBody RechargeDTO rechargeDTO) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        
        BigDecimal amount = rechargeDTO.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "充值金额必须大于0");
        }
        
        Wallet wallet = walletService.recharge(userId, amount);
        
        Map<String, Object> result = new HashMap<>();
        result.put("balance", wallet.getBalance());
        
        return Result.success(result);
    }

    /**
     * 钱包提现
     * @param withdrawDTO 提现参数
     * @return 提现结果
     */
    @PostMapping("/withdraw")
    public Result<Map<String, Object>> withdrawWallet(@RequestBody WithdrawDTO withdrawDTO) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        
        BigDecimal amount = withdrawDTO.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "提现金额必须大于0");
        }
        
        Wallet wallet = walletService.withdraw(userId, amount);
        
        Map<String, Object> result = new HashMap<>();
        result.put("balance", wallet.getBalance());
        
        return Result.success(result);
    }

    /**
     * 获取钱包交易记录
     * @param page 页码
     * @param size 每页记录数
     * @return 交易记录列表
     */
    @GetMapping("/transactions")
    public Result<Map<String, Object>> getWalletTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // 如果指定了类型为积分，则返回积分交易记录
        if ("POINTS".equalsIgnoreCase(type)) {
            List<PointsTransaction> transactions = pointsService.getPointsTransactionsByUserId(userId, page, size);
            // 将实体类转换为DTO
            List<PointsTransactionDTO> transactionDTOs = transactions.stream()
                    .map(PointsTransactionDTO::fromEntity)
                    .collect(Collectors.toList());
            
            int total = pointsService.countPointsTransactionsByUserId(userId);
            
            result.put("records", transactionDTOs);
            result.put("total", total);
            result.put("pages", (total + size - 1) / size);
            result.put("current", page);
            result.put("size", size);
        } else {
            // 否则返回钱包交易记录
            List<WalletTransaction> transactions = walletService.getTransactionsByUserId(userId, page, size);
            // 将实体类转换为DTO
            List<WalletTransactionDTO> transactionDTOs = transactions.stream()
                    .map(WalletTransactionDTO::fromEntity)
                    .collect(Collectors.toList());
            
            int total = walletService.countTransactionsByUserId(userId);
            
            result.put("records", transactionDTOs);
            result.put("total", total);
            result.put("pages", (total + size - 1) / size);
            result.put("current", page);
            result.put("size", size);
        }
        
        return Result.success(result);
    }

    /**
     * 使用钱包支付订单
     * @param paymentDTO 支付参数
     * @return 支付结果
     */
    @PostMapping("/wallet-pay")
    public Result<Map<String, Object>> payWithWallet(@RequestBody WalletPaymentDTO paymentDTO) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        
        String orderNo = paymentDTO.getOrderNo();
        Integer pointsUsed = paymentDTO.getPointsUsed();
        
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "订单号不能为空");
        }
        
        boolean success = walletService.payOrder(userId, orderNo, pointsUsed != null ? pointsUsed : 0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("status", success ? "PAID" : "PAYMENT_FAILED");
        
        return Result.success(result);
    }

    @Data
    public static class RechargeDTO {
        private BigDecimal amount;
    }

    @Data
    public static class WithdrawDTO {
        private BigDecimal amount;
    }

    @Data
    public static class WalletPaymentDTO {
        private String orderNo;
        private Integer pointsUsed;
    }
} 