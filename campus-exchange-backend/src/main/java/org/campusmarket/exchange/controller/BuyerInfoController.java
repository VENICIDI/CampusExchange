package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.dto.BuyerInfoVO;
import org.campusmarket.exchange.service.IBuyerInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 买家信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/buyer-info")
public class BuyerInfoController {

    @Resource
    private IBuyerInfoService buyerInfoService;
    
    /**
     * 获取买家信息和评价
     * @param buyerId 买家用户ID
     * @return 买家信息VO
     */
    @GetMapping("/{buyerId}")
    public Result<BuyerInfoVO> getBuyerInfo(@PathVariable("buyerId") Long buyerId) {
        log.info("获取买家信息: {}", buyerId);
        BuyerInfoVO buyerInfoVO = buyerInfoService.getBuyerInfo(buyerId);
        
        if (buyerInfoVO != null) {
            return Result.success(buyerInfoVO);
        } else {
            return Result.error("买家信息不存在");
        }
    }
} 