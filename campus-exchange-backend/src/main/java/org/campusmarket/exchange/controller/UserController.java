package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.entity.User;
import org.campusmarket.exchange.enums.UserStatusEnum;
import org.campusmarket.exchange.service.IUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Resource
    private IUserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        // 实际应该从Spring Security获取当前登录用户信息
        return Result.error(400, "暂未实现");
    }
    
    /**
     * 更新用户个人信息
     */
    @PutMapping("/profile")
    public Result<?> updateUserProfile(@RequestBody User user) {
        // 实际应该从Spring Security获取当前登录用户ID，并限制只能修改自己的信息
        boolean success = userService.updateUser(user);
        return success ? Result.success(null, "更新成功") : Result.error(400, "更新失败");
    }
    
    /**
     * 管理员获取待审核用户列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<User>> getPendingUsers() {
        List<User> pendingUsers = userService.getPendingUsers();
        return Result.success(pendingUsers);
    }
    
    /**
     * 管理员审核用户
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateUserStatus(
            @PathVariable Long userId, 
            @RequestParam UserStatusEnum status) {
        boolean success = userService.updateUserStatus(userId, status);
        return success ? Result.success(null, "状态更新成功") : Result.error(400, "状态更新失败");
    }
} 